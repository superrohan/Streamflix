package com.streamflix.recommendation.service;

import com.streamflix.common.event.VideoPlaybackEvent;
import com.streamflix.recommendation.dto.RecommendationDto;
import com.streamflix.recommendation.entity.TrendingContent;
import com.streamflix.recommendation.entity.ViewingEvent;
import com.streamflix.recommendation.repository.TrendingContentRepository;
import com.streamflix.recommendation.repository.ViewingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Recommendation engine service.
 *
 * Algorithm Overview:
 *
 * 1. TRENDING CONTENT (Global):
 *    - Calculated every 15 minutes
 *    - Based on view counts in last 24 hours
 *    - Weighted by completion rate (completed views count more)
 *    - Time decay to favor recent views
 *
 * 2. PERSONALIZED RECOMMENDATIONS (Per Profile):
 *    - Genre affinity: Weights genres based on watch history
 *    - Collaborative: "Users who watched X also watched Y"
 *    - Content similarity: Same director, cast, themes
 *    - Recency: Boost for unwatched new releases
 *
 * 3. "BECAUSE YOU WATCHED" (Contextual):
 *    - Find similar content to recently watched
 *    - Uses content similarity scores
 *    - Excludes already watched content
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ViewingEventRepository viewingEventRepository;
    private final TrendingContentRepository trendingContentRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TRENDING_CACHE_KEY = "trending:";
    private static final Duration TRENDING_CACHE_TTL = Duration.ofMinutes(15);

    /**
     * Get trending content.
     */
    @Cacheable(value = "trending", key = "#category + '-' + #limit")
    public List<RecommendationDto.TrendingItem> getTrending(String category, int limit) {
        return trendingContentRepository.findByCategory(category, limit).stream()
                .map(this::toTrendingItem)
                .collect(Collectors.toList());
    }

    /**
     * Get personalized recommendations for a profile.
     */
    public List<RecommendationDto.RecommendedItem> getPersonalizedRecommendations(
            UUID profileId, int limit) {

        // In a real implementation, this would:
        // 1. Look up profile preferences
        // 2. Query content matching preferences
        // 3. Apply collaborative filtering
        // 4. Score and rank results

        // Simplified: Return popular content for now
        return trendingContentRepository.findByCategory("OVERALL", limit).stream()
                .map(tc -> RecommendationDto.RecommendedItem.builder()
                        .contentId(tc.getContentId().toString())
                        .score(tc.getScore().doubleValue())
                        .reason("Popular right now")
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get "Because you watched X" recommendations.
     */
    public List<RecommendationDto.BecauseYouWatched> getBecauseYouWatched(
            UUID profileId, int limit) {

        // Get recently completed content for this profile
        List<ViewingEvent> recentCompleted = viewingEventRepository
                .findRecentCompletedByProfile(profileId, 5);

        // For each, find similar content (simplified)
        return recentCompleted.stream()
                .limit(3)
                .map(event -> RecommendationDto.BecauseYouWatched.builder()
                        .sourceContentId(event.getContentId().toString())
                        .recommendations(List.of()) // Would populate with similar content
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Record a view start event.
     */
    @Transactional
    public void recordViewStart(VideoPlaybackEvent event) {
        saveViewingEvent(event, "VIDEO_STARTED");
    }

    /**
     * Record a view completion event.
     */
    @Transactional
    public void recordViewCompleted(VideoPlaybackEvent event) {
        saveViewingEvent(event, "VIDEO_COMPLETED");
        // Increment popularity score
        updateContentPopularity(event.getContentId(), 1.0);
    }

    /**
     * Record significant progress (>25% watched).
     */
    @Transactional
    public void recordSignificantProgress(VideoPlaybackEvent event) {
        saveViewingEvent(event, "VIDEO_PROGRESS");
        // Partial weight for popularity
        double weight = event.getWatchPercentage() / 100.0;
        updateContentPopularity(event.getContentId(), weight);
    }

    /**
     * Scheduled task to recalculate trending content.
     */
    @Scheduled(fixedRateString = "${recommendation.trending.refresh-interval-minutes:15}000")
    @Transactional
    public void recalculateTrending() {
        log.info("Recalculating trending content...");

        Instant windowStart = Instant.now().minus(Duration.ofHours(24));

        // Get view counts per content in last 24 hours
        List<Object[]> viewCounts = viewingEventRepository.getViewCountsByContent(windowStart);

        // Clear existing trending
        trendingContentRepository.deleteByCategory("OVERALL");

        // Insert new trending
        int rank = 1;
        for (Object[] row : viewCounts) {
            UUID contentId = (UUID) row[0];
            Long viewCount = (Long) row[1];
            Double avgCompletion = row[2] != null ? (Double) row[2] : 0.0;

            // Score = views * (1 + completion_rate)
            double score = viewCount * (1 + avgCompletion / 100.0);

            TrendingContent trending = TrendingContent.builder()
                    .contentId(contentId)
                    .rank(rank++)
                    .category("OVERALL")
                    .score(java.math.BigDecimal.valueOf(score))
                    .viewCount24h(viewCount)
                    .calculatedAt(Instant.now())
                    .build();

            trendingContentRepository.save(trending);

            if (rank > 50) break;  // Top 50 only
        }

        log.info("Trending recalculation complete. {} items.", rank - 1);
    }

    private void saveViewingEvent(VideoPlaybackEvent event, String eventType) {
        // Check for duplicate (idempotency)
        if (viewingEventRepository.existsByEventId(event.getEventId())) {
            log.debug("Duplicate event ignored: {}", event.getEventId());
            return;
        }

        ViewingEvent viewingEvent = ViewingEvent.builder()
                .eventId(event.getEventId())
                .profileId(UUID.fromString(event.getProfileId()))
                .contentId(UUID.fromString(event.getContentId()))
                .eventType(eventType)
                .watchPercentage(event.getWatchPercentage())
                .eventTimestamp(event.getTimestamp())
                .build();

        viewingEventRepository.save(viewingEvent);
    }

    private void updateContentPopularity(String contentId, double weight) {
        // Update in Redis for real-time aggregation
        String key = "popularity:" + contentId;
        redisTemplate.opsForValue().increment(key, weight);
        redisTemplate.expire(key, Duration.ofHours(25));
    }

    private RecommendationDto.TrendingItem toTrendingItem(TrendingContent tc) {
        return RecommendationDto.TrendingItem.builder()
                .contentId(tc.getContentId().toString())
                .rank(tc.getRank())
                .viewCount24h(tc.getViewCount24h())
                .build();
    }
}
