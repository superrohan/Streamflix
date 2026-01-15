package com.streamflix.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class SearchDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequest {
        private String query;
        private String contentType;
        private String genre;
        private Integer releaseYear;
        private String maturityRating;
        @Builder.Default
        private int page = 0;
        @Builder.Default
        private int size = 20;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResponse {
        private String query;
        private List<SearchResult> results;
        private long totalResults;
        private int page;
        private int size;
        private long responseTimeMs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResult {
        private String id;
        private String title;
        private String slug;
        private String contentType;
        private String description;
        private List<String> genres;
        private Integer releaseYear;
        private String maturityRating;
        private String posterUrl;
        private BigDecimal averageRating;
        private float score;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AutocompleteSuggestion {
        private String id;
        private String title;
        private String contentType;
        private String posterUrl;
    }
}
