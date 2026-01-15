package com.streamflix.recommendation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "viewing_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "watch_percentage")
    private Integer watchPercentage;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "processed")
    @Builder.Default
    private Boolean processed = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
