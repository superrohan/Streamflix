package com.streamflix.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event emitted during video playback operations.
 *
 * This event drives:
 * - Watch history updates
 * - Resume playback position
 * - Recommendation engine signals
 * - Analytics aggregation
 * - Trending calculations
 *
 * Partition Strategy:
 * - Partitioned by userId to ensure per-user ordering
 * - This guarantees that a user's play/pause/stop events are processed in order
 *
 * Idempotency:
 * - Consumers should use eventId for deduplication
 * - Position updates are last-write-wins within a time window
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoPlaybackEvent extends DomainEvent {

    public static final String TOPIC = "streamflix.playback.events";

    /**
     * User who triggered the event.
     */
    private String userId;

    /**
     * Profile within the user account.
     */
    private String profileId;

    /**
     * Content being played.
     */
    private String contentId;

    /**
     * Type of content: MOVIE, EPISODE, TRAILER
     */
    private ContentType contentType;

    /**
     * For episodes, the series ID.
     */
    private String seriesId;

    /**
     * Current playback position in seconds.
     */
    private Long positionSeconds;

    /**
     * Total duration of the content in seconds.
     */
    private Long durationSeconds;

    /**
     * Calculated watch percentage (0-100).
     */
    private Integer watchPercentage;

    /**
     * Device information for multi-device support.
     */
    private DeviceInfo deviceInfo;

    /**
     * Quality level being streamed.
     */
    private String qualityLevel;

    /**
     * Audio track language code.
     */
    private String audioTrack;

    /**
     * Subtitle language code (null if disabled).
     */
    private String subtitleTrack;

    /**
     * Session identifier for this viewing session.
     */
    private String sessionId;

    @Override
    public String getTopic() {
        return TOPIC;
    }

    @Override
    public String getPartitionKey() {
        // Partition by user to ensure per-user ordering
        return userId;
    }

    /**
     * Check if the user has substantially watched the content.
     * Used for "Continue Watching" logic.
     */
    public boolean isSubstantialWatch() {
        return watchPercentage != null && watchPercentage >= 5;
    }

    /**
     * Check if the content is considered completed.
     * Netflix typically uses 90% threshold.
     */
    public boolean isCompleted() {
        return watchPercentage != null && watchPercentage >= 90;
    }

    public enum ContentType {
        MOVIE,
        EPISODE,
        TRAILER,
        PREVIEW,
        EXTRA
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class DeviceInfo {
        private String deviceId;
        private String deviceType;  // TV, MOBILE, TABLET, WEB, CONSOLE
        private String deviceModel;
        private String osName;
        private String osVersion;
        private String appVersion;
        private String ipAddress;
        private String country;
        private String region;
    }
}
