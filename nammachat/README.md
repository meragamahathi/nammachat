# NammaChat 💬

A full-stack real-time chat application built with **Java Spring Boot** and **WebSocket (STOMP over SockJS)**. Multiple users can chat simultaneously across different rooms with live presence tracking, typing indicators, and emoji reactions — all powered by a real backend server.

![NammaChat Preview](preview.png)

---

## Tech Stack

| Layer     | Technology                          |
|-----------|-------------------------------------|
| Backend   | Java 21, Spring Boot 3.2            |
| Real-time | WebSocket, STOMP protocol, SockJS   |
| Frontend  | HTML5, CSS3, Vanilla JavaScript     |
| Design    | Glassmorphism UI, animated gradients|

---

## Features

- **Real-time messaging** — messages delivered instantly via WebSocket, no polling
- **Multiple chat rooms** — General, Tech, Gaming, Music, Movies, Random
- **Live presence tracking** — online users list updates in real time on join/leave
- **Typing indicators** — shows when another user is typing
- **Emoji reactions** — react to any message with emoji; reaction counts sync across all users
- **Room history** — last 50 messages loaded when you join a room
- **Custom identity** — choose your avatar emoji and accent color
- **Glassmorphism UI** — dark-themed interface with animated gradient backgrounds

---

## Architecture

```
Browser (SockJS + STOMP.js)
        │
        │  ws://localhost:8080/ws
        ▼
Spring Boot (WebSocketConfig)
        │
        ├── /app/chat.join      → ChatController.joinRoom()
        ├── /app/chat.send      → ChatController.sendMessage()
        ├── /app/chat.typing    → ChatController.typingIndicator()
        ├── /app/chat.react     → ChatController.reactToMessage()
        └── /app/chat.switchRoom→ ChatController.switchRoom()
        │
        ▼
Simple Message Broker
        │
        └── /topic/room.{roomName}  → All subscribers in that room
```

**Message flow:**
1. Client sends STOMP frame to `/app/chat.send`
2. `ChatController` handles it, assigns ID + timestamp, saves to room history
3. Message is broadcast to `/topic/room.{room}` via `SimpMessageSendingOperations`
4. All subscribers in that room receive it instantly

---

## Project Structure

```
nammachat/
├── pom.xml
└── src/
    └── main/
        ├── java/com/nammachat/
        │   ├── NammaChatApplication.java       # Spring Boot entry point
        │   ├── config/
        │   │   ├── WebSocketConfig.java         # STOMP + SockJS configuration
        │   │   └── WebSocketEventListener.java  # Connect/disconnect handlers
        │   ├── controller/
        │   │   └── ChatController.java          # All @MessageMapping handlers
        │   ├── model/
        │   │   ├── ChatMessage.java             # Message model + MessageType enum
        │   │   └── ActiveUser.java              # Online user model
        │   └── service/
        │       └── ActiveUserService.java       # In-memory user + history store
        └── resources/
            ├── application.properties
            └── static/
                └── index.html                  # Frontend (connects to WS)
```

---

## Run Locally

**Prerequisites:** Java 21+, Maven 3.8+

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/nammachat.git
cd nammachat

# Build and run
mvn spring-boot:run
```

Open `http://localhost:8080` in **two or more browser tabs** to test real-time messaging between users.

---

## How WebSocket Works Here

Traditional HTTP is request-response — the client always initiates. WebSocket opens a **persistent bidirectional connection**, so the server can push data to the client instantly.

This project uses **STOMP** (Simple Text Oriented Messaging Protocol) on top of WebSocket, which adds pub/sub semantics:
- Clients **subscribe** to topics like `/topic/room.General`
- Clients **publish** messages to destinations like `/app/chat.send`
- Spring Boot acts as the message broker, routing messages between subscribers

**SockJS** is used as a fallback transport for environments where WebSocket is blocked.

---

## Key Learning Outcomes

- Configuring Spring Boot's `@EnableWebSocketMessageBroker`
- Understanding STOMP protocol's destination-based routing
- Managing WebSocket session lifecycle (connect, disconnect events)
- Building event-driven architecture where the server pushes state changes
- Handling concurrent users safely with `ConcurrentHashMap`

---

## License

MIT
