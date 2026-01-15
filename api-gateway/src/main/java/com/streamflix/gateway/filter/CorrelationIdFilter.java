package com.streamflix.gateway.filter;

import com.streamflix.common.security.SecurityConstants;
import com.streamflix.common.util.CorrelationIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter that ensures every request has a correlation ID.
 *
 * Correlation IDs enable:
 * - End-to-end request tracing across all services
 * - Log aggregation in ELK/Splunk/Datadog
 * - Debugging distributed issues
 *
 * Flow:
 * 1. Check if request has X-Correlation-ID header
 * 2. If not, generate a new correlation ID
 * 3. Add correlation ID to request headers for downstream services
 * 4. Add correlation ID to response headers for client debugging
 *
 * Order: Runs first (highest priority) to ensure all subsequent filters have access.
 */
@Slf4j
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Get or generate correlation ID
        String correlationId = exchange.getRequest().getHeaders()
                .getFirst(SecurityConstants.CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = CorrelationIdGenerator.generate();
            log.debug("Generated new correlation ID: {}", correlationId);
        } else {
            log.debug("Using existing correlation ID: {}", correlationId);
        }

        // Add correlation ID to request for downstream services
        final String finalCorrelationId = correlationId;
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header(SecurityConstants.CORRELATION_ID_HEADER, finalCorrelationId)
                .build();

        // Add correlation ID to response for client
        exchange.getResponse().getHeaders()
                .add(SecurityConstants.CORRELATION_ID_HEADER, finalCorrelationId);

        // Store in exchange attributes for other filters
        exchange.getAttributes().put("correlationId", finalCorrelationId);

        // Continue filter chain with modified request
        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .contextWrite(ctx -> ctx.put("correlationId", finalCorrelationId));
    }

    @Override
    public int getOrder() {
        // Run first - before authentication and all other filters
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
