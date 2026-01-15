package com.streamflix.playback.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks watch progress for resume playback functionality.
 */
@Entity
@Table(name = "watch_progress")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @Column(name = "episode_id")
    private UUID episodeId;

    @Column(name = "position_seconds", nullable = false)
    @Builder.Default
    private Long positionSeconds = 0L;

    @Column(name = "duration_seconds", nullable = false)
    private Long durationSeconds;

    @Column(name = "watch_percentage", nullable = false)
    @Builder.Default
    private Integer watchPercentage = 0;

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "last_watched_at")
    @Builder.Default
    private Instant lastWatchedAt = Instant.now();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public void updateProgress(long position, long duration) {
        this.positionSeconds = position;
        this.durationSeconds = duration;
        this.watchPercentage = duration > 0 ? (int) ((position * 100) / duration) : 0;
        this.lastWatchedAt = Instant.now();
    }

    public void markCompleted() {
        this.isCompleted = true;
        this.lastWatchedAt = Instant.now();
    }
}
