package com.streamflix.gateway.filter;

import com.streamflix.common.security.SecurityConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Filter that requires a profile to be selected.
 *
 * Some operations (like playback, recommendations) require a profile context
 * because they are personalized per profile within an account.
 *
 * This filter checks for the X-Profile-ID header (set by JWT auth filter)
 * and returns 400 Bad Request if not present.
 */
@Slf4j
@Component
public class RequireProfileFilter extends AbstractGatewayFilterFactory<RequireProfileFilter.Config> {

    public RequireProfileFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String profileId = exchange.getRequest().getHeaders()
                    .getFirst(SecurityConstants.PROFILE_ID_HEADER);

            // Also check exchange attributes (set by auth filter)
            if (profileId == null || profileId.isEmpty()) {
                profileId = exchange.getAttribute("profileId");
            }

            if (profileId == null || profileId.isEmpty()) {
                log.debug("Profile not selected for request: {}",
                        exchange.getRequest().getPath());
                return writeErrorResponse(exchange);
            }

            log.debug("Profile {} selected, proceeding", profileId);
            return chain.filter(exchange);
        };
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String correlationId = exchange.getAttribute("correlationId");
        String json = String.format(
                "{\"success\":false,\"error\":{\"code\":\"PROFILE_REQUIRED\",\"message\":\"Please select a profile to continue.\"},\"correlationId\":\"%s\"}",
                correlationId != null ? correlationId : ""
        );

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public String name() {
        return "RequireProfile";
    }

    public static class Config {
        // Configuration properties can be added here
    }
}
