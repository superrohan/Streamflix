package com.streamflix.recommendation.repository;

import com.streamflix.recommendation.entity.ViewingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ViewingEventRepository extends JpaRepository<ViewingEvent, UUID> {

    boolean existsByEventId(String eventId);

    @Query("SELECT ve FROM ViewingEvent ve WHERE ve.profileId = :profileId AND ve.eventType = 'VIDEO_COMPLETED' ORDER BY ve.eventTimestamp DESC")
    List<ViewingEvent> findRecentCompletedByProfile(@Param("profileId") UUID profileId, int limit);

    @Query("SELECT ve.contentId, COUNT(ve), AVG(ve.watchPercentage) FROM ViewingEvent ve WHERE ve.eventTimestamp > :since GROUP BY ve.contentId ORDER BY COUNT(ve) DESC")
    List<Object[]> getViewCountsByContent(@Param("since") Instant since);
}
