package com.nammachat.service;

import com.nammachat.model.ActiveUser;
import com.nammachat.model.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ActiveUserService {

    // sessionId -> ActiveUser
    private final Map<String, ActiveUser> activeUsers = new ConcurrentHashMap<>();

    // room -> last 50 messages (chat history)
    private final Map<String, LinkedList<ChatMessage>> roomHistory = new ConcurrentHashMap<>();

    private static final int MAX_HISTORY = 50;

    public void addUser(String sessionId, String username, String avatar, String color, String room, String joinedAt) {
        ActiveUser user = new ActiveUser(sessionId, username, avatar, color, room, joinedAt);
        activeUsers.put(sessionId, user);
    }

    public void removeUser(String sessionId) {
        activeUsers.remove(sessionId);
    }

    public List<ActiveUser> getUsersInRoom(String room) {
        return activeUsers.values().stream()
                .filter(u -> room.equals(u.getRoom()))
                .collect(Collectors.toList());
    }

    public List<ActiveUser> getAllUsers() {
        return new ArrayList<>(activeUsers.values());
    }

    public ActiveUser getUserBySession(String sessionId) {
        return activeUsers.get(sessionId);
    }

    public void saveMessage(String room, ChatMessage message) {
        roomHistory.computeIfAbsent(room, k -> new LinkedList<>());
        LinkedList<ChatMessage> history = roomHistory.get(room);
        history.addLast(message);
        if (history.size() > MAX_HISTORY) {
            history.removeFirst();
        }
    }

    public List<ChatMessage> getRoomHistory(String room) {
        return new ArrayList<>(roomHistory.getOrDefault(room, new LinkedList<>()));
    }

    public void addReactionToMessage(String room, String messageId, String emoji) {
        List<ChatMessage> history = getRoomHistory(room);
        for (ChatMessage msg : history) {
            if (messageId.equals(msg.getMessageId())) {
                Map<String, Integer> reactions = msg.getReactions();
                if (reactions == null) {
                    reactions = new ConcurrentHashMap<>();
                    msg.setReactions(reactions);
                }
                reactions.merge(emoji, 1, Integer::sum);
                break;
            }
        }
    }
}
