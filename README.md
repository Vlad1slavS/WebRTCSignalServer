<h1 align="center">📞 SignalServer - WebRTC Video Calling Platform</h1>

<p align="center">
  <strong>Languages:</strong> <a href="README.md">🇺🇸 English</a> | <a href="README.ru.md">🇷🇺 Русский</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring_Boot-3.2+-brightgreen?style=flat-square&logo=spring" alt="Spring Boot">
  <img src="https://img.shields.io/badge/WebRTC-Enabled-blue?style=flat-square&logo=webrtc" alt="WebRTC">
  <img src="https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Redis-7.0+-red?style=flat-square&logo=redis" alt="Redis">
  <img src="https://img.shields.io/badge/WebSocket-Real--time-green?style=flat-square&logo=socketdotio" alt="WebSocket">
</p>

<p align="center">
  <strong>A scalable, high-performance backend server for WebRTC-based video calling with real-time messaging, room management, and advanced media features.</strong>
</p>

---

## 🚀 Features

🎥 **WebRTC Signaling Server** - Complete peer-to-peer connection management with ICE candidates and SDP handling  
🏠 **Smart Room Management** - Create, join, and manage video rooms with customizable settings and permissions  
💬 **Real-time Messaging** - Live chat with file attachments, emoji reactions, and message history  
📹 **Advanced Media Controls** - Video/audio mute, screen sharing, recording, and quality adjustment  
🔐 **Secure Authentication** - JWT-based auth with refresh tokens, email verification, and password reset  
📊 **Session Management** - Track user sessions, connection states, and call statistics  
⚡ **High Performance** - Redis caching, connection pooling, and optimized database queries  
🛡️ **Enterprise Security** - Role-based access, rate limiting, and comprehensive input validation  
📈 **Real-time Analytics** - Live participant tracking, call quality metrics, and usage statistics  
🔄 **Auto Scaling Ready** - Stateless design with Redis session storage for horizontal scaling  
📱 **Multi-device Support** - Cross-platform compatibility with responsive room layouts  
🎛️ **Admin Dashboard** - User management, room monitoring, and system health tracking

---
<!-- 
## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Client    │    │  Mobile Client  │    │  Desktop App    │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────┴──────────────┐
                    │      Load Balancer         │
                    └─────────────┬──────────────┘
                                 │
                 ┌───────────────┼───────────────┐
                 │               │               │
        ┌────────┴────────┐ ┌───────┴────────┐ ┌────────┴────────┐
        │ SignalServer #1 │ │ SignalServer #2│ │ SignalServer #3 │
        └────────┬────────┘ └────────┬───────┘ └────────┬────────┘
                 │                   │                  │
                 └───────────────────┼──────────────────┘
                                     │
    ┌─────────────┬──────────────────┼──────────────────┬─────────────┐
    │             │                  │                  │             │
┌───▼───┐    ┌────▼────┐    ┌────────▼────────┐    ┌────▼───┐   ┌────▼───┐
│ Redis │    │   JWT   │    │   PostgreSQL    │    │  File  │   │ SMTP   │
│ Cache │    │ Service │    │   Database      │    │Storage │   │ Server │
└───────┘    └─────────┘    └─────────────────┘    └────────┘   └────────┘
```

---

## 📦 Installation

### Prerequisites

- **Java 17+** ☕
- **Maven 3.8+** 📦
- **PostgreSQL 14+** 🐘
- **Redis 6.0+** 🗄️
- **SMTP Server** (optional, for emails) 📧

### Quick Start

```bash
# Clone the repository
git clone https://github.com/yourusername/signal-server.git
cd signal-server

# Configure database
createdb signalserver
psql signalserver < src/main/resources/db/schema.sql

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### Docker Setup

```bash
# Run with Docker Compose
docker-compose up -d

# Or build custom image
docker build -t signal-server .
docker run -p 8080:8080 signal-server
```

### Configuration

Edit `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/signalserver
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:password}
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}

  mail:
    host: ${SMTP_HOST:smtp.gmail.com}
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:app-password}

app:
  jwt:
    secret: ${JWT_SECRET:your-secret-key}
    expiration: 86400  # 24 hours
  
  url: ${APP_URL:http://localhost:3000}
  
  file:
    upload-dir: ${UPLOAD_DIR:./uploads}
    max-size: 10MB
```

---

## 💻 API Usage

### Authentication

```bash
# Register new user
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "firstName": "John",
    "lastName": "Doe"
  }'

# Login
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "john@example.com",
    "password": "SecurePass123"
  }'

# Verify email
curl -X POST "http://localhost:8080/api/auth/verify-email?token=verification_token"
```

### Room Management

```bash
# Create a room
curl -X POST "http://localhost:8080/api/rooms" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Team Meeting",
    "description": "Weekly standup meeting",
    "maxParticipants": 10,
    "isPublic": false,
    "password": "meeting123"
  }'

# Join room
curl -X POST "http://localhost:8080/api/rooms/ABC123/join" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "password": "meeting123",
    "mediaSettings": {
      "videoEnabled": true,
      "audioEnabled": true
    }
  }'

# Leave room
curl -X POST "http://localhost:8080/api/rooms/ABC123/leave" \
  -H "Authorization: Bearer $TOKEN"
```

### Real-time Messaging (WebSocket)

```javascript
// Connect to WebSocket
const ws = new WebSocket('ws://localhost:8080/ws/signal');

// Join room signaling
ws.send(JSON.stringify({
  type: 'join-room',
  roomCode: 'ABC123',
  sessionId: 'session-123'
}));

// Send chat message
ws.send(JSON.stringify({
  type: 'chat-message',
  roomCode: 'ABC123',
  message: {
    content: 'Hello everyone!',
    type: 'TEXT'
  }
}));

// WebRTC offer/answer
ws.send(JSON.stringify({
  type: 'webrtc-offer',
  roomCode: 'ABC123',
  targetSessionId: 'session-456',
  sdp: offerSdp
}));
```

### File Upload

```bash
# Upload avatar
curl -X POST "http://localhost:8080/api/files/upload/avatar" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@avatar.jpg"

# Upload chat attachment
curl -X POST "http://localhost:8080/api/files/upload/chat" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@document.pdf" \
  -F "roomCode=ABC123"
```

---

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Integration Tests

```bash
# Run WebSocket tests
mvn test -Dtest=WebSocketIntegrationTest

# Run room management tests
mvn test -Dtest=RoomServiceTest

# Run authentication tests
mvn test -Dtest=AuthServiceTest
```

### Load Testing

```bash
# Install WebSocket load test tool
npm install -g artillery

# Run load test
artillery run load-test.yml
```

---


## 🔧 Advanced Usage

### WebSocket Events

```java
@Component
public class SignalHandler {
    
    @EventListener
    public void handleUserJoined(UserJoinedEvent event) {
        // Notify other participants
        webSocketService.broadcastToRoom(
            event.getRoomCode(), 
            "user-joined", 
            event.getUser()
        );
    }
    
    @EventListener 
    public void handleWebRTCOffer(WebRTCOfferEvent event) {
        // Forward offer to target peer
        webSocketService.sendToSession(
            event.getTargetSessionId(),
            "webrtc-offer",
            event.getSdp()
        );
    }
}
```

### Custom Room Settings

```java
@Service
public class CustomRoomService {
    
    public Room createMeetingRoom(CreateRoomRequest request) {
        Room room = roomService.createRoom(request);
        
        // Custom business logic
        room.getSettings().setRecordingEnabled(true);
        room.getSettings().setWaitingRoom(true);
        
        // Send calendar invites
        emailService.sendRoomInvitations(room, request.getInvitees());
        
        return room;
    }
}
```

### Media Processing

```java
@Component
public class MediaProcessor {
    
    public void processRecording(String roomCode, String recordingUrl) {
        // Process video recording
        CompletableFuture.runAsync(() -> {
            // Convert to different formats
            // Generate thumbnails  
            // Upload to cloud storage
            // Notify participants
        });
    }
    
    public MediaQualityReport analyzeCallQuality(String sessionId) {
        return analyticsService.generateQualityReport(sessionId);
    }
}
```

---

## ⚡️ Troubleshooting

### Common Issues

**❌ Problem:** `WebSocket connection failed`  
**✅ Solution:** Check CORS configuration and firewall settings:

```yaml
# application.yml
cors:
  allowed-origins: 
    - "http://localhost:3000"
    - "https://yourdomain.com"
  allowed-methods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
```

**❌ Problem:** `Redis connection timeout`  
**✅ Solution:** Verify Redis configuration and increase timeout:

```yaml
spring:
  redis:
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 50
        max-wait: 3000ms
```

**❌ Problem:** `Room not found error`  
**✅ Solution:** Check room expiration settings and database connectivity:

```bash
# Check active rooms
curl -X GET "http://localhost:8080/api/admin/rooms/active" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**❌ Problem:** `JWT token expired`  
**✅ Solution:** Implement token refresh mechanism:

```bash
# Refresh access token
curl -X POST "http://localhost:8080/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "your-refresh-token"}'
```

---

### WebRTC Debugging

```javascript
// Enable WebRTC debug logs
const pc = new RTCPeerConnection({
  iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
});

// Monitor connection state
pc.onconnectionstatechange = () => {
  console.log('Connection state:', pc.connectionState);
};

// Log ICE candidates
pc.onicecandidate = (event) => {
  if (event.candidate) {
    console.log('ICE candidate:', event.candidate);
  }
};
```

---

## 📈 Monitoring

### Built-in Metrics

```bash
# System health
GET /actuator/health

# Application metrics  
GET /actuator/metrics

# Active sessions
GET /api/admin/statistics/sessions

# Room statistics
GET /api/admin/statistics/rooms

# User statistics  
GET /api/admin/statistics/users
```

### Custom Monitoring

```java
@Component
public class SignalServerMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter roomCreations;
    private final Timer connectionSetupTime;
    
    public SignalServerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.roomCreations = Counter.builder("rooms.created")
            .description("Number of rooms created")
            .register(meterRegistry);
            
        this.connectionSetupTime = Timer.builder("webrtc.setup.time")
            .description("WebRTC connection setup time")
            .register(meterRegistry);
    }
    
    public void recordRoomCreation() {
        roomCreations.increment();
    }
    
    public void recordConnectionSetup(Duration duration) {
        connectionSetupTime.record(duration);
    }
}
```

---

## 🔒 Security Features

### Rate Limiting

```java
@RestController
@RateLimited(limit = 100, window = "1h") // 100 requests per hour
public class RoomController {
    
    @PostMapping("/rooms")
    @RateLimited(limit = 10, window = "1m") // 10 room creations per minute
    public ResponseEntity<RoomResponse> createRoom(@RequestBody CreateRoomRequest request) {
        // Implementation
    }
}
```

### Input Validation

```java
@Data
public class CreateRoomRequest {
    
    @NotBlank(message = "Room name is required")
    @Size(min = 3, max = 100, message = "Room name must be 3-100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Min(value = 2, message = "Minimum 2 participants required")
    @Max(value = 50, message = "Maximum 50 participants allowed")
    private int maxParticipants;
}
```

---

## 🚀 Deployment

### Production Configuration

```yaml
spring:
  profiles:
    active: production
  
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      
  jpa:
    hibernate:
      ddl-auto: validate
      
logging:
  level:
    com.example.signalserver: INFO
    org.springframework.web.socket: DEBUG
    
server:
  port: 8080
  servlet:
    session:
      timeout: 30m
      
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### Docker Production

```dockerfile
FROM openjdk:17-jdk-slim

COPY target/signal-server-*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 👥 Team

- **Backend Development** - Spring Boot, WebRTC Signaling, Database Design
- **Real-time Communication** - WebSocket, Redis, Session Management  
- **Security & Authentication** - JWT, OAuth2, Rate Limiting
- **DevOps & Deployment** - Docker, CI/CD, Monitoring

---

<p align="center">
  <strong>Built with ❤️ for seamless video communication</strong>
</p> -->