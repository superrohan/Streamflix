package com.streamflix.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Season entity for TV series.
 */
@Entity
@Table(name = "seasons")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", nullable = false)
    private Content series;

    @Column(name = "season_number", nullable = false)
    private Integer seasonNumber;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    @Column(name = "episode_count")
    @Builder.Default
    private Integer episodeCount = 0;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean isPublished = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("episodeNumber ASC")
    private Set<Episode> episodes = new HashSet<>();

    public void addEpisode(Episode episode) {
        episodes.add(episode);
        episode.setSeason(this);
        this.episodeCount = episodes.size();
    }
}
