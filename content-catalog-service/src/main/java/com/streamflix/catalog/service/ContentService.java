package com.streamflix.catalog.service;

import com.streamflix.catalog.dto.ContentDto;
import com.streamflix.catalog.entity.Content;
import com.streamflix.catalog.entity.Genre;
import com.streamflix.catalog.repository.ContentRepository;
import com.streamflix.catalog.repository.GenreRepository;
import com.streamflix.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for content catalog operations.
 *
 * Implements cache-aside pattern:
 * - Read: Check cache first, load from DB if miss
 * - Write: Update DB, then invalidate cache
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final GenreRepository genreRepository;
    private final ContentEventPublisher eventPublisher;

    /**
     * Get content by ID.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "content", key = "#id", unless = "#result == null")
    public ContentDto.Response getContentById(UUID id) {
        Content content = contentRepository.findByIdWithGenres(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content", id.toString()));
        return toResponse(content);
    }

    /**
     * Get content by slug.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "content-slug", key = "#slug", unless = "#result == null")
    public ContentDto.Response getContentBySlug(String slug) {
        Content content = contentRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Content", "slug", slug));
        return toResponse(content);
    }

    /**
     * Get movies with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ContentDto.Summary> getMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "popularityScore"));
        return contentRepository.findByContentType(Content.ContentType.MOVIE, pageable)
                .map(this::toSummary);
    }

    /**
     * Get series with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ContentDto.Summary> getSeries(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "popularityScore"));
        return contentRepository.findByContentType(Content.ContentType.SERIES, pageable)
                .map(this::toSummary);
    }

    /**
     * Get popular content.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "popular-content", key = "#page + '-' + #size")
    public Page<ContentDto.Summary> getPopularContent(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return contentRepository.findPopular(pageable)
                .map(this::toSummary);
    }

    /**
     * Get featured content.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "featured-content")
    public List<ContentDto.Summary> getFeaturedContent() {
        return contentRepository.findFeatured().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /**
     * Get content by genre.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "genre-content", key = "#genreSlug + '-' + #page + '-' + #size")
    public Page<ContentDto.Summary> getContentByGenre(String genreSlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return contentRepository.findByGenre(genreSlug, pageable)
                .map(this::toSummary);
    }

    /**
     * Get new releases.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "new-releases", key = "#page + '-' + #size")
    public Page<ContentDto.Summary> getNewReleases(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return contentRepository.findNewReleases(pageable)
                .map(this::toSummary);
    }

    /**
     * Get Streamflix Originals.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "originals", key = "#page + '-' + #size")
    public Page<ContentDto.Summary> getOriginals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return contentRepository.findOriginals(pageable)
                .map(this::toSummary);
    }

    /**
     * Get kids content.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "kids-content", key = "#page + '-' + #size")
    public Page<ContentDto.Summary> getKidsContent(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return contentRepository.findKidsContent(pageable)
                .map(this::toSummary);
    }

    /**
     * Get all active genres.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "genres")
    public List<ContentDto.GenreResponse> getAllGenres() {
        return genreRepository.findAllActive().stream()
                .map(this::toGenreResponse)
                .collect(Collectors.toList());
    }

    /**
     * Increment view count for content.
     */
    @Transactional
    @CacheEvict(value = "content", key = "#contentId")
    public void incrementViewCount(UUID contentId) {
        contentRepository.findById(contentId).ifPresent(content -> {
            content.setViewCount(content.getViewCount() + 1);
            contentRepository.save(content);
        });
    }

    private ContentDto.Response toResponse(Content content) {
        List<String> genres = content.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.toList());

        return ContentDto.Response.builder()
                .id(content.getId().toString())
                .contentType(content.getContentType().name())
                .title(content.getTitle())
                .originalTitle(content.getOriginalTitle())
                .slug(content.getSlug())
                .description(content.getDescription())
                .shortDescription(content.getShortDescription())
                .releaseYear(content.getReleaseYear())
                .runtimeMinutes(content.getRuntimeMinutes())
                .maturityRating(content.getMaturityRating().name())
                .posterUrl(content.getPosterUrl())
                .backdropUrl(content.getBackdropUrl())
                .trailerUrl(content.getTrailerUrl())
                .genres(genres)
                .averageRating(content.getAverageRating())
                .ratingCount(content.getRatingCount())
                .isOriginal(content.getIsOriginal())
                .isFeatured(content.getIsFeatured())
                .build();
    }

    private ContentDto.Summary toSummary(Content content) {
        return ContentDto.Summary.builder()
                .id(content.getId().toString())
                .contentType(content.getContentType().name())
                .title(content.getTitle())
                .slug(content.getSlug())
                .shortDescription(content.getShortDescription())
                .releaseYear(content.getReleaseYear())
                .maturityRating(content.getMaturityRating().name())
                .posterUrl(content.getPosterUrl())
                .averageRating(content.getAverageRating())
                .isOriginal(content.getIsOriginal())
                .build();
    }

    private ContentDto.GenreResponse toGenreResponse(Genre genre) {
        return ContentDto.GenreResponse.builder()
                .id(genre.getId().toString())
                .name(genre.getName())
                .slug(genre.getSlug())
                .description(genre.getDescription())
                .imageUrl(genre.getImageUrl())
                .build();
    }
}
