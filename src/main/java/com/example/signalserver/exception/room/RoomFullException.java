package com.example.signalserver.exception.room;


public class RoomFullException extends RuntimeException {

    private String roomCode;
    private int maxParticipants;
    private int currentParticipants;

    public RoomFullException(String roomCode, int maxParticipants, int currentParticipants) {
        super(String.format("Room '%s' is full. Maximum participants: %d, Current: %d",
                roomCode, maxParticipants, currentParticipants));
        this.roomCode = roomCode;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = currentParticipants;
    }

    public RoomFullException(String message) {
        super(message);
    }

    public RoomFullException(String message, Throwable cause) {
        super(message, cause);
    }

    // Getters
    public String getRoomCode() { return roomCode; }
    public int getMaxParticipants() { return maxParticipants; }
    public int getCurrentParticipants() { return currentParticipants; }
}

