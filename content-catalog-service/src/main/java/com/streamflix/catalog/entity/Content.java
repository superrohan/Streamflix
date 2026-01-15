package com.streamflix.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Content entity representing movies and series.
 */
@Entity
@Table(name = "content")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "content_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "original_title", length = 500)
    private String originalTitle;

    @Column(nullable = false, unique = true, length = 500)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "runtime_minutes")
    private Integer runtimeMinutes;

    @Column(name = "maturity_rating", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MaturityRating maturityRating = MaturityRating.ALL;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    @Column(name = "backdrop_url", length = 500)
    private String backdropUrl;

    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Column(name = "imdb_id", length = 20)
    private String imdbId;

    @Column(name = "tmdb_id")
    private Integer tmdbId;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "rating_count")
    @Builder.Default
    private Integer ratingCount = 0;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "popularity_score", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal popularityScore = BigDecimal.ZERO;

    @Column(name = "is_original")
    @Builder.Default
    private Boolean isOriginal = false;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean isPublished = false;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "available_from")
    private Instant availableFrom;

    @Column(name = "available_until")
    private Instant availableUntil;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ManyToMany
    @JoinTable(
        name = "content_genres",
        joinColumns = @JoinColumn(name = "content_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("seasonNumber ASC")
    private Set<Season> seasons = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "content_tags", joinColumns = @JoinColumn(name = "content_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    public boolean isMovie() {
        return ContentType.MOVIE.equals(contentType);
    }

    public boolean isSeries() {
        return ContentType.SERIES.equals(contentType);
    }

    public boolean isAvailable() {
        if (!Boolean.TRUE.equals(isPublished)) {
            return false;
        }
        Instant now = Instant.now();
        if (availableFrom != null && now.isBefore(availableFrom)) {
            return false;
        }
        if (availableUntil != null && now.isAfter(availableUntil)) {
            return false;
        }
        return true;
    }

    public enum ContentType {
        MOVIE,
        SERIES
    }

    public enum MaturityRating {
        ALL,
        KIDS,
        TEEN,
        ADULT
    }
}
