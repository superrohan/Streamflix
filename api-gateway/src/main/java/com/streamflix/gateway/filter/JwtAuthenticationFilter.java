package com.streamflix.gateway.filter;

import com.streamflix.common.security.SecurityConstants;
import com.streamflix.gateway.security.JwtTokenValidator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT Authentication Gateway Filter.
 *
 * Validates JWT tokens and propagates user context to downstream services.
 *
 * Flow:
 * 1. Extract Bearer token from Authorization header
 * 2. Validate token signature and expiration
 * 3. Check token blacklist (for logout support)
 * 4. Extract claims and add as headers for downstream services
 *
 * Headers Propagated:
 * - X-User-ID: The authenticated user's ID
 * - X-Profile-ID: The selected profile (if present in token)
 * - X-User-Roles: Comma-separated list of roles
 *
 * Public paths (configured in SecurityConstants) bypass authentication.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtTokenValidator jwtTokenValidator;

    public JwtAuthenticationFilter() {
        super(Config.class);
        this.jwtTokenValidator = new JwtTokenValidator();
    }

    public JwtAuthenticationFilter(JwtTokenValidator jwtTokenValidator) {
        super(Config.class);
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            // Skip authentication for public paths
            if (isPublicPath(path)) {
                log.debug("Skipping authentication for public path: {}", path);
                return chain.filter(exchange);
            }

            // Extract Bearer token
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
                log.debug("No valid Authorization header found for path: {}", path);
                return onUnauthorized(exchange, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(SecurityConstants.BEARER_PREFIX.length());

            // Validate token
            return jwtTokenValidator.validateToken(token)
                    .flatMap(result -> {
                        if (!result.isValid()) {
                            log.debug("Token validation failed: {}", result.getError());
                            return onTokenError(exchange, result);
                        }

                        // Token is valid, extract claims and propagate
                        Claims claims = result.getClaims();

                        // Verify it's an access token, not refresh
                        if (!jwtTokenValidator.isAccessToken(claims)) {
                            return onUnauthorized(exchange, "Invalid token type");
                        }

                        String userId = jwtTokenValidator.extractUserId(claims);
                        String profileId = jwtTokenValidator.extractProfileId(claims);
                        List<String> roles = jwtTokenValidator.extractRoles(claims);

                        log.debug("Authenticated user: {}, profile: {}, roles: {}",
                                userId, profileId, roles);

                        // Build modified request with user context headers
                        ServerHttpRequest.Builder requestBuilder = request.mutate();
                        requestBuilder.header(SecurityConstants.USER_ID_HEADER, userId);

                        if (profileId != null && !profileId.isEmpty()) {
                            requestBuilder.header(SecurityConstants.PROFILE_ID_HEADER, profileId);
                        }

                        if (!roles.isEmpty()) {
                            requestBuilder.header(SecurityConstants.ROLES_HEADER, String.join(",", roles));
                        }

                        // Store in exchange attributes for other filters
                        exchange.getAttributes().put("userId", userId);
                        exchange.getAttributes().put("profileId", profileId);
                        exchange.getAttributes().put("roles", roles);

                        return chain.filter(exchange.mutate()
                                .request(requestBuilder.build())
                                .build());
                    });
        };
    }

    private boolean isPublicPath(String path) {
        return Arrays.stream(SecurityConstants.PUBLIC_PATHS)
                .anyMatch(publicPath -> {
                    if (publicPath.endsWith("/**")) {
                        String prefix = publicPath.substring(0, publicPath.length() - 3);
                        return path.startsWith(prefix);
                    }
                    return path.equals(publicPath);
                });
    }

    private Mono<Void> onUnauthorized(ServerWebExchange exchange, String message) {
        return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message);
    }

    private Mono<Void> onTokenError(ServerWebExchange exchange,
                                     JwtTokenValidator.TokenValidationResult result) {
        return switch (result.getErrorType()) {
            case EXPIRED -> writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                    "TOKEN_EXPIRED", "Your session has expired. Please log in again.");
            case REVOKED -> writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                    "TOKEN_REVOKED", "Your session has been terminated. Please log in again.");
            default -> writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN", result.getError());
        };
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status,
                                           String errorCode, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String correlationId = exchange.getAttribute("correlationId");
        String json = String.format(
                "{\"success\":false,\"error\":{\"code\":\"%s\",\"message\":\"%s\"},\"correlationId\":\"%s\"}",
                errorCode, message, correlationId != null ? correlationId : ""
        );

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public String name() {
        return "JwtAuthentication";
    }

    public static class Config {
        // Configuration properties can be added here
    }
}
