package com.example.signalserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class CreateRoomRequest {

    @NotBlank(message = "Room name is required")
    @Size(min = 3, max = 100, message = "Room name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Min(value = 2, message = "Maximum participants must be at least 2")
    @Max(value = 50, message = "Maximum participants cannot exceed 50")
    private int maxParticipants = 10;

    private boolean isPublic = true;

    private String password;

    private boolean recordingEnabled = false;

    private boolean chatEnabled = true;

    private boolean screenSharingEnabled = true;

    private boolean waitingRoom = false;

    // Room settings
    private RoomSettings settings;

    @Data
    public static class RoomSettings {
        private boolean muteOnJoin = false;
        private boolean videoOnJoin = true;
        private boolean allowParticipantVideo = true;
        private boolean allowParticipantAudio = true;
        private boolean allowParticipantScreenShare = true;
        private boolean allowParticipantChat = true;
        private String backgroundImage;
        private int sessionTimeout = 3600; // в секундах
    }
}
