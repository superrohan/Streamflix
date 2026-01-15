package com.streamflix.gateway.health;

import com.streamflix.gateway.config.Resilience4jConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Health and diagnostic endpoints for the API Gateway.
 */
@RestController
@RequestMapping("/gateway")
@RequiredArgsConstructor
public class GatewayHealthController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final Resilience4jConfig.CircuitBreakerHealthInfo circuitBreakerHealthInfo;

    /**
     * Get circuit breaker status for all downstream services.
     */
    @GetMapping("/circuit-breakers")
    public Mono<ResponseEntity<Map<String, Object>>> getCircuitBreakerStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("circuitBreakers", circuitBreakerHealthInfo.getHealthInfo());
        response.put("timestamp", java.time.Instant.now().toString());
        return Mono.just(ResponseEntity.ok(response));
    }

    /**
     * Get gateway health summary.
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", java.time.Instant.now().toString());

        // Check circuit breakers
        long openCircuits = circuitBreakerRegistry.getAllCircuitBreakers().stream()
                .filter(cb -> cb.getState() == io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN)
                .count();

        health.put("openCircuitBreakers", openCircuits);
        health.put("totalCircuitBreakers", circuitBreakerRegistry.getAllCircuitBreakers().spliterator().getExactSizeIfKnown());

        if (openCircuits > 0) {
            health.put("status", "DEGRADED");
        }

        return Mono.just(ResponseEntity.ok(health));
    }
}
