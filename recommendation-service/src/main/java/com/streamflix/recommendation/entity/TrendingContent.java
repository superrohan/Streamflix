package com.streamflix.recommendation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trending_content")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendingContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @Column(name = "rank", nullable = false)
    private Integer rank;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "score", nullable = false, precision = 10, scale = 4)
    private BigDecimal score;

    @Column(name = "view_count_24h")
    @Builder.Default
    private Long viewCount24h = 0L;

    @Column(name = "calculated_at")
    @Builder.Default
    private Instant calculatedAt = Instant.now();
}
