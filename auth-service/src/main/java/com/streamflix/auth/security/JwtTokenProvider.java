package com.streamflix.auth.security;

import com.streamflix.auth.entity.Profile;
import com.streamflix.auth.entity.User;
import com.streamflix.common.security.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT Token Provider for generating and validating tokens.
 *
 * Token Structure:
 * - Access Token: Short-lived, contains user/profile info for API access
 * - Refresh Token: Longer-lived, used only to obtain new access tokens
 *
 * Security Considerations:
 * - Access tokens expire quickly (1 hour by default)
 * - Refresh tokens stored hashed in DB, can be revoked
 * - Tokens include JTI for blacklisting support
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration,
            @Value("${jwt.issuer}") String issuer) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.issuer = issuer;
    }

    /**
     * Generate access token for a user.
     */
    public String generateAccessToken(User user, Profile profile) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenExpiration);

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .toList();

        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getEmail())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(SecurityConstants.CLAIM_USER_ID, user.getId().toString())
                .claim(SecurityConstants.CLAIM_EMAIL, user.getEmail())
                .claim(SecurityConstants.CLAIM_ROLES, roles)
                .claim(SecurityConstants.CLAIM_TOKEN_TYPE, SecurityConstants.TOKEN_TYPE_ACCESS);

        if (profile != null) {
            builder.claim(SecurityConstants.CLAIM_PROFILE_ID, profile.getId().toString());
        }

        return builder.signWith(secretKey).compact();
    }

    /**
     * Generate refresh token for a user.
     */
    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshTokenExpiration);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getEmail())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(SecurityConstants.CLAIM_USER_ID, user.getId().toString())
                .claim(SecurityConstants.CLAIM_TOKEN_TYPE, SecurityConstants.TOKEN_TYPE_REFRESH)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Parse and validate a token.
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get access token expiration time in seconds.
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration / 1000;
    }

    /**
     * Get refresh token expiration time.
     */
    public Instant getRefreshTokenExpiration() {
        return Instant.now().plusMillis(refreshTokenExpiration);
    }

    /**
     * Extract user ID from token.
     */
    public String extractUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get(SecurityConstants.CLAIM_USER_ID, String.class);
    }

    /**
     * Check if token is a refresh token.
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            String tokenType = claims.get(SecurityConstants.CLAIM_TOKEN_TYPE, String.class);
            return SecurityConstants.TOKEN_TYPE_REFRESH.equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Hash a token for storage.
     */
    public String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
