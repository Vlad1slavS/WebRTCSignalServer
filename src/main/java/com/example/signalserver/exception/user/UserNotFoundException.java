package com.example.signalserver.exception.user;

public class UserNotFoundException extends RuntimeException {

    private Long userId;
    private String username;
    private String email;

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
        this.userId = userId;
    }

    public UserNotFoundException(String identifier, String type) {
        super("User not found with " + type + ": " + identifier);
        if ("username".equals(type)) {
            this.username = identifier;
        } else if ("email".equals(type)) {
            this.email = identifier;
        }
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static UserNotFoundException byId(Long userId) {
        return new UserNotFoundException(userId);
    }

    public static UserNotFoundException byUsername(String username) {
        return new UserNotFoundException(username, "username");
    }

    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException(email, "email");
    }

    // Getters
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}
