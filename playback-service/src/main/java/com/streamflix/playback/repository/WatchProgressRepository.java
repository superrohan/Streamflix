package com.streamflix.playback.repository;

import com.streamflix.playback.entity.WatchProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WatchProgressRepository extends JpaRepository<WatchProgress, UUID> {

    @Query("SELECT wp FROM WatchProgress wp WHERE wp.profileId = :profileId AND wp.contentId = :contentId AND (wp.episodeId = :episodeId OR (wp.episodeId IS NULL AND :episodeId IS NULL))")
    Optional<WatchProgress> findByProfileIdAndContentIdAndEpisodeId(
            @Param("profileId") UUID profileId,
            @Param("contentId") UUID contentId,
            @Param("episodeId") UUID episodeId);

    @Query(value = "SELECT * FROM watch_progress WHERE profile_id = :profileId AND is_completed = false AND watch_percentage > 5 ORDER BY last_watched_at DESC LIMIT :limit", nativeQuery = true)
    List<WatchProgress> findContinueWatching(@Param("profileId") UUID profileId, @Param("limit") int limit);

    @Query(value = "SELECT * FROM watch_progress WHERE profile_id = :profileId ORDER BY last_watched_at DESC OFFSET :offset LIMIT :limit", nativeQuery = true)
    List<WatchProgress> findWatchHistory(@Param("profileId") UUID profileId, @Param("offset") int offset, @Param("limit") int limit);
}
