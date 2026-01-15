package com.streamflix.catalog.controller;

import com.streamflix.catalog.dto.ContentDto;
import com.streamflix.catalog.service.ContentService;
import com.streamflix.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for content catalog operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @GetMapping("/content/{id}")
    public ResponseEntity<ApiResponse<ContentDto.Response>> getContentById(@PathVariable String id) {
        ContentDto.Response content = contentService.getContentById(UUID.fromString(id));
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @GetMapping("/content/slug/{slug}")
    public ResponseEntity<ApiResponse<ContentDto.Response>> getContentBySlug(@PathVariable String slug) {
        ContentDto.Response content = contentService.getContentBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @GetMapping("/movies")
    public ResponseEntity<ApiResponse<Page<ContentDto.Summary>>> getMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ContentDto.Summary> movies = contentService.getMovies(page, size);
        return ResponseEntity.ok(ApiResponse.success(movies));
    }

    @GetMapping("/series")
    public ResponseEntity<ApiResponse<Page<ContentDto.Summary>>> getSeries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ContentDto.Summary> series = contentService.getSeries(page, size);
        return ResponseEntity.ok(ApiResponse.success(series));
    }

    @GetMapping("/content/popular")
    public ResponseEntity<ApiResponse<Page<ContentDto.Summary>>> getPopularContent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ContentDto.Summary> content = contentService.getPopularContent(page, size);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @GetMapping("/content/featured")
    public ResponseEntity<ApiResponse<List<ContentDto.Summary>>> getFeaturedContent() {
        List<ContentDto.Summary> content = contentService.getFeaturedContent();
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @GetMapping("/content/new-releases")
    public ResponseEntity<ApiResponse<Page<ContentDto.Summary>>> getNewReleases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ContentDto.Summary> content = contentService.getNewReleases(page, size);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @GetMapping("/content/originals")
    public ResponseEntity<ApiResponse<Page<ContentDto.Summary>>> getOriginals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ContentDto.Summary> content = contentService.getOriginals(page, size);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @GetMapping("/content/kids")
    public ResponseEntity<ApiResponse<Page<ContentDto.Summary>>> getKidsContent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ContentDto.Summary> content = contentService.getKidsContent(page, size);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @GetMapping("/genres")
    public ResponseEntity<ApiResponse<List<ContentDto.GenreResponse>>> getGenres() {
        List<ContentDto.GenreResponse> genres = contentService.getAllGenres();
        return ResponseEntity.ok(ApiResponse.success(genres));
    }

    @GetMapping("/genres/{slug}/content")
    public ResponseEntity<ApiResponse<Page<ContentDto.Summary>>> getContentByGenre(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ContentDto.Summary> content = contentService.getContentByGenre(slug, page, size);
        return ResponseEntity.ok(ApiResponse.success(content));
    }
}
