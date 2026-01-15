package com.streamflix.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * Event emitted when content is added, updated, or removed.
 *
 * Consumers:
 * - Search Service: Index updates
 * - Recommendation Service: Catalog refresh
 * - CDN: Cache invalidation signals
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentEvent extends DomainEvent {

    public static final String TOPIC = "streamflix.content.events";

    private String contentId;
    private String title;
    private ContentEventType contentEventType;
    private String contentCategory;  // MOVIE, SERIES, EPISODE
    private List<String> genres;
    private List<String> tags;
    private Integer releaseYear;
    private String maturityRating;
    private Map<String, Object> metadata;

    @Override
    public String getTopic() {
        return TOPIC;
    }

    @Override
    public String getPartitionKey() {
        return contentId;
    }

    public enum ContentEventType {
        CONTENT_ADDED,
        CONTENT_UPDATED,
        CONTENT_DELETED,
        CONTENT_PUBLISHED,
        CONTENT_UNPUBLISHED
    }
}
