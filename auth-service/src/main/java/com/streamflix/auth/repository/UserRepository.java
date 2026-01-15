package com.streamflix.auth.repository;

import com.streamflix.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email (case-insensitive), excluding soft-deleted.
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.deletedAt IS NULL")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * Check if email exists (case-insensitive), excluding soft-deleted.
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Find user by ID, excluding soft-deleted.
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveById(@Param("id") UUID id);

    /**
     * Find user with profiles eagerly loaded.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profiles WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdWithProfiles(@Param("id") UUID id);

    /**
     * Find user with roles eagerly loaded.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdWithRoles(@Param("id") UUID id);

    /**
     * Find user by email with roles eagerly loaded (for authentication).
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE LOWER(u.email) = LOWER(:email) AND u.deletedAt IS NULL")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    /**
     * Soft delete a user.
     */
    @Modifying
    @Query("UPDATE User u SET u.deletedAt = :deletedAt WHERE u.id = :id")
    int softDelete(@Param("id") UUID id, @Param("deletedAt") Instant deletedAt);

    /**
     * Update last login information.
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginAt, u.lastLoginIp = :ip, u.failedLoginAttempts = 0 WHERE u.id = :id")
    void updateLastLogin(@Param("id") UUID id, @Param("loginAt") Instant loginAt, @Param("ip") String ip);

    /**
     * Increment failed login attempts.
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :id")
    void incrementFailedLoginAttempts(@Param("id") UUID id);

    /**
     * Lock user account.
     */
    @Modifying
    @Query("UPDATE User u SET u.accountLocked = true, u.lockReason = :reason, u.lockedUntil = :until WHERE u.id = :id")
    void lockAccount(@Param("id") UUID id, @Param("reason") String reason, @Param("until") Instant until);

    /**
     * Unlock user account.
     */
    @Modifying
    @Query("UPDATE User u SET u.accountLocked = false, u.lockReason = null, u.lockedUntil = null, u.failedLoginAttempts = 0 WHERE u.id = :id")
    void unlockAccount(@Param("id") UUID id);

    /**
     * Verify user email.
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :id")
    void verifyEmail(@Param("id") UUID id);

    /**
     * Update password.
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :passwordHash WHERE u.id = :id")
    void updatePassword(@Param("id") UUID id, @Param("passwordHash") String passwordHash);
}
