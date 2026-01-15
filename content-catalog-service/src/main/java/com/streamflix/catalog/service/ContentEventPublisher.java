package com.streamflix.catalog.service;

import com.streamflix.catalog.entity.Content;
import com.streamflix.common.event.ContentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Publishes content-related events to Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String SOURCE = "content-catalog-service";

    public void publishContentAdded(Content content) {
        ContentEvent event = buildEvent(content, ContentEvent.ContentEventType.CONTENT_ADDED);
        sendEvent(event);
    }

    public void publishContentUpdated(Content content) {
        ContentEvent event = buildEvent(content, ContentEvent.ContentEventType.CONTENT_UPDATED);
        sendEvent(event);
    }

    public void publishContentPublished(Content content) {
        ContentEvent event = buildEvent(content, ContentEvent.ContentEventType.CONTENT_PUBLISHED);
        sendEvent(event);
    }

    private ContentEvent buildEvent(Content content, ContentEvent.ContentEventType eventType) {
        return ContentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType.name())
                .timestamp(Instant.now())
                .source(SOURCE)
                .contentId(content.getId().toString())
                .title(content.getTitle())
                .contentEventType(eventType)
                .contentCategory(content.getContentType().name())
                .genres(content.getGenres().stream()
                        .map(g -> g.getName())
                        .collect(Collectors.toList()))
                .releaseYear(content.getReleaseYear())
                .maturityRating(content.getMaturityRating().name())
                .version(1)
                .build();
    }

    private void sendEvent(ContentEvent event) {
        kafkaTemplate.send(ContentEvent.TOPIC, event.getContentId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send content event: {}", ex.getMessage());
                    } else {
                        log.debug("Content event sent: {}", event.getEventType());
                    }
                });
    }
}
