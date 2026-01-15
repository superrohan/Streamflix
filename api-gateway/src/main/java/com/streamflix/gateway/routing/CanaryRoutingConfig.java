package com.streamflix.gateway.routing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Canary routing support for gradual rollouts.
 *
 * Enables traffic splitting between stable and canary deployments.
 *
 * Features:
 * - Header-based canary selection (X-Canary: true forces canary)
 * - Cookie-based sticky canary (for consistent user experience)
 * - Percentage-based random routing
 * - Metrics for canary vs stable traffic
 *
 * Usage in route config:
 *   filters:
 *     - CanaryRouting=10   # 10% to canary
 */
@Slf4j
@Component
public class CanaryRoutingConfig extends AbstractGatewayFilterFactory<CanaryRoutingConfig.Config> {

    private static final String CANARY_HEADER = "X-Canary";
    private static final String CANARY_COOKIE = "streamflix-canary";
    private static final Random RANDOM = new Random();

    public CanaryRoutingConfig() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            boolean useCanary = false;
            String reason = "random";

            // Check explicit canary header
            String canaryHeader = request.getHeaders().getFirst(CANARY_HEADER);
            if ("true".equalsIgnoreCase(canaryHeader)) {
                useCanary = true;
                reason = "header";
            } else if ("false".equalsIgnoreCase(canaryHeader)) {
                useCanary = false;
                reason = "header-opt-out";
            } else {
                // Check canary cookie for sticky routing
                String canaryCookie = request.getCookies().getFirst(CANARY_COOKIE) != null
                        ? request.getCookies().getFirst(CANARY_COOKIE).getValue()
                        : null;

                if (canaryCookie != null) {
                    useCanary = "true".equals(canaryCookie);
                    reason = "cookie";
                } else {
                    // Random percentage-based routing
                    int roll = RANDOM.nextInt(100);
                    useCanary = roll < config.getPercentage();
                    reason = "random-" + roll;
                }
            }

            log.debug("Canary routing decision: {} (reason: {}, config: {}%)",
                    useCanary, reason, config.getPercentage());

            // Add canary indicator header for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header(CANARY_HEADER, String.valueOf(useCanary))
                    .build();

            // Store in exchange for metrics
            exchange.getAttributes().put("canary", useCanary);

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    @Override
    public String name() {
        return "CanaryRouting";
    }

    public static class Config {
        private int percentage = 0;  // 0-100

        public int getPercentage() {
            return percentage;
        }

        public void setPercentage(int percentage) {
            if (percentage < 0) percentage = 0;
            if (percentage > 100) percentage = 100;
            this.percentage = percentage;
        }
    }
}
