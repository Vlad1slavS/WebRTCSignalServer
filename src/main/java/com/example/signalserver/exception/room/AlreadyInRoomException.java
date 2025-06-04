package com.example.signalserver.exception.room;

public class AlreadyInRoomException extends RuntimeException {

    private Long userId;
    private String username;
    private String roomCode;
    private String currentRoomCode;

    public AlreadyInRoomException(String username, String roomCode) {
        super("User '" + username + "' is already in room: " + roomCode);
        this.username = username;
        this.roomCode = roomCode;
    }

    public AlreadyInRoomException(Long userId, String currentRoomCode, String targetRoomCode) {
        super(String.format("User %d is already in room '%s', cannot join room '%s'",
                userId, currentRoomCode, targetRoomCode));
        this.userId = userId;
        this.currentRoomCode = currentRoomCode;
        this.roomCode = targetRoomCode;
    }

    public AlreadyInRoomException(String message) {
        super(message);
    }

    public AlreadyInRoomException(String message, Throwable cause) {
        super(message, cause);
    }

    // Getters
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRoomCode() { return roomCode; }
    public String getCurrentRoomCode() { return currentRoomCode; }
}

