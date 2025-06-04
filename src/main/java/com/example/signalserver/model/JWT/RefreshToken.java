package com.example.signalserver.model.JWT;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.example.signalserver.model.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    private String clientIp;
    private String userAgent;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    public void updateExpiryDate(long durationInSeconds) {
        this.expiryDate = LocalDateTime.now().plusSeconds(durationInSeconds);
    }
}
