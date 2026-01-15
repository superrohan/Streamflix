package com.streamflix.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User entity representing an account in the Streamflix platform.
 *
 * Design Notes:
 * - Soft delete support via deletedAt
 * - Optimistic locking via version
 * - Multiple profiles per user (Netflix model)
 * - Subscription tier support
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Column(name = "language_preference", length = 10)
    @Builder.Default
    private String languagePreference = "en";

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "account_locked")
    @Builder.Default
    private Boolean accountLocked = false;

    @Column(name = "lock_reason", length = 500)
    private String lockReason;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "subscription_tier", length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubscriptionTier subscriptionTier = SubscriptionTier.BASIC;

    @Column(name = "subscription_status", length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.ACTIVE;

    @Column(name = "subscription_expires_at")
    private Instant subscriptionExpiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Profile> profiles = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public void addProfile(Profile profile) {
        profiles.add(profile);
        profile.setUser(this);
    }

    public void removeProfile(Profile profile) {
        profiles.remove(profile);
        profile.setUser(null);
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isLocked() {
        if (!accountLocked) {
            return false;
        }
        if (lockedUntil != null && Instant.now().isAfter(lockedUntil)) {
            // Lock has expired
            return false;
        }
        return true;
    }

    public void recordSuccessfulLogin(String ipAddress) {
        this.failedLoginAttempts = 0;
        this.lastLoginAt = Instant.now();
        this.lastLoginIp = ipAddress;
        // Auto-unlock if previously locked
        if (this.accountLocked && this.lockedUntil != null && Instant.now().isAfter(this.lockedUntil)) {
            this.accountLocked = false;
            this.lockReason = null;
            this.lockedUntil = null;
        }
    }

    public void recordFailedLogin() {
        this.failedLoginAttempts++;
    }

    public void lockAccount(String reason, Instant until) {
        this.accountLocked = true;
        this.lockReason = reason;
        this.lockedUntil = until;
    }

    public void unlockAccount() {
        this.accountLocked = false;
        this.lockReason = null;
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
    }

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return null;
        }
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }

    public enum SubscriptionTier {
        BASIC,
        STANDARD,
        PREMIUM
    }

    public enum SubscriptionStatus {
        ACTIVE,
        CANCELLED,
        SUSPENDED,
        EXPIRED
    }
}
