package com.example.signalserver.model.entity;

import com.example.signalserver.model.enums.EventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "room_events")
@EntityListeners(AuditingEntityListener.class)
public class RoomEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private String eventData;

    @CreatedDate
    private LocalDateTime timestamp;

    // Constructors
    public RoomEvent() {
        // No-arg constructor required by JPA
    }

    public RoomEvent(Room room, User user, EventType eventType) {
        this.room = room;
        this.user = user;
        this.eventType = eventType;
    }
}