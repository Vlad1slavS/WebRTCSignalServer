package com.example.signalserver.model.entity;

import com.example.signalserver.model.enums.ConnectionStatus;
import com.example.signalserver.model.enums.MediaQuality;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "call_sessions")
@EntityListeners(AuditingEntityListener.class)
public class CallSession {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(unique = true)
    private String socketId;

    private String peerId;
    private String userAgent;
    private String ipAddress;

    private boolean videoEnabled = true;
    private boolean audioEnabled = true;
    private boolean screenSharing = false;

    @Enumerated(EnumType.STRING)
    private ConnectionStatus connectionStatus = ConnectionStatus.CONNECTING;

    @Enumerated(EnumType.STRING)
    private MediaQuality videoQuality = MediaQuality.HD;

    // Network statistics
    private Integer bandwidth;
    private Integer latency;
    private Double packetLoss;

    @CreatedDate
    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;

    @Column(name = "is_active")
    private boolean active = true;

    // Constructors
    public CallSession() {
        // No-arg constructor required by JPA
    }

    public CallSession(Room room, User user, String socketId) {
        this.room = room;
        this.user = user;
        this.socketId = socketId;
    }

    public Duration getCallDuration() {
        LocalDateTime endTime = leftAt != null ? leftAt : LocalDateTime.now();
        return Duration.between(joinedAt, endTime);
    }
}