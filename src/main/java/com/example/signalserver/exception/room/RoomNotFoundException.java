package com.example.signalserver.exception.room;


public class RoomNotFoundException extends RuntimeException {

    private Long roomId;
    private String roomCode;

    public RoomNotFoundException(String roomCode) {
        super("Room not found with code: " + roomCode);
        this.roomCode = roomCode;
    }

    public RoomNotFoundException(Long roomId) {
        super("Room not found with ID: " + roomId);
        this.roomId = roomId;
    }

    public RoomNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static RoomNotFoundException byCode(String roomCode) {
        return new RoomNotFoundException(roomCode);
    }

    public static RoomNotFoundException byId(Long roomId) {
        return new RoomNotFoundException(roomId);
    }

    // Getters
    public Long getRoomId() { return roomId; }
    public String getRoomCode() { return roomCode; }
}

