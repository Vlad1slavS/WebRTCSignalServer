package com.example.signalserver.model.entity;

import com.example.signalserver.model.enums.RoomStatus;
import com.example.signalserver.model.enums.RoomType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@EntityListeners(AuditingEntityListener.class)
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String roomCode;

    @NotBlank(message = "Room name is required")
    private String name;

    private String description;

    // ИСПРАВЛЕНИЕ 1: Заменить @Max на @Min для минимума и правильная валидация
    @Min(value = 2, message = "Minimum 2 participants required")
    @Max(value = 50, message = "Maximum 50 participants allowed")
    private Integer maxParticipants = 5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Enumerated(EnumType.STRING)
    private RoomStatus status = RoomStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    private RoomType type = RoomType.PUBLIC;

    private String password; // для приватных комнат

    private boolean recordingEnabled = false;
    private String recordingUrl;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime lastActivityAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CallSession> sessions = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<RoomEvent> events = new ArrayList<>();

    // Constructors
    public Room() {
        // No-arg constructor required by JPA
    }

    public Room(String roomCode, String name, User creator) {
        this.roomCode = roomCode;
        this.name = name;
        this.creator = creator;
    }

    // Getters and Setters (остальные геттеры и сеттеры остаются без изменений)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRecordingEnabled() {
        return recordingEnabled;
    }

    public void setRecordingEnabled(boolean recordingEnabled) {
        this.recordingEnabled = recordingEnabled;
    }

    public String getRecordingUrl() {
        return recordingUrl;
    }

    public void setRecordingUrl(String recordingUrl) {
        this.recordingUrl = recordingUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public List<CallSession> getSessions() {
        return sessions;
    }

    public void setSessions(List<CallSession> sessions) {
        this.sessions = sessions;
    }

    public List<RoomEvent> getEvents() {
        return events;
    }

    public void setEvents(List<RoomEvent> events) {
        this.events = events;
    }

    // Business methods
    public int getCurrentParticipants() {
        return (int) sessions.stream().filter(CallSession::isActive).count();
    }

    public boolean isFull() {
        return getCurrentParticipants() >= maxParticipants;
    }

    // ДОПОЛНИТЕЛЬНЫЕ ПОЛЕЗНЫЕ МЕТОДЫ
    public boolean isActive() {
        return status == RoomStatus.ACTIVE;
    }

    public boolean requiresPassword() {
        return type == RoomType.PRIVATE && password != null && !password.isEmpty();
    }

    public void addSession(CallSession session) {
        sessions.add(session);
        session.setRoom(this);
    }

    public void removeSession(CallSession session) {
        sessions.remove(session);
        session.setRoom(null);
    }

    // ИСПРАВЛЕНИЕ 2: Добавить методы equals и hashCode для правильной работы с коллекциями
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        Room room = (Room) o;
        return id != null && id.equals(room.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", roomCode='" + roomCode + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", maxParticipants=" + maxParticipants +
                '}';
    }
}
