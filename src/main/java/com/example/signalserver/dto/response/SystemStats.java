package com.example.signalserver.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class SystemStats {

    // Общие статистики
    private long totalUsers;
    private long activeUsers;
    private long onlineUsers;
    private long totalRooms;
    private long activeRooms;
    private long totalCallSessions;
    private long activeCallSessions;

    // Статистики за периоды
    private PeriodStats today;
    private PeriodStats thisWeek;
    private PeriodStats thisMonth;

    // Системные метрики
    private SystemMetrics system;

    // Статистики использования
    private UsageStats usage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generatedAt;

    @Data
    @Builder
    public static class PeriodStats {
        private int newUsers;
        private int newRooms;
        private int totalSessions;
        private long totalDurationMinutes;
        private int totalMessages;
        private int peakConcurrentUsers;
        private int peakConcurrentRooms;
    }

    @Data
    @Builder
    public static class SystemMetrics {
        private double cpuUsage;
        private long memoryUsedMB;
        private long memoryTotalMB;
        private double memoryUsagePercent;
        private long diskUsedGB;
        private long diskTotalGB;
        private double diskUsagePercent;
        private int activeConnections;
        private String uptime;
        private String version;
    }

    @Data
    @Builder
    public static class UsageStats {
        private Map<String, Integer> deviceTypes; // desktop, mobile, tablet
        private Map<String, Integer> browsers; // chrome, firefox, safari, etc.
        private Map<String, Integer> countries;
        private Map<String, Integer> videoQualities; // HD, FHD, SD
        private Map<String, Long> popularRoomSizes;
        private Map<String, Double> averageSessionDurations;
        private int totalBandwidthMB;
    }
}
