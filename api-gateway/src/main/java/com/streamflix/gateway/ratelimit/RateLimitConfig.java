package com.streamflix.gateway.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Rate limiting configuration.
 *
 * Uses Redis-backed rate limiter with the token bucket algorithm.
 *
 * Token Bucket Algorithm:
 * - replenishRate: Tokens added per second (sustained rate)
 * - burstCapacity: Maximum tokens (allows burst traffic)
 * - requestedTokens: Tokens consumed per request
 *
 * Example: replenishRate=100, burstCapacity=200
 * - Allows 100 requests/second sustained
 * - Can burst to 200 requests momentarily
 * - Bucket refills at 100 tokens/second
 */
@Slf4j
@Configuration
public class RateLimitConfig {

    /**
     * Default rate limiter for general API endpoints.
     */
    @Bean
    @Primary
    public RedisRateLimiter defaultRateLimiter() {
        return new RedisRateLimiter(100, 200, 1);
    }

    /**
     * Strict rate limiter for authentication endpoints.
     * Prevents brute force attacks.
     */
    @Bean
    public RedisRateLimiter authRateLimiter() {
        return new RedisRateLimiter(5, 10, 1);
    }

    /**
     * Relaxed rate limiter for read-heavy endpoints.
     */
    @Bean
    public RedisRateLimiter contentRateLimiter() {
        return new RedisRateLimiter(200, 400, 1);
    }

    /**
     * Rate limiter for search endpoints.
     */
    @Bean
    public RedisRateLimiter searchRateLimiter() {
        return new RedisRateLimiter(50, 100, 1);
    }

    /**
     * Rate limiter for playback events.
     * Higher limit since these are frequent.
     */
    @Bean
    public RedisRateLimiter playbackRateLimiter() {
        return new RedisRateLimiter(300, 500, 1);
    }
}
