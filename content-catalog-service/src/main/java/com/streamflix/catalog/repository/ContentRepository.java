package com.streamflix.catalog.repository;

import com.streamflix.catalog.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Content entity.
 */
@Repository
public interface ContentRepository extends JpaRepository<Content, UUID> {

    @Query("SELECT c FROM Content c WHERE c.slug = :slug AND c.deletedAt IS NULL")
    Optional<Content> findBySlug(@Param("slug") String slug);

    @Query("SELECT c FROM Content c LEFT JOIN FETCH c.genres WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Content> findByIdWithGenres(@Param("id") UUID id);

    @Query("SELECT c FROM Content c WHERE c.contentType = :type AND c.isPublished = true AND c.deletedAt IS NULL")
    Page<Content> findByContentType(@Param("type") Content.ContentType type, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.isPublished = true AND c.deletedAt IS NULL ORDER BY c.popularityScore DESC")
    Page<Content> findPopular(Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.isFeatured = true AND c.isPublished = true AND c.deletedAt IS NULL ORDER BY c.publishedAt DESC")
    List<Content> findFeatured();

    @Query("SELECT c FROM Content c WHERE c.isOriginal = true AND c.isPublished = true AND c.deletedAt IS NULL ORDER BY c.publishedAt DESC")
    Page<Content> findOriginals(Pageable pageable);

    @Query("SELECT c FROM Content c JOIN c.genres g WHERE g.slug = :genreSlug AND c.isPublished = true AND c.deletedAt IS NULL ORDER BY c.popularityScore DESC")
    Page<Content> findByGenre(@Param("genreSlug") String genreSlug, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.releaseYear = :year AND c.isPublished = true AND c.deletedAt IS NULL ORDER BY c.popularityScore DESC")
    Page<Content> findByReleaseYear(@Param("year") Integer year, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.isPublished = true AND c.deletedAt IS NULL AND " +
           "(c.maturityRating = 'ALL' OR c.maturityRating = 'KIDS') ORDER BY c.popularityScore DESC")
    Page<Content> findKidsContent(Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.isPublished = true AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    Page<Content> findNewReleases(Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.id IN :ids AND c.deletedAt IS NULL")
    List<Content> findByIds(@Param("ids") List<UUID> ids);
}
