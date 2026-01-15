package com.streamflix.gateway.health;

import com.streamflix.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Fallback controller for circuit breaker scenarios.
 *
 * When a circuit breaker is open, requests are routed here
 * instead of failing immediately.
 *
 * Provides:
 * - Graceful degradation messages
 * - Service-specific fallback responses
 * - Metrics for fallback usage
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<Void>>> defaultFallback(ServerWebExchange exchange) {
        String correlationId = exchange.getAttribute("correlationId");
        log.warn("[{}] Default fallback triggered", correlationId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .error(ApiResponse.ErrorInfo.builder()
                        .code("SERVICE_UNAVAILABLE")
                        .message("The service is temporarily unavailable. Please try again in a few moments.")
                        .traceId(correlationId)
                        .build())
                .correlationId(correlationId)
                .timestamp(java.time.Instant.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/auth")
    public Mono<ResponseEntity<ApiResponse<Void>>> authFallback(ServerWebExchange exchange) {
        String correlationId = exchange.getAttribute("correlationId");
        log.warn("[{}] Auth service fallback triggered", correlationId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .error(ApiResponse.ErrorInfo.builder()
                        .code("AUTH_SERVICE_UNAVAILABLE")
                        .message("Authentication service is temporarily unavailable. Please try again shortly.")
                        .traceId(correlationId)
                        .build())
                .correlationId(correlationId)
                .timestamp(java.time.Instant.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/content")
    public Mono<ResponseEntity<ApiResponse<Void>>> contentFallback(ServerWebExchange exchange) {
        String correlationId = exchange.getAttribute("correlationId");
        log.warn("[{}] Content service fallback triggered", correlationId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .error(ApiResponse.ErrorInfo.builder()
                        .code("CONTENT_SERVICE_UNAVAILABLE")
                        .message("Content catalog is temporarily unavailable. Please try again shortly.")
                        .traceId(correlationId)
                        .build())
                .correlationId(correlationId)
                .timestamp(java.time.Instant.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/playback")
    public Mono<ResponseEntity<ApiResponse<Void>>> playbackFallback(ServerWebExchange exchange) {
        String correlationId = exchange.getAttribute("correlationId");
        log.warn("[{}] Playback service fallback triggered", correlationId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .error(ApiResponse.ErrorInfo.builder()
                        .code("PLAYBACK_SERVICE_UNAVAILABLE")
                        .message("Playback service is temporarily unavailable. Please try again shortly.")
                        .traceId(correlationId)
                        .build())
                .correlationId(correlationId)
                .timestamp(java.time.Instant.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/recommendations")
    public Mono<ResponseEntity<ApiResponse<Void>>> recommendationsFallback(ServerWebExchange exchange) {
        String correlationId = exchange.getAttribute("correlationId");
        log.warn("[{}] Recommendations service fallback triggered", correlationId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .error(ApiResponse.ErrorInfo.builder()
                        .code("RECOMMENDATIONS_UNAVAILABLE")
                        .message("Personalized recommendations are temporarily unavailable.")
                        .traceId(correlationId)
                        .build())
                .correlationId(correlationId)
                .timestamp(java.time.Instant.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<ApiResponse<Void>>> searchFallback(ServerWebExchange exchange) {
        String correlationId = exchange.getAttribute("correlationId");
        log.warn("[{}] Search service fallback triggered", correlationId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .error(ApiResponse.ErrorInfo.builder()
                        .code("SEARCH_SERVICE_UNAVAILABLE")
                        .message("Search is temporarily unavailable. Please try again shortly.")
                        .traceId(correlationId)
                        .build())
                .correlationId(correlationId)
                .timestamp(java.time.Instant.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}
