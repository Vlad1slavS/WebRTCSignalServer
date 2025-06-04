package com.example.signalserver.service.auth;

import com.example.signalserver.dto.request.LoginRequest;
import com.example.signalserver.dto.request.RegisterRequest;
import com.example.signalserver.dto.response.AuthResponse;
import com.example.signalserver.dto.response.UserResponse;
import com.example.signalserver.exception.AuthenticationException;
import com.example.signalserver.exception.user.UserNotFoundException;
import com.example.signalserver.exception.ValidationException;
import com.example.signalserver.model.entity.User;
import com.example.signalserver.model.JWT.RefreshToken;
import com.example.signalserver.model.JWT.VerificationToken;
import com.example.signalserver.model.enums.UserStatus;
import com.example.signalserver.repository.user.UserRepository;
import com.example.signalserver.repository.JWT.RefreshTokenRepository;
import com.example.signalserver.repository.JWT.VerificationTokenRepository;
import com.example.signalserver.security.CustomUserDetails;
import com.example.signalserver.service.user.UserService;
import com.example.signalserver.validate.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    // Паттерн для валидации username
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$");

    @Value("${app.jwt.expiration:86400}")
    private int jwtExpiration;

    @Value("${app.refresh-token.expiration:604800}")
    private long refreshTokenDuration;

    @Value("${app.verification.expiration:86400}")
    private long verificationTokenDuration;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    /**
     * Регистрация нового пользователя
     */
    public void register(RegisterRequest request, String clientIp, String userAgent) {
        logger.info("Registration attempt for username: {}, email: {}",
                request.getUsername(), request.getEmail());

        // Валидация входных данных
        validateRegistrationRequest(request);

        // Проверка уникальности username и email
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email is already in use!");
        }

        try {
            // Создание пользователя
            User user = createUserFromRequest(request);
            user = userRepository.save(user);

            logger.info("User created successfully: id={}, username={}",
                    user.getId(), user.getUsername());

            // Создание токена верификации email
            if (shouldSendVerificationEmail()) {
                createAndSendVerificationToken(user);
            }

            // Логирование регистрации
            logUserAction(user.getId(), "REGISTRATION", clientIp, userAgent);

        } catch (Exception e) {
            logger.error("Registration failed for username: {}", request.getUsername(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Аутентификация пользователя
     */
    public AuthResponse login(LoginRequest request, String clientIp, String userAgent) {
        logger.info("Login attempt for: {}", request.getUsernameOrEmail());

        try {
            // Поиск пользователя по username или email
            User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail())
                    .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

            // Проверка статуса пользователя
            validateUserForLogin(user);

            // Аутентификация
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Генерация токенов
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String accessToken = jwtService.generateToken(userDetails);
            RefreshToken refreshToken = createRefreshToken(user, clientIp, userAgent);

            // Обновление информации о входе
            updateUserLoginInfo(user, clientIp, userAgent);

            // Логирование успешного входа
            logUserAction(user.getId(), "LOGIN_SUCCESS", clientIp, userAgent);

            logger.info("User logged in successfully: id={}, username={}",
                    user.getId(), user.getUsername());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration)
                    .user(UserResponse.fromUser(user))
                    .build();

        } catch (BadCredentialsException e) {
            logger.warn("Login failed - invalid credentials for: {}", request.getUsernameOrEmail());
            logFailedLoginAttempt(request.getUsernameOrEmail(), clientIp, userAgent);
            throw new AuthenticationException("Invalid credentials");
        } catch (Exception e) {
            logger.error("Login failed for: {}", request.getUsernameOrEmail(), e);
            throw new AuthenticationException("Login failed: " + e.getMessage());
        }
    }

    /**
     * Обновление токена доступа
     */
    public AuthResponse refreshToken(String refreshTokenValue, String clientIp) {
        logger.debug("Token refresh attempt from IP: {}", clientIp);

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new AuthenticationException("Refresh token not found"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthenticationException("Refresh token has expired");
        }

        try {
            User user = refreshToken.getUser();
            validateUserForLogin(user);

            // Генерация нового access token
            CustomUserDetails userDetails = new CustomUserDetails(user);
            String newAccessToken = jwtService.generateToken(userDetails);

            // Опционально: обновление refresh token
            refreshToken.updateExpiryDate(refreshTokenDuration);
            refreshToken = refreshTokenRepository.save(refreshToken);

            logger.info("Token refreshed successfully for user: {}", user.getUsername());

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration)
                    .user(UserResponse.fromUser(user))
                    .build();

        } catch (Exception e) {
            logger.error("Token refresh failed", e);
            throw new AuthenticationException("Token refresh failed: " + e.getMessage());
        }
    }

    /**
     * Выход из системы
     */
    public void logout(String accessToken) {
        try {
            String username = getCurrentUsername();
            logger.info("Logout attempt for user: {}", username);

            // Удаление refresh tokens пользователя
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Удаление всех refresh токенов пользователя
                refreshTokenRepository.deleteByUser(user);

                // Обновление статуса пользователя
                userService.updateUserOnlineStatus(user.getId(), false);
                // Логирование
                logUserAction(user.getId(), "LOGOUT", null, null);
            }

            // Очистка Security Context
            SecurityContextHolder.clearContext();

            logger.info("User logged out successfully: {}", username);

        } catch (Exception e) {
            logger.error("Logout failed", e);
            throw new RuntimeException("Logout failed: " + e.getMessage());
        }
    }

    /**
     * Верификация email
     */
    public void verifyEmail(String tokenValue) {
        logger.info("Email verification attempt with token: {}", tokenValue);

        VerificationToken token = verificationTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ValidationException("Invalid verification token"));

        if (token.isExpired()) {
            verificationTokenRepository.delete(token);
            throw new ValidationException("Verification token has expired");
        }

        try {
            User user = token.getUser();
            user.setEmailVerified(true);
            user.setStatus(UserStatus.ONLINE);
            userRepository.save(user);

            verificationTokenRepository.delete(token);

            logger.info("Email verified successfully for user: {}", user.getUsername());


        } catch (Exception e) {
            logger.error("Email verification failed", e);
            throw new RuntimeException("Email verification failed: " + e.getMessage());
        }
    }

    /**
     * Повторная отправка письма с верификацией
     */
    public void resendVerificationEmail(String email) {
        logger.info("Resend verification email request for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            throw new ValidationException("Email is already verified");
        }

        try {
            // Удаление старого токена
            verificationTokenRepository.deleteByUser(user);

            // Создание нового токена
            createAndSendVerificationToken(user);

            logger.info("Verification email resent for user: {}", user.getUsername());

        } catch (Exception e) {
            logger.error("Failed to resend verification email", e);
            throw new RuntimeException("Failed to resend verification email: " + e.getMessage());
        }
    }

    /**
     * Запрос на сброс пароля
     */
    public void forgotPassword(String email) {
        logger.info("Password reset request for email: {}", email);

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Не раскрываем информацию о существовании email
            logger.warn("Password reset request for non-existent email: {}", email);
            return;
        }

        try {
            User user = userOpt.get();

            // Создание токена сброса пароля
            String resetToken = generateSecureToken();
            VerificationToken token = createPasswordResetToken(user, resetToken);

            logger.info("Password reset email sent for user: {}", user.getUsername());

        } catch (Exception e) {
            logger.error("Failed to process password reset request", e);
            throw new RuntimeException("Failed to process password reset request");
        }
    }

    /**
     * Сброс пароля
     */
    public void resetPassword(String tokenValue, String newPassword) {
        logger.info("Password reset attempt with token: {}", tokenValue);

        VerificationToken token = verificationTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ValidationException("Invalid reset token"));

        if (token.isExpired()) {
            verificationTokenRepository.delete(token);
            throw new ValidationException("Reset token has expired");
        }

        try {
            validatePassword(newPassword);

            User user = token.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Удаление всех refresh токенов (принудительный logout)
            refreshTokenRepository.deleteByUser(user);

            verificationTokenRepository.delete(token);

            logger.info("Password reset successfully for user: {}", user.getUsername());

            // Логирование
            logUserAction(user.getId(), "PASSWORD_RESET", null, null);

        } catch (Exception e) {
            logger.error("Password reset failed", e);
            throw new RuntimeException("Password reset failed: " + e.getMessage());
        }
    }

    /**
     * Изменение пароля (для аутентифицированного пользователя)
     */
    public void changePassword(String username, String currentPassword, String newPassword) {
        logger.info("Password change request for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Проверка текущего пароля
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        try {
            validatePassword(newPassword);

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Удаление всех refresh токенов кроме текущего
            // (опционально - можно оставить текущую сессию)

            logger.info("Password changed successfully for user: {}", username);

            // Логирование
            logUserAction(user.getId(), "PASSWORD_CHANGE", null, null);

        } catch (Exception e) {
            logger.error("Password change failed for user: {}", username, e);
            throw new RuntimeException("Password change failed: " + e.getMessage());
        }
    }

    /**
     * Получение текущего пользователя
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return authentication.getName();
        }
        throw new AuthenticationException("No authenticated user found");
    }

    public User getCurrentUser() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
    }

    // Приватные методы

    private void validateRegistrationRequest(RegisterRequest request) {
        // Валидация username
        if (!StringUtils.hasText(request.getUsername()) ||
                !USERNAME_PATTERN.matcher(request.getUsername()).matches()) {
            throw new ValidationException("Username must be 3-20 characters long and contain only letters, numbers, and underscores");
        }

        // Валидация email
        if (!StringUtils.hasText(request.getEmail()) ||
                !EmailValidator.isValid(request.getEmail())) {
            throw new ValidationException("Invalid email format");
        }

        // Валидация пароля
        validatePassword(request.getPassword());

        // Валидация имени
        if (!StringUtils.hasText(request.getFirstName()) || request.getFirstName().length() > 50) {
            throw new ValidationException("First name is required and must be less than 50 characters");
        }

        if (!StringUtils.hasText(request.getLastName()) || request.getLastName().length() > 50) {
            throw new ValidationException("Last name is required and must be less than 50 characters");
        }
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new ValidationException("Password is required");
        }

        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Password must contain at least one uppercase letter, one lowercase letter, and one number");
        }
    }

    private void validateUserForLogin(User user) {
        if (user.getStatus() == UserStatus.BANNED) {
            throw new AuthenticationException("Your account has been banned");
        }

        if (user.getStatus() == UserStatus.DELETED) {
            throw new AuthenticationException("Account not found");
        }

        // Опционально: проверка верификации email
        // if (!user.isEmailVerified()) {
        //     throw new AuthenticationException("Please verify your email before logging in");
        // }
    }

    private User createUserFromRequest(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setStatus(UserStatus.ONLINE); // или PENDING если требуется верификация email
        user.setEmailVerified(!shouldSendVerificationEmail()); // true если не требуется верификация
        user.setOnline(false);
        return user;
    }

    private RefreshToken createRefreshToken(User user, String clientIp, String userAgent) {
        // Удаление старых токенов для этого пользователя (опционально)
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(generateSecureToken())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenDuration))
                .clientIp(clientIp)
                .userAgent(userAgent)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    private void createAndSendVerificationToken(User user) {
        String tokenValue = generateSecureToken();

        VerificationToken token = VerificationToken.builder()
                .user(user)
                .token(tokenValue)
                .type(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .expiryDate(LocalDateTime.now().plusSeconds(verificationTokenDuration))
                .build();

        verificationTokenRepository.save(token);
    }

    private VerificationToken createPasswordResetToken(User user, String tokenValue) {
        // Удаление старых токенов сброса пароля
        verificationTokenRepository.deleteByUserAndType(user, VerificationToken.TokenType.PASSWORD_RESET);

        VerificationToken token = VerificationToken.builder()
                .user(user)
                .token(tokenValue)
                .type(VerificationToken.TokenType.PASSWORD_RESET)
                .expiryDate(LocalDateTime.now().plusMinutes(30)) // 30 минут на сброс пароля
                .build();

        return verificationTokenRepository.save(token);
    }

    private void updateUserLoginInfo(User user, String clientIp, String userAgent) {
        user.setOnline(true);
        user.setLastSeenAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private void logUserAction(Long userId, String action, String clientIp, String userAgent) {
        try {
            // Здесь можно добавить логирование в базу данных или отправку в аналитику
            logger.info("User action: userId={}, action={}, ip={}", userId, action, clientIp);
        } catch (Exception e) {
            logger.warn("Failed to log user action", e);
        }
    }

    private void logFailedLoginAttempt(String usernameOrEmail, String clientIp, String userAgent) {
        try {
            logger.warn("Failed login attempt: username/email={}, ip={}", usernameOrEmail, clientIp);
            // Здесь можно добавить защиту от брутфорс атак
        } catch (Exception e) {
            logger.warn("Failed to log failed login attempt", e);
        }
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private boolean shouldSendVerificationEmail() {
        // Можно сделать это настраиваемым через application.yml
        return true;
    }
}
