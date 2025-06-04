package com.example.signalserver.controller;

import com.example.signalserver.dto.request.LoginRequest;
import com.example.signalserver.dto.request.RegisterRequest;
import com.example.signalserver.dto.request.RefreshTokenRequest;
import com.example.signalserver.dto.request.ChangePasswordRequest;
import com.example.signalserver.dto.response.AuthResponse;
import com.example.signalserver.dto.response.MessageResponse;
import com.example.signalserver.service.auth.AuthService;
import com.example.signalserver.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Register new user", description = "Create a new user account")
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request,
                                                    HttpServletRequest httpRequest) {
        try {
            String clientIp = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            authService.register(request, clientIp, userAgent);

            return ResponseEntity.ok(new MessageResponse(
                    "User registered successfully! Please check your email for verification.",
                    "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage(), "ERROR"));
        }
    }

    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        try {
            String clientIp = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            AuthResponse response = authService.login(request, clientIp, userAgent);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage(), "ERROR"));
        }
    }

    @Operation(summary = "Refresh JWT token", description = "Get new access token using refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request,
                                          HttpServletRequest httpRequest) {
        try {
            String clientIp = getClientIpAddress(httpRequest);
            AuthResponse response = authService.refreshToken(request.getRefreshToken(), clientIp);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage(), "ERROR"));
        }
    }

    @Operation(summary = "Logout user", description = "Invalidate user session and tokens")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        try {
            String token = extractJwtFromRequest(request);
            authService.logout(token);

            return ResponseEntity.ok(new MessageResponse(
                    "User logged out successfully!",
                    "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage(), "ERROR"));
        }
    }

    @Operation(summary = "Verify email", description = "Verify user email with verification token")
    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            return ResponseEntity.ok(new MessageResponse(
                    "Email verified successfully!",
                    "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage(), "ERROR"));
        }
    }

    @Operation(summary = "Resend verification email", description = "Send new verification email")
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerificationEmail(@RequestParam String email) {
        try {
            authService.resendVerificationEmail(email);
            return ResponseEntity.ok(new MessageResponse(
                    "Verification email sent successfully!",
                    "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage(), "ERROR"));
        }
    }

    @Operation(summary = "Forgot password", description = "Send password reset email")
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestParam String email) {
        try {
            authService.forgotPassword(email);
            return ResponseEntity.ok(new MessageResponse(
                    "Password reset email sent successfully!",
                    "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage(), "ERROR"));
        }
    }

    @Operation(summary = "Reset password", description = "Reset password using reset token")
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestParam String token,
                                                         @RequestParam String newPassword) {
        try {
            authService.resetPassword(token, newPassword);
            return ResponseEntity.ok(new MessageResponse(
                    "Password reset successfully!",
                    "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage(), "ERROR"));
        }
    }

    @Operation(summary = "Change password", description = "Change user password (authenticated)")
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                          HttpServletRequest httpRequest) {
        try {
            String username = getCurrentUsername(httpRequest);
            authService.changePassword(username, request.getCurrentPassword(),
                    request.getNewPassword());

            return ResponseEntity.ok(new MessageResponse(
                    "Password changed successfully!",
                    "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage(), "ERROR"));
        }
    }

    @Operation(summary = "Check if username exists", description = "Validate username availability")
    @GetMapping("/check-username")
    public ResponseEntity<MessageResponse> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(new MessageResponse(
                exists ? "Username already taken" : "Username available",
                exists ? "TAKEN" : "AVAILABLE"
        ));
    }

    @Operation(summary = "Check if email exists", description = "Validate email availability")
    @GetMapping("/check-email")
    public ResponseEntity<MessageResponse> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(new MessageResponse(
                exists ? "Email already registered" : "Email available",
                exists ? "TAKEN" : "AVAILABLE"
        ));
    }

    // Utility methods
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String getCurrentUsername(HttpServletRequest request) {
        // Извлекаем username из JWT токена или Security Context
        return authService.getCurrentUsername();
    }
}
