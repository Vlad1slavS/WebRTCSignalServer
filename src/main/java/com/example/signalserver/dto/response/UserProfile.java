package com.example.signalserver.dto.response;

import com.example.signalserver.model.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfile {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private boolean online;
    private UserStatus status;
    private LocalDateTime lastSeenAt;
}
