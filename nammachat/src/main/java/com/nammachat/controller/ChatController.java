package com.nammachat.controller;

import com.nammachat.model.ActiveUser;
import com.nammachat.model.ChatMessage;
import com.nammachat.service.ActiveUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
public class ChatController {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private ActiveUserService activeUserService;

    /**
     * Handles: /app/chat.join
     * Called when a user enters a room for the first time.
     */
    @MessageMapping("/chat.join")
    public void joinRoom(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String timestamp = LocalDateTime.now().toString();

        // Store user info in WebSocket session for disconnect tracking
        headerAccessor.getSessionAttributes().put("username", message.getSender());
        headerAccessor.getSessionAttributes().put("room", message.getRoom());
        headerAccessor.getSessionAttributes().put("avatar", message.getAvatar());
        headerAccessor.getSessionAttributes().put("color", message.getColor());

        // Register user in active users map
        activeUserService.addUser(
            sessionId,
            message.getSender(),
            message.getAvatar(),
            message.getColor(),
            message.getRoom(),
            timestamp
        );

        // Send chat history to the joining user
        List<ChatMessage> history = activeUserService.getRoomHistory(message.getRoom());
        if (!history.isEmpty()) {
            ChatMessage historyMsg = new ChatMessage();
            historyMsg.setType(ChatMessage.MessageType.ROOM_HISTORY);
            historyMsg.setRoom(message.getRoom());
            historyMsg.setHistory(history);
            messagingTemplate.convertAndSend("/topic/room." + message.getRoom(), historyMsg);
        }

        // Build and broadcast JOIN notification
        message.setType(ChatMessage.MessageType.JOIN);
        message.setTimestamp(timestamp);
        message.setContent(message.getSender() + " joined " + message.getRoom());
        messagingTemplate.convertAndSend("/topic/room." + message.getRoom(), message);

        // Broadcast updated user list to the room
        broadcastUserList(message.getRoom());

        System.out.println("[NammaChat] " + message.getSender() + " joined room: " + message.getRoom());
    }

    /**
     * Handles: /app/chat.send
     * Called when a user sends a chat message.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message) {
        // Assign unique ID and timestamp
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now().toString());
        message.setType(ChatMessage.MessageType.CHAT);

        // Persist in room history
        activeUserService.saveMessage(message.getRoom(), message);

        // Broadcast to everyone in the room
        messagingTemplate.convertAndSend("/topic/room." + message.getRoom(), message);

        System.out.println("[NammaChat] [" + message.getRoom() + "] " + message.getSender() + ": " + message.getContent());
    }

    /**
     * Handles: /app/chat.typing
     * Broadcasts typing indicator to room (excluding sender via client-side filter).
     */
    @MessageMapping("/chat.typing")
    public void typingIndicator(@Payload ChatMessage message) {
        message.setType(message.isTyping()
            ? ChatMessage.MessageType.TYPING
            : ChatMessage.MessageType.STOP_TYPING
        );
        messagingTemplate.convertAndSend("/topic/room." + message.getRoom(), message);
    }

    /**
     * Handles: /app/chat.react
     * Adds emoji reaction to a specific message.
     */
    @MessageMapping("/chat.react")
    public void reactToMessage(@Payload ChatMessage message) {
        // Update reaction count in history
        activeUserService.addReactionToMessage(
            message.getRoom(),
            message.getTargetMessageId(),
            message.getEmoji()
        );

        // Build reaction broadcast
        message.setType(ChatMessage.MessageType.REACTION);
        message.setTimestamp(LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/room." + message.getRoom(), message);
    }

    /**
     * Handles: /app/chat.switchRoom
     * Called when a user switches to a different room.
     */
    @MessageMapping("/chat.switchRoom")
    public void switchRoom(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String oldRoom = (String) headerAccessor.getSessionAttributes().get("room");

        if (oldRoom != null && !oldRoom.equals(message.getRoom())) {
            // Remove from old room
            activeUserService.removeUser(sessionId);
            broadcastUserList(oldRoom);

            // Send leave notification to old room
            ChatMessage leaveMsg = new ChatMessage();
            leaveMsg.setType(ChatMessage.MessageType.LEAVE);
            leaveMsg.setSender(message.getSender());
            leaveMsg.setAvatar(message.getAvatar());
            leaveMsg.setColor(message.getColor());
            leaveMsg.setRoom(oldRoom);
            leaveMsg.setContent(message.getSender() + " switched rooms");
            leaveMsg.setTimestamp(LocalDateTime.now().toString());
            messagingTemplate.convertAndSend("/topic/room." + oldRoom, leaveMsg);
        }

        // Join new room
        headerAccessor.getSessionAttributes().put("room", message.getRoom());
        activeUserService.addUser(
            sessionId,
            message.getSender(),
            message.getAvatar(),
            message.getColor(),
            message.getRoom(),
            LocalDateTime.now().toString()
        );

        // Send history of new room
        List<ChatMessage> history = activeUserService.getRoomHistory(message.getRoom());
        if (!history.isEmpty()) {
            ChatMessage historyMsg = new ChatMessage();
            historyMsg.setType(ChatMessage.MessageType.ROOM_HISTORY);
            historyMsg.setRoom(message.getRoom());
            historyMsg.setHistory(history);
            messagingTemplate.convertAndSend("/topic/room." + message.getRoom(), historyMsg);
        }

        // Join notification to new room
        message.setType(ChatMessage.MessageType.JOIN);
        message.setTimestamp(LocalDateTime.now().toString());
        message.setContent(message.getSender() + " joined " + message.getRoom());
        messagingTemplate.convertAndSend("/topic/room." + message.getRoom(), message);

        broadcastUserList(message.getRoom());
    }

    // Helper: broadcast updated user list to a room
    private void broadcastUserList(String room) {
        ChatMessage userListMsg = new ChatMessage();
        userListMsg.setType(ChatMessage.MessageType.USER_LIST);
        userListMsg.setRoom(room);
        userListMsg.setActiveUsers(activeUserService.getUsersInRoom(room));
        messagingTemplate.convertAndSend("/topic/room." + room, userListMsg);
    }
}
