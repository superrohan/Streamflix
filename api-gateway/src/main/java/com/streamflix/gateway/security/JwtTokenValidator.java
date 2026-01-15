package com.streamflix.gateway.security;

import com.streamflix.common.security.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * JWT Token Validator for the API Gateway.
 *
 * Responsibilities:
 * - Validate JWT signature and expiration
 * - Check token blacklist (for logout support)
 * - Extract claims for downstream propagation
 *
 * Security Considerations:
 * - Uses HS256 with 256-bit secret (production should use RS256)
 * - Blacklist stored in Redis with TTL matching token expiration
 * - Clock skew tolerance of 60 seconds
 */
@Slf4j
@Component
public class JwtTokenValidator {

    private final SecretKey secretKey;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    public JwtTokenValidator(
            @Value("${jwt.secret}") String secret,
            ReactiveRedisTemplate<String, String> redisTemplate) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.redisTemplate = redisTemplate;
    }

    // Constructor for testing without Redis
    public JwtTokenValidator() {
        this.secretKey = Keys.hmacShaKeyFor(
                "your-256-bit-secret-key-for-development-only-change-in-production"
                        .getBytes(StandardCharsets.UTF_8));
        this.redisTemplate = null;
    }

    /**
     * Validate a JWT token.
     *
     * @param token The JWT token to validate
     * @return Mono<TokenValidationResult> containing validation result
     */
    public Mono<TokenValidationResult> validateToken(String token) {
        // First, parse and validate the token structure
        Claims claims;
        try {
            claims = parseToken(token);
        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            return Mono.just(TokenValidationResult.expired());
        } catch (JwtException e) {
            log.debug("Invalid token: {}", e.getMessage());
            return Mono.just(TokenValidationResult.invalid(e.getMessage()));
        }

        // Check if token is blacklisted (logout)
        if (redisTemplate != null) {
            String jti = claims.getId();
            if (jti != null) {
                return redisTemplate.hasKey(BLACKLIST_PREFIX + jti)
                        .flatMap(isBlacklisted -> {
                            if (Boolean.TRUE.equals(isBlacklisted)) {
                                log.debug("Token {} is blacklisted", jti);
                                return Mono.just(TokenValidationResult.revoked());
                            }
                            return Mono.just(TokenValidationResult.valid(claims));
                        })
                        .onErrorResume(e -> {
                            log.warn("Redis error during blacklist check, allowing token: {}", e.getMessage());
                            // Fail open - if Redis is down, allow the token
                            return Mono.just(TokenValidationResult.valid(claims));
                        });
            }
        }

        return Mono.just(TokenValidationResult.valid(claims));
    }

    /**
     * Parse a JWT token and extract claims.
     *
     * @throws JwtException if token is invalid
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .clockSkewSeconds(60)  // Allow 60 seconds clock skew
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract user ID from claims.
     */
    public String extractUserId(Claims claims) {
        return claims.get(SecurityConstants.CLAIM_USER_ID, String.class);
    }

    /**
     * Extract profile ID from claims.
     */
    public String extractProfileId(Claims claims) {
        return claims.get(SecurityConstants.CLAIM_PROFILE_ID, String.class);
    }

    /**
     * Extract email (subject) from claims.
     */
    public String extractEmail(Claims claims) {
        return claims.getSubject();
    }

    /**
     * Extract roles from claims.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(Claims claims) {
        Object roles = claims.get(SecurityConstants.CLAIM_ROLES);
        if (roles instanceof List) {
            return (List<String>) roles;
        }
        return Collections.emptyList();
    }

    /**
     * Check if token is an access token (not refresh).
     */
    public boolean isAccessToken(Claims claims) {
        String tokenType = claims.get(SecurityConstants.CLAIM_TOKEN_TYPE, String.class);
        return SecurityConstants.TOKEN_TYPE_ACCESS.equals(tokenType);
    }

    /**
     * Blacklist a token (for logout).
     * Token is stored in Redis until it would naturally expire.
     */
    public Mono<Boolean> blacklistToken(String token) {
        if (redisTemplate == null) {
            return Mono.just(false);
        }

        try {
            Claims claims = parseToken(token);
            String jti = claims.getId();
            Date expiration = claims.getExpiration();

            if (jti != null && expiration != null) {
                long ttlSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;
                if (ttlSeconds > 0) {
                    return redisTemplate.opsForValue()
                            .set(BLACKLIST_PREFIX + jti, "revoked",
                                    java.time.Duration.ofSeconds(ttlSeconds))
                            .thenReturn(true);
                }
            }
        } catch (JwtException e) {
            log.warn("Cannot blacklist invalid token: {}", e.getMessage());
        }

        return Mono.just(false);
    }

    /**
     * Result of token validation.
     */
    public static class TokenValidationResult {
        private final boolean valid;
        private final String error;
        private final TokenError errorType;
        private final Claims claims;

        private TokenValidationResult(boolean valid, Claims claims, String error, TokenError errorType) {
            this.valid = valid;
            this.claims = claims;
            this.error = error;
            this.errorType = errorType;
        }

        public static TokenValidationResult valid(Claims claims) {
            return new TokenValidationResult(true, claims, null, null);
        }

        public static TokenValidationResult invalid(String error) {
            return new TokenValidationResult(false, null, error, TokenError.INVALID);
        }

        public static TokenValidationResult expired() {
            return new TokenValidationResult(false, null, "Token has expired", TokenError.EXPIRED);
        }

        public static TokenValidationResult revoked() {
            return new TokenValidationResult(false, null, "Token has been revoked", TokenError.REVOKED);
        }

        public boolean isValid() {
            return valid;
        }

        public Claims getClaims() {
            return claims;
        }

        public String getError() {
            return error;
        }

        public TokenError getErrorType() {
            return errorType;
        }

        public enum TokenError {
            INVALID,
            EXPIRED,
            REVOKED
        }
    }
}
