package com.streamflix.playback.service;

import com.streamflix.common.event.VideoPlaybackEvent;
import com.streamflix.playback.dto.PlaybackDto;
import com.streamflix.playback.entity.WatchProgress;
import com.streamflix.playback.repository.WatchProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for playback tracking and watch history.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaybackService {

    private final WatchProgressRepository watchProgressRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${playback.completion-threshold-percent:90}")
    private int completionThreshold;

    /**
     * Start playback - record initial state and emit event.
     */
    @Transactional
    public PlaybackDto.StartResponse startPlayback(UUID profileId, PlaybackDto.StartRequest request) {
        UUID contentId = UUID.fromString(request.getContentId());
        UUID episodeId = request.getEpisodeId() != null ? UUID.fromString(request.getEpisodeId()) : null;

        // Get or create watch progress
        WatchProgress progress = watchProgressRepository
                .findByProfileIdAndContentIdAndEpisodeId(profileId, contentId, episodeId)
                .orElseGet(() -> WatchProgress.builder()
                        .profileId(profileId)
                        .contentId(contentId)
                        .episodeId(episodeId)
                        .durationSeconds(request.getDurationSeconds())
                        .build());

        progress.setLastWatchedAt(Instant.now());
        watchProgressRepository.save(progress);

        // Emit playback started event
        emitPlaybackEvent(profileId, progress, "VIDEO_STARTED", request);

        return PlaybackDto.StartResponse.builder()
                .sessionId(UUID.randomUUID().toString())
                .resumePositionSeconds(progress.getPositionSeconds())
                .build();
    }

    /**
     * Update playback progress.
     */
    @Transactional
    public void updateProgress(UUID profileId, PlaybackDto.ProgressUpdate request) {
        UUID contentId = UUID.fromString(request.getContentId());
        UUID episodeId = request.getEpisodeId() != null ? UUID.fromString(request.getEpisodeId()) : null;

        WatchProgress progress = watchProgressRepository
                .findByProfileIdAndContentIdAndEpisodeId(profileId, contentId, episodeId)
                .orElseGet(() -> WatchProgress.builder()
                        .profileId(profileId)
                        .contentId(contentId)
                        .episodeId(episodeId)
                        .durationSeconds(request.getDurationSeconds())
                        .build());

        progress.updateProgress(request.getPositionSeconds(), request.getDurationSeconds());

        // Check if completed
        if (progress.getWatchPercentage() >= completionThreshold && !progress.getIsCompleted()) {
            progress.markCompleted();
            emitPlaybackEvent(profileId, progress, "VIDEO_COMPLETED", request);
        }

        watchProgressRepository.save(progress);
    }

    /**
     * Get continue watching list for a profile.
     */
    @Transactional(readOnly = true)
    public List<PlaybackDto.ContinueWatching> getContinueWatching(UUID profileId, int limit) {
        return watchProgressRepository.findContinueWatching(profileId, limit).stream()
                .map(this::toContinueWatching)
                .collect(Collectors.toList());
    }

    /**
     * Get watch history for a profile.
     */
    @Transactional(readOnly = true)
    public List<PlaybackDto.WatchHistoryItem> getWatchHistory(UUID profileId, int page, int size) {
        return watchProgressRepository.findWatchHistory(profileId, page * size, size).stream()
                .map(this::toWatchHistoryItem)
                .collect(Collectors.toList());
    }

    /**
     * Get resume position for specific content.
     */
    @Transactional(readOnly = true)
    public Optional<Long> getResumePosition(UUID profileId, UUID contentId, UUID episodeId) {
        return watchProgressRepository
                .findByProfileIdAndContentIdAndEpisodeId(profileId, contentId, episodeId)
                .map(WatchProgress::getPositionSeconds);
    }

    private void emitPlaybackEvent(UUID profileId, WatchProgress progress, String eventType, Object request) {
        VideoPlaybackEvent event = VideoPlaybackEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .timestamp(Instant.now())
                .source("playback-service")
                .profileId(profileId.toString())
                .contentId(progress.getContentId().toString())
                .positionSeconds(progress.getPositionSeconds())
                .durationSeconds(progress.getDurationSeconds())
                .watchPercentage(progress.getWatchPercentage())
                .version(1)
                .build();

        kafkaTemplate.send(VideoPlaybackEvent.TOPIC, profileId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to emit playback event: {}", ex.getMessage());
                    }
                });
    }

    private PlaybackDto.ContinueWatching toContinueWatching(WatchProgress progress) {
        return PlaybackDto.ContinueWatching.builder()
                .contentId(progress.getContentId().toString())
                .episodeId(progress.getEpisodeId() != null ? progress.getEpisodeId().toString() : null)
                .positionSeconds(progress.getPositionSeconds())
                .durationSeconds(progress.getDurationSeconds())
                .watchPercentage(progress.getWatchPercentage())
                .lastWatchedAt(progress.getLastWatchedAt())
                .build();
    }

    private PlaybackDto.WatchHistoryItem toWatchHistoryItem(WatchProgress progress) {
        return PlaybackDto.WatchHistoryItem.builder()
                .contentId(progress.getContentId().toString())
                .episodeId(progress.getEpisodeId() != null ? progress.getEpisodeId().toString() : null)
                .watchedAt(progress.getLastWatchedAt())
                .isCompleted(progress.getIsCompleted())
                .build();
    }
}
