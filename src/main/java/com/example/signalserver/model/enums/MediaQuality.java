package com.example.signalserver.model.enums;

public enum MediaQuality {
    LOW(240), MEDIUM(480), HD(720), FULL_HD(1080);

    private final int resolution;

    MediaQuality(int resolution) {
        this.resolution = resolution;
    }

    public int getResolution() {
        return resolution;
    }
}
