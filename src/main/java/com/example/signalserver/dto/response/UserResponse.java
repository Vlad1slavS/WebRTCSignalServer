package com.example.signalserver.dto.response;

import com.example.signalserver.model.entity.User;
import com.example.signalserver.model.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String avatarUrl;
    private UserStatus status;
    private boolean online;
    private boolean emailVerified;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSeenAt;

    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .online(user.isOnline())
                .emailVerified(user.isEmailVerified())
                .lastSeenAt(user.getLastSeenAt())
                .build();
    }
}
