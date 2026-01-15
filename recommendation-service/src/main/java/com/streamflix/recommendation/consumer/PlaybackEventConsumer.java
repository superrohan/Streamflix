package com.streamflix.recommendation.consumer;

import com.streamflix.common.event.VideoPlaybackEvent;
import com.streamflix.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for playback events.
 *
 * Processes viewing events to:
 * - Update profile preferences
 * - Update content popularity scores
 * - Trigger recommendation recalculation
 *
 * Idempotency: Uses eventId for deduplication.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaybackEventConsumer {

    private final RecommendationService recommendationService;

    @KafkaListener(
        topics = "streamflix.playback.events",
        groupId = "recommendation-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePlaybackEvent(VideoPlaybackEvent event, Acknowledgment ack) {
        try {
            log.debug("Received playback event: {} for content {} by profile {}",
                    event.getEventType(), event.getContentId(), event.getProfileId());

            // Process based on event type
            switch (event.getEventType()) {
                case "VIDEO_STARTED":
                    recommendationService.recordViewStart(event);
                    break;
                case "VIDEO_COMPLETED":
                    recommendationService.recordViewCompleted(event);
                    break;
                case "VIDEO_PROGRESS":
                    // Progress updates are batched, only significant ones processed
                    if (event.getWatchPercentage() != null && event.getWatchPercentage() >= 25) {
                        recommendationService.recordSignificantProgress(event);
                    }
                    break;
                default:
                    log.debug("Ignoring event type: {}", event.getEventType());
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing playback event: {}", e.getMessage(), e);
            // Don't acknowledge - will be retried
            throw e;
        }
    }
}
