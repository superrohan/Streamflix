package com.streamflix.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Resilience4j configuration and monitoring.
 *
 * Provides:
 * - Circuit breaker metrics export to Prometheus
 * - Event logging for circuit state changes
 * - Custom configurations per service
 */
@Slf4j
@Configuration
public class Resilience4jConfig {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final MeterRegistry meterRegistry;

    public Resilience4jConfig(CircuitBreakerRegistry circuitBreakerRegistry,
                               MeterRegistry meterRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // Register circuit breaker metrics with Micrometer
        TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry)
                .bindTo(meterRegistry);

        // Add event listeners for all circuit breakers
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(this::addEventListeners);

        // Listen for new circuit breakers
        circuitBreakerRegistry.getEventPublisher()
                .onEntryAdded(event -> addEventListeners(event.getAddedEntry()));
    }

    private void addEventListeners(CircuitBreaker circuitBreaker) {
        String name = circuitBreaker.getName();

        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn("CircuitBreaker {} state transition: {} -> {}",
                                name,
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()))
                .onError(event ->
                        log.debug("CircuitBreaker {} error: {} - {}",
                                name,
                                event.getElapsedDuration().toMillis(),
                                event.getThrowable().getMessage()))
                .onSuccess(event ->
                        log.trace("CircuitBreaker {} success: {}ms",
                                name,
                                event.getElapsedDuration().toMillis()))
                .onCallNotPermitted(event ->
                        log.warn("CircuitBreaker {} call not permitted - circuit is OPEN",
                                name))
                .onSlowCallRateExceeded(event ->
                        log.warn("CircuitBreaker {} slow call rate exceeded: {}%",
                                name,
                                event.getSlowCallRate()))
                .onFailureRateExceeded(event ->
                        log.warn("CircuitBreaker {} failure rate exceeded: {}%",
                                name,
                                event.getFailureRate()));
    }

    /**
     * Expose circuit breaker health info.
     */
    @Bean
    public CircuitBreakerHealthInfo circuitBreakerHealthInfo() {
        return new CircuitBreakerHealthInfo(circuitBreakerRegistry);
    }

    public static class CircuitBreakerHealthInfo {
        private final CircuitBreakerRegistry registry;

        public CircuitBreakerHealthInfo(CircuitBreakerRegistry registry) {
            this.registry = registry;
        }

        public java.util.Map<String, Object> getHealthInfo() {
            java.util.Map<String, Object> info = new java.util.HashMap<>();

            registry.getAllCircuitBreakers().forEach(cb -> {
                java.util.Map<String, Object> cbInfo = new java.util.HashMap<>();
                cbInfo.put("state", cb.getState().name());
                cbInfo.put("failureRate", cb.getMetrics().getFailureRate());
                cbInfo.put("slowCallRate", cb.getMetrics().getSlowCallRate());
                cbInfo.put("bufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
                cbInfo.put("failedCalls", cb.getMetrics().getNumberOfFailedCalls());
                cbInfo.put("successfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());
                info.put(cb.getName(), cbInfo);
            });

            return info;
        }
    }
}
