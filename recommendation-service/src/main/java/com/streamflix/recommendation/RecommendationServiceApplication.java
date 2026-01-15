package com.streamflix.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Streamflix Recommendation Service.
 *
 * Responsibilities:
 * - Generate personalized recommendations per profile
 * - Calculate trending content based on viewing patterns
 * - Consume playback events from Kafka
 * - Provide "Because you watched X" suggestions
 *
 * Algorithm Overview:
 * - Collaborative filtering based on viewing history
 * - Content-based filtering using genre preferences
 * - Popularity weighting for new content discovery
 * - Time decay for recency bias
 *
 * @author Streamflix Engineering
 */
@SpringBootApplication
@EnableScheduling
public class RecommendationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecommendationServiceApplication.class, args);
    }
}
