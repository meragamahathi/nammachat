package com.nammachat.config;

import com.nammachat.model.ChatMessage;
import com.nammachat.service.ActiveUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class WebSocketEventListener {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private ActiveUserService activeUserService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        // Connection is tracked when user sends JOIN message
        System.out.println("[NammaChat] New WebSocket connection established");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        if (sessionAttributes != null) {
            String username = (String) sessionAttributes.get("username");
            String room = (String) sessionAttributes.get("room");
            String avatar = (String) sessionAttributes.get("avatar");
            String color = (String) sessionAttributes.get("color");

            if (username != null) {
                System.out.println("[NammaChat] User disconnected: " + username);

                // Remove from active users
                activeUserService.removeUser(headerAccessor.getSessionId());

                // Broadcast leave message to the room
                ChatMessage leaveMessage = new ChatMessage();
                leaveMessage.setType(ChatMessage.MessageType.LEAVE);
                leaveMessage.setSender(username);
                leaveMessage.setAvatar(avatar != null ? avatar : "👤");
                leaveMessage.setColor(color != null ? color : "#7c6af7");
                leaveMessage.setRoom(room != null ? room : "General");
                leaveMessage.setContent(username + " left the chat");
                leaveMessage.setTimestamp(LocalDateTime.now().toString());

                messagingTemplate.convertAndSend(
                    "/topic/room." + leaveMessage.getRoom(),
                    leaveMessage
                );

                // Broadcast updated user list
                broadcastUserList(leaveMessage.getRoom());
            }
        }
    }

    private void broadcastUserList(String room) {
        ChatMessage userListMsg = new ChatMessage();
        userListMsg.setType(ChatMessage.MessageType.USER_LIST);
        userListMsg.setRoom(room);
        userListMsg.setActiveUsers(activeUserService.getUsersInRoom(room));
        messagingTemplate.convertAndSend("/topic/room." + room, userListMsg);
    }
}
