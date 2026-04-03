package com.nammachat.model;

import java.util.List;
import java.util.Map;

public class ChatMessage {

    public enum MessageType {
        CHAT,       // Normal chat message
        JOIN,       // User joined a room
        LEAVE,      // User left / disconnected
        TYPING,     // Typing indicator
        STOP_TYPING,// Stopped typing
        REACTION,   // Emoji reaction to a message
        USER_LIST,  // Broadcast active user list
        ROOM_HISTORY// Last N messages when joining
    }

    private MessageType type;
    private String messageId;
    private String sender;
    private String avatar;
    private String color;       // User's unique color
    private String content;
    private String room;
    private String timestamp;
    private boolean edited;

    // For REACTION type
    private String targetMessageId;
    private String emoji;

    // For USER_LIST type
    private List<ActiveUser> activeUsers;

    // For ROOM_HISTORY type
    private List<ChatMessage> history;

    // For TYPING
    private boolean isTyping;

    // Reaction map: emoji -> count
    private Map<String, Integer> reactions;

    // --- Constructors ---
    public ChatMessage() {}

    // --- Getters & Setters ---
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }

    public String getTargetMessageId() { return targetMessageId; }
    public void setTargetMessageId(String targetMessageId) { this.targetMessageId = targetMessageId; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public List<ActiveUser> getActiveUsers() { return activeUsers; }
    public void setActiveUsers(List<ActiveUser> activeUsers) { this.activeUsers = activeUsers; }

    public List<ChatMessage> getHistory() { return history; }
    public void setHistory(List<ChatMessage> history) { this.history = history; }

    public boolean isTyping() { return isTyping; }
    public void setTyping(boolean typing) { isTyping = typing; }

    public Map<String, Integer> getReactions() { return reactions; }
    public void setReactions(Map<String, Integer> reactions) { this.reactions = reactions; }
}
