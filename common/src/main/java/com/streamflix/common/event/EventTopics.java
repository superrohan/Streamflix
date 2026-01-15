package com.streamflix.common.event;

/**
 * Centralized Kafka topic definitions.
 *
 * Naming Convention: streamflix.{domain}.{event-type}
 *
 * Topic Configuration Recommendations:
 * - playback.events: High throughput, 12+ partitions, 7-day retention
 * - user.events: Medium throughput, 6 partitions, 30-day retention
 * - content.events: Low throughput, 3 partitions, 90-day retention
 * - search.events: High throughput, 6 partitions, 7-day retention
 */
public final class EventTopics {

    private EventTopics() {
        // Prevent instantiation
    }

    // Playback Domain
    public static final String PLAYBACK_EVENTS = "streamflix.playback.events";
    public static final String PLAYBACK_PROGRESS = "streamflix.playback.progress";

    // User Domain
    public static final String USER_EVENTS = "streamflix.user.events";
    public static final String USER_ACTIVITY = "streamflix.user.activity";

    // Content Domain
    public static final String CONTENT_EVENTS = "streamflix.content.events";
    public static final String CONTENT_CATALOG = "streamflix.content.catalog";

    // Search Domain
    public static final String SEARCH_EVENTS = "streamflix.search.events";
    public static final String SEARCH_INDEX = "streamflix.search.index";

    // Analytics Domain
    public static final String ANALYTICS_AGGREGATES = "streamflix.analytics.aggregates";
    public static final String ANALYTICS_REALTIME = "streamflix.analytics.realtime";

    // Recommendation Domain
    public static final String RECOMMENDATION_SIGNALS = "streamflix.recommendation.signals";
    public static final String TRENDING_UPDATES = "streamflix.recommendation.trending";

    // Dead Letter Topics (for failed message processing)
    public static final String DLT_SUFFIX = ".dlt";

    public static String getDeadLetterTopic(String originalTopic) {
        return originalTopic + DLT_SUFFIX;
    }
}
