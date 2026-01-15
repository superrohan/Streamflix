package com.streamflix.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Event emitted when a user performs a search.
 *
 * Used for:
 * - Search analytics
 * - Query suggestion improvements
 * - Personalization signals
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchEvent extends DomainEvent {

    public static final String TOPIC = "streamflix.search.events";

    private String userId;
    private String profileId;
    private String query;
    private List<String> filters;
    private Integer resultCount;
    private Long responseTimeMs;
    private String selectedContentId;  // If user clicked on a result

    @Override
    public String getTopic() {
        return TOPIC;
    }

    @Override
    public String getPartitionKey() {
        return userId != null ? userId : query;
    }
}
