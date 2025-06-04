package com.example.signalserver.service.user;

import com.example.signalserver.dto.request.RegisterRequest;
import com.example.signalserver.dto.response.UserProfile;
import com.example.signalserver.exception.user.UserAlreadyExistsException;
import com.example.signalserver.exception.user.UserNotFoundException;
import com.example.signalserver.model.entity.User;
import com.example.signalserver.model.enums.UserStatus;
import com.example.signalserver.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String USER_CACHE_PREFIX = "user:";
    private static final String ONLINE_USERS_KEY = "online_users";

    public User createUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setStatus(UserStatus.OFFLINE);

        user = userRepository.save(user);


        return user;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Обновление онлайн статуса пользователя
     */
    public void updateUserOnlineStatus(Long userId, boolean online) {
        try {
            LocalDateTime now = LocalDateTime.now();
            // Используем оптимизированный запрос из репозитория
            userRepository.updateUserOnlineStatus(userId, online, now);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user online status", e);
        }
    }

    public void setUserOnline(Long userId) {
        User user = findById(userId);
        user.setOnline(true);
        user.setStatus(UserStatus.ONLINE);
        user.setLastSeenAt(LocalDateTime.now());
        userRepository.save(user);

        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId.toString());
        cacheUser(user);
    }

    public void setUserOffline(Long userId) {
        User user = findById(userId);
        user.setOnline(false);
        user.setStatus(UserStatus.OFFLINE);
        user.setLastSeenAt(LocalDateTime.now());
        userRepository.save(user);

        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId.toString());
        cacheUser(user);
    }

    public void banUser(Long userId) {
        User user = findById(userId);
        user.setStatus(UserStatus.OFFLINE);
        userRepository.save(user);

    }

    private void cacheUser(User user) {
        redisTemplate.opsForValue().set(
                USER_CACHE_PREFIX + user.getId(),
                user,
                Duration.ofHours(1)
        );
    }

    public int getTotalUsersCount() {
        return (int) userRepository.count();
    }

    public UserProfile getUserProfile(Long userId) {
        User user = findById(userId);

        UserProfile profile = new UserProfile();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setEmail(user.getEmail());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setAvatarUrl(user.getAvatarUrl());
        profile.setOnline(user.isOnline());
        profile.setStatus(user.getStatus());
        profile.setLastSeenAt(user.getLastSeenAt());

        return profile;
    }

    public User updateLastActivity(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setLastSeenAt(LocalDateTime.now());
        return user;
    }
}
