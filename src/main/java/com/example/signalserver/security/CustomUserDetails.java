package com.example.signalserver.security;

import com.example.signalserver.model.entity.User;
import com.example.signalserver.model.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Простая реализация - всем пользователям только роль USER
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // пока не используем логику истечения аккаунта
    }

    @Override
    public boolean isAccountNonLocked() {
        // Аккаунт заблокирован только если статус BANNED
        return user.getStatus() != UserStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Пользователь активен если статус ACTIVE и email подтвержден
        return user.getStatus() == UserStatus.ONLINE && user.isEmailVerified();
    }

    // Дополнительные методы для доступа к данным пользователя
    public Long getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getFirstName() {
        return user.getFirstName();
    }

    public String getLastName() {
        return user.getLastName();
    }

    public String getFullName() {
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    public String getAvatarUrl() {
        return user.getAvatarUrl();
    }

    public boolean isOnline() {
        return user.isOnline();
    }

    public UserStatus getStatus() {
        return user.getStatus();
    }

    public LocalDateTime getLastSeenAt() {
        return user.getLastSeenAt();
    }

    public User getUser() {
        return user;
    }
}
