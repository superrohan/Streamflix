package com.streamflix.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for extracting claims from JWT tokens.
 *
 * Used by services to validate and extract user information
 * from tokens passed by the API Gateway.
 */
@Slf4j
public class JwtClaimsExtractor {

    private final SecretKey secretKey;

    public JwtClaimsExtractor(String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parse and validate a JWT token.
     *
     * @param token The JWT token string
     * @return Optional containing claims if valid, empty otherwise
     */
    public Optional<Claims> extractClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (Exception e) {
            log.warn("Failed to parse JWT token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extract user ID from token.
     */
    public Optional<String> extractUserId(String token) {
        return extractClaims(token)
                .map(claims -> claims.get("userId", String.class));
    }

    /**
     * Extract profile ID from token.
     */
    public Optional<String> extractProfileId(String token) {
        return extractClaims(token)
                .map(claims -> claims.get("profileId", String.class));
    }

    /**
     * Extract email from token.
     */
    public Optional<String> extractEmail(String token) {
        return extractClaims(token)
                .map(Claims::getSubject);
    }

    /**
     * Extract roles from token.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaims(token)
                .map(claims -> claims.get("roles", List.class))
                .orElse(Collections.emptyList());
    }

    /**
     * Check if the token is valid (not expired and properly signed).
     */
    public boolean isValid(String token) {
        return extractClaims(token).isPresent();
    }
}
