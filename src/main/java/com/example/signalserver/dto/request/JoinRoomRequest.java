package com.example.signalserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinRoomRequest {

    @NotBlank(message = "Room code is required")
    private String roomCode;

    private String password;

    private String displayName;

    private MediaSettings mediaSettings;

    private String userAgent;

    private DeviceInfo deviceInfo;

    @Data
    public static class MediaSettings {
        private boolean videoEnabled = true;
        private boolean audioEnabled = true;
        private String videoQuality = "HD"; // HD, FHD, SD
        private String audioQuality = "HIGH"; // HIGH, MEDIUM, LOW
        private boolean echoCancellation = true;
        private boolean noiseSuppression = true;
    }

    @Data
    public static class DeviceInfo {
        private String deviceType; // desktop, mobile, tablet
        private String browser;
        private String os;
        private String browserVersion;
        private ScreenResolution screenResolution;
    }

    @Data
    public static class ScreenResolution {
        private int width;
        private int height;
    }
}
