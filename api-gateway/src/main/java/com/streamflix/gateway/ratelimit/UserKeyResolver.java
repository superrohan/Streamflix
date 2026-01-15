package com.streamflix.gateway.ratelimit;

import com.streamflix.common.security.SecurityConstants;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Key resolver for rate limiting.
 *
 * Strategy:
 * - Authenticated users: Rate limit by user ID
 * - Anonymous users: Rate limit by IP address
 *
 * This ensures:
 * - Authenticated users get consistent rate limits across devices
 * - Anonymous users are limited per IP (with awareness of proxies)
 * - API abuse is harder when tied to accounts
 */
public class UserKeyResolver implements KeyResolver {

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        // First, try to get user ID from attributes (set by auth filter)
        String userId = exchange.getAttribute("userId");
        if (userId != null && !userId.isEmpty()) {
            return Mono.just("user:" + userId);
        }

        // Fall back to user ID header
        String userIdHeader = exchange.getRequest().getHeaders()
                .getFirst(SecurityConstants.USER_ID_HEADER);
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            return Mono.just("user:" + userIdHeader);
        }

        // Anonymous user - use IP address
        String ip = getClientIp(exchange);
        return Mono.just("ip:" + ip);
    }

    private String getClientIp(ServerWebExchange exchange) {
        // Check for forwarded headers (when behind load balancer/proxy)
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs; take the first (original client)
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }

        // Fall back to remote address
        if (exchange.getRequest().getRemoteAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }
}
