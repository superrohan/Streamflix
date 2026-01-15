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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Filter that enforces role-based access control.
 *
 * Usage in route config:
 *   filters:
 *     - RequireRole=ROLE_ADMIN,ROLE_ANALYTICS_VIEWER
 *
 * User must have at least one of the specified roles to access the route.
 */
@Slf4j
@Component
public class RequireRoleFilter extends AbstractGatewayFilterFactory<RequireRoleFilter.Config> {

    public RequireRoleFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            List<String> userRoles = getUserRoles(exchange);
            List<String> requiredRoles = config.getRoles();

            log.debug("Checking roles. Required: {}, User has: {}", requiredRoles, userRoles);

            // Check if user has any of the required roles
            boolean hasRequiredRole = requiredRoles.stream()
                    .anyMatch(userRoles::contains);

            if (!hasRequiredRole) {
                log.warn("Access denied. User roles {} do not include any of required roles {}",
                        userRoles, requiredRoles);
                return writeErrorResponse(exchange, requiredRoles);
            }

            return chain.filter(exchange);
        };
    }

    @SuppressWarnings("unchecked")
    private List<String> getUserRoles(ServerWebExchange exchange) {
        // First check exchange attributes (set by auth filter)
        Object rolesAttr = exchange.getAttribute("roles");
        if (rolesAttr instanceof List) {
            return (List<String>) rolesAttr;
        }

        // Fall back to header
        String rolesHeader = exchange.getRequest().getHeaders()
                .getFirst(SecurityConstants.ROLES_HEADER);
        if (rolesHeader != null && !rolesHeader.isEmpty()) {
            return Arrays.asList(rolesHeader.split(","));
        }

        return Collections.emptyList();
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, List<String> requiredRoles) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String correlationId = exchange.getAttribute("correlationId");
        String json = String.format(
                "{\"success\":false,\"error\":{\"code\":\"ACCESS_DENIED\",\"message\":\"You do not have permission to access this resource. Required roles: %s\"},\"correlationId\":\"%s\"}",
                String.join(", ", requiredRoles),
                correlationId != null ? correlationId : ""
        );

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public String name() {
        return "RequireRole";
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("roles");
    }

    public static class Config {
        private List<String> roles = Collections.emptyList();

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(String roles) {
            this.roles = Arrays.asList(roles.split(","));
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
