package com.example.signalserver.dto.response;

import com.example.signalserver.model.enums.RoomStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RoomResponse {

    private Long id;
    private String roomCode;
    private String name;
    private String description;
    private RoomStatus status;
    private boolean isPublic;
    private boolean hasPassword;
    private int maxParticipants;
    private int currentParticipants;

    private UserResponse creator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveAt;

    private RoomSettings settings;
    private List<ParticipantInfo> participants;

    @Data
    @Builder
    public static class RoomSettings {
        private boolean recordingEnabled;
        private boolean chatEnabled;
        private boolean screenSharingEnabled;
        private boolean waitingRoom;
        private boolean muteOnJoin;
        private boolean videoOnJoin;
    }

    @Data
    @Builder
    public static class ParticipantInfo {
        private UserResponse user;
        private boolean isModerator;
        private boolean videoEnabled;
        private boolean audioEnabled;
        private boolean screenSharing;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime joinedAt;
    }
}
