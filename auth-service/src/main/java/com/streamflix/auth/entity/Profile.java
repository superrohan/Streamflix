package com.streamflix.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Profile entity representing a viewing profile within a user account.
 *
 * Mirrors Netflix's multi-profile model:
 * - Each account can have multiple profiles
 * - Each profile has its own watch history, preferences, recommendations
 * - Kids profiles have content restrictions
 * - Optional PIN protection per profile
 */
@Entity
@Table(name = "profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "is_kids_profile")
    @Builder.Default
    private Boolean isKidsProfile = false;

    @Column(name = "maturity_rating", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MaturityRating maturityRating = MaturityRating.ALL;

    @Column(name = "language_preference", length = 10)
    @Builder.Default
    private String languagePreference = "en";

    @Column(name = "autoplay_next_episode")
    @Builder.Default
    private Boolean autoplayNextEpisode = true;

    @Column(name = "autoplay_previews")
    @Builder.Default
    private Boolean autoplayPreviews = true;

    @Column(name = "pin_hash", length = 255)
    private String pinHash;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isPinProtected() {
        return pinHash != null && !pinHash.isEmpty();
    }

    public boolean isKids() {
        return Boolean.TRUE.equals(isKidsProfile);
    }

    /**
     * Maturity rating levels for content filtering.
     * Maps to common content rating systems.
     */
    public enum MaturityRating {
        ALL,        // All ages (G, TV-Y)
        KIDS,       // Kids content (PG, TV-Y7)
        TEEN,       // Teen content (PG-13, TV-14)
        ADULT       // Adult content (R, TV-MA)
    }
}
