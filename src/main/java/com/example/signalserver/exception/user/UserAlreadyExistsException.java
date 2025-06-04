package com.example.signalserver.exception.user;

public class UserAlreadyExistsException extends RuntimeException {

    private String conflictField;
    private String conflictValue;

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String conflictField, String conflictValue) {
        super("User already exists with " + conflictField + ": " + conflictValue);
        this.conflictField = conflictField;
        this.conflictValue = conflictValue;
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public static UserAlreadyExistsException withUsername(String username) {
        return new UserAlreadyExistsException("username", username);
    }

    public static UserAlreadyExistsException withEmail(String email) {
        return new UserAlreadyExistsException("email", email);
    }

    // Getters
    public String getConflictField() { return conflictField; }
    public String getConflictValue() { return conflictValue; }
}
