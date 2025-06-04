package com.example.signalserver.dto.request;

import lombok.Data;

@Data
public class MediaSettingsUpdate {

    private String sessionId;

    private VideoSettings video;

    private AudioSettings audio;

    private ScreenShareSettings screenShare;

    @Data
    public static class VideoSettings {
        private boolean enabled;
        private String quality; // HD, FHD, SD, MOBILE
        private int width;
        private int height;
        private int frameRate;
        private String codec; // VP8, VP9, H264
        private boolean facingMode; // true for front camera, false for back
    }

    @Data
    public static class AudioSettings {
        private boolean enabled;
        private String quality; // HIGH, MEDIUM, LOW
        private boolean echoCancellation;
        private boolean noiseSuppression;
        private boolean autoGainControl;
        private int sampleRate;
        private String codec; // OPUS, G.711
        private double volume = 1.0;
    }

    @Data
    public static class ScreenShareSettings {
        private boolean enabled;
        private String type; // screen, window, tab
        private boolean includeAudio;
        private int maxFrameRate = 30;
        private String quality = "HD";
    }
}
