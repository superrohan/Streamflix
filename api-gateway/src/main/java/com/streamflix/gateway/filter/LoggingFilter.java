package com.streamflix.gateway.filter;

import com.streamflix.common.security.SecurityConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Global filter for request/response logging.
 *
 * Logs:
 * - Incoming requests with method, path, and client info
 * - Response status codes and latency
 * - Slow requests (configurable threshold)
 *
 * Security: Sensitive headers are redacted in logs.
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final long SLOW_REQUEST_THRESHOLD_MS = 3000;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant startTime = Instant.now();
        ServerHttpRequest request = exchange.getRequest();

        String correlationId = exchange.getAttribute("correlationId");
        String method = request.getMethod().name();
        String path = request.getPath().value();
        String clientIp = getClientIp(request);

        // Log incoming request
        log.info("[{}] -> {} {} from {}",
                correlationId, method, path, clientIp);

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> logResponse(exchange, startTime, correlationId))
                .doOnError(error -> logError(exchange, startTime, correlationId, error));
    }

    private void logResponse(ServerWebExchange exchange, Instant startTime, String correlationId) {
        Duration duration = Duration.between(startTime, Instant.now());
        long latencyMs = duration.toMillis();
        ServerHttpResponse response = exchange.getResponse();
        HttpStatusCode statusCode = response.getStatusCode();

        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        if (latencyMs > SLOW_REQUEST_THRESHOLD_MS) {
            log.warn("[{}] <- {} {} {} (SLOW: {}ms)",
                    correlationId, method, path,
                    statusCode != null ? statusCode.value() : "?",
                    latencyMs);
        } else {
            log.info("[{}] <- {} {} {} ({}ms)",
                    correlationId, method, path,
                    statusCode != null ? statusCode.value() : "?",
                    latencyMs);
        }

        // Log at DEBUG level for detailed request info
        if (log.isDebugEnabled()) {
            String userId = exchange.getAttribute("userId");
            String profileId = exchange.getAttribute("profileId");
            log.debug("[{}] User: {}, Profile: {}, Route: {}",
                    correlationId,
                    userId != null ? userId : "anonymous",
                    profileId != null ? profileId : "none",
                    exchange.getAttribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR"));
        }
    }

    private void logError(ServerWebExchange exchange, Instant startTime, String correlationId, Throwable error) {
        Duration duration = Duration.between(startTime, Instant.now());
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        log.error("[{}] <- {} {} ERROR ({}ms): {}",
                correlationId, method, path,
                duration.toMillis(),
                error.getMessage());
    }

    private String getClientIp(ServerHttpRequest request) {
        // Check for forwarded headers (when behind load balancer)
        String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first (original client)
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }

        // Fall back to remote address
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    @Override
    public int getOrder() {
        // Run after correlation ID filter
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
