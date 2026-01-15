package com.streamflix.playback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class PlaybackDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartRequest {
        @NotBlank
        private String contentId;
        private String episodeId;
        @NotNull
        private Long durationSeconds;
        private String deviceId;
        private String deviceType;
        private String qualityLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartResponse {
        private String sessionId;
        private Long resumePositionSeconds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressUpdate {
        @NotBlank
        private String contentId;
        private String episodeId;
        @NotNull
        private Long positionSeconds;
        @NotNull
        private Long durationSeconds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContinueWatching {
        private String contentId;
        private String episodeId;
        private Long positionSeconds;
        private Long durationSeconds;
        private Integer watchPercentage;
        private Instant lastWatchedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WatchHistoryItem {
        private String contentId;
        private String episodeId;
        private Instant watchedAt;
        private Boolean isCompleted;
    }
}
