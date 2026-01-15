package com.streamflix.auth.repository;

import com.streamflix.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for RefreshToken entity operations.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Find refresh token by hash.
     */
    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user WHERE rt.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHash(@Param("tokenHash") String tokenHash);

    /**
     * Find valid refresh token by hash (not expired, not revoked).
     */
    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user WHERE rt.tokenHash = :tokenHash AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    Optional<RefreshToken> findValidByTokenHash(@Param("tokenHash") String tokenHash, @Param("now") Instant now);

    /**
     * Find all active refresh tokens for a user.
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revokedAt IS NULL AND rt.expiresAt > :now ORDER BY rt.issuedAt DESC")
    List<RefreshToken> findActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    /**
     * Revoke all tokens for a user (logout all devices).
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt, rt.revokedReason = :reason WHERE rt.user.id = :userId AND rt.revokedAt IS NULL")
    int revokeAllByUserId(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt, @Param("reason") String reason);

    /**
     * Revoke token by device ID.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt, rt.revokedReason = :reason WHERE rt.user.id = :userId AND rt.deviceId = :deviceId AND rt.revokedAt IS NULL")
    int revokeByUserIdAndDeviceId(@Param("userId") UUID userId, @Param("deviceId") String deviceId, @Param("revokedAt") Instant revokedAt, @Param("reason") String reason);

    /**
     * Delete expired tokens (cleanup job).
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoff")
    int deleteExpired(@Param("cutoff") Instant cutoff);

    /**
     * Count active sessions for a user.
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    long countActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
}
