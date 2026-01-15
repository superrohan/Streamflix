package com.streamflix.auth.repository;

import com.streamflix.auth.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Profile entity operations.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    /**
     * Find all active profiles for a user.
     */
    @Query("SELECT p FROM Profile p WHERE p.user.id = :userId AND p.deletedAt IS NULL ORDER BY p.createdAt")
    List<Profile> findByUserId(@Param("userId") UUID userId);

    /**
     * Find profile by ID, excluding soft-deleted.
     */
    @Query("SELECT p FROM Profile p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Profile> findActiveById(@Param("id") UUID id);

    /**
     * Find profile by ID with user eagerly loaded.
     */
    @Query("SELECT p FROM Profile p JOIN FETCH p.user WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Profile> findByIdWithUser(@Param("id") UUID id);

    /**
     * Count profiles for a user.
     */
    @Query("SELECT COUNT(p) FROM Profile p WHERE p.user.id = :userId AND p.deletedAt IS NULL")
    long countByUserId(@Param("userId") UUID userId);

    /**
     * Check if profile name exists for user.
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Profile p WHERE p.user.id = :userId AND p.name = :name AND p.deletedAt IS NULL")
    boolean existsByUserIdAndName(@Param("userId") UUID userId, @Param("name") String name);

    /**
     * Check if profile belongs to user.
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Profile p WHERE p.id = :profileId AND p.user.id = :userId AND p.deletedAt IS NULL")
    boolean belongsToUser(@Param("profileId") UUID profileId, @Param("userId") UUID userId);

    /**
     * Find kids profiles for a user.
     */
    @Query("SELECT p FROM Profile p WHERE p.user.id = :userId AND p.isKidsProfile = true AND p.deletedAt IS NULL")
    List<Profile> findKidsProfilesByUserId(@Param("userId") UUID userId);
}
