package com.streamflix.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events in the Streamflix platform.
 *
 * Events are the backbone of our event-driven architecture, enabling:
 * - Loose coupling between services
 * - Reliable async communication via Kafka
 * - Event sourcing capabilities
 * - Audit trail for all state changes
 *
 * Each event contains:
 * - eventId: Unique identifier for idempotency checks
 * - eventType: Discriminator for event routing
 * - timestamp: When the event occurred
 * - correlationId: For distributed tracing
 * - causationId: Links to the event that caused this event
 * - version: Schema version for evolution
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "eventType",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = VideoPlaybackEvent.class, name = "VIDEO_STARTED"),
    @JsonSubTypes.Type(value = VideoPlaybackEvent.class, name = "VIDEO_PAUSED"),
    @JsonSubTypes.Type(value = VideoPlaybackEvent.class, name = "VIDEO_RESUMED"),
    @JsonSubTypes.Type(value = VideoPlaybackEvent.class, name = "VIDEO_COMPLETED"),
    @JsonSubTypes.Type(value = VideoPlaybackEvent.class, name = "VIDEO_PROGRESS"),
    @JsonSubTypes.Type(value = UserEvent.class, name = "USER_REGISTERED"),
    @JsonSubTypes.Type(value = UserEvent.class, name = "USER_LOGIN"),
    @JsonSubTypes.Type(value = UserEvent.class, name = "PROFILE_CREATED"),
    @JsonSubTypes.Type(value = ContentEvent.class, name = "CONTENT_ADDED"),
    @JsonSubTypes.Type(value = ContentEvent.class, name = "CONTENT_UPDATED"),
    @JsonSubTypes.Type(value = SearchEvent.class, name = "SEARCH_PERFORMED")
})
public abstract class DomainEvent {

    /**
     * Unique identifier for this event instance.
     * Used for idempotency checks in consumers.
     */
    private String eventId;

    /**
     * Type discriminator for event routing and deserialization.
     */
    private String eventType;

    /**
     * When the event occurred (not when it was processed).
     */
    private Instant timestamp;

    /**
     * Correlation ID for distributed tracing.
     * All events in a single user action share this ID.
     */
    private String correlationId;

    /**
     * ID of the event that caused this event.
     * Enables causation chain reconstruction.
     */
    private String causationId;

    /**
     * Schema version for event evolution.
     */
    private int version;

    /**
     * Source service that emitted this event.
     */
    private String source;

    /**
     * Initialize event with default values.
     */
    public void initializeDefaults() {
        if (this.eventId == null) {
            this.eventId = UUID.randomUUID().toString();
        }
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
        if (this.version == 0) {
            this.version = 1;
        }
    }

    /**
     * Get the Kafka topic for this event.
     * Override in subclasses for custom routing.
     */
    public abstract String getTopic();

    /**
     * Get the partition key for Kafka.
     * Used to ensure ordering within a partition.
     */
    public abstract String getPartitionKey();
}
