package com.nammachat.model;

public class ActiveUser {
    private String sessionId;
    private String username;
    private String avatar;
    private String color;
    private String room;
    private String joinedAt;

    public ActiveUser() {}

    public ActiveUser(String sessionId, String username, String avatar, String color, String room, String joinedAt) {
        this.sessionId = sessionId;
        this.username = username;
        this.avatar = avatar;
        this.color = color;
        this.room = room;
        this.joinedAt = joinedAt;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getJoinedAt() { return joinedAt; }
    public void setJoinedAt(String joinedAt) { this.joinedAt = joinedAt; }
}
