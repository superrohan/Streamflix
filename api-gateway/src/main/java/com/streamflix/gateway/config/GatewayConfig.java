package com.streamflix.gateway.config;

import com.streamflix.gateway.filter.CorrelationIdFilter;
import com.streamflix.gateway.filter.JwtAuthenticationFilter;
import com.streamflix.gateway.filter.LoggingFilter;
import com.streamflix.gateway.filter.RequireProfileFilter;
import com.streamflix.gateway.filter.RequireRoleFilter;
import com.streamflix.gateway.ratelimit.UserKeyResolver;
import com.streamflix.gateway.security.JwtTokenValidator;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central configuration for API Gateway components.
 *
 * Defines:
 * - Custom filter beans
 * - Rate limiting key resolvers
 * - Security configuration
 */
@Configuration
public class GatewayConfig {

    /**
     * Key resolver for rate limiting.
     * Uses user ID for authenticated requests, IP for anonymous.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return new UserKeyResolver();
    }

    /**
     * JWT token validator bean.
     */
    @Bean
    public JwtTokenValidator jwtTokenValidator() {
        return new JwtTokenValidator();
    }

    /**
     * Correlation ID filter factory.
     */
    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }

    /**
     * Request/Response logging filter factory.
     */
    @Bean
    public LoggingFilter loggingFilter() {
        return new LoggingFilter();
    }

    /**
     * JWT Authentication filter factory.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenValidator jwtTokenValidator) {
        return new JwtAuthenticationFilter(jwtTokenValidator);
    }

    /**
     * Profile requirement filter factory.
     */
    @Bean
    public RequireProfileFilter requireProfileFilter() {
        return new RequireProfileFilter();
    }

    /**
     * Role-based access filter factory.
     */
    @Bean
    public RequireRoleFilter requireRoleFilter() {
        return new RequireRoleFilter();
    }
}
