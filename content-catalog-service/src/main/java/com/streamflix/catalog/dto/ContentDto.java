package com.streamflix.catalog.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTOs for content operations.
 */
public class ContentDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private String id;
        private String contentType;
        private String title;
        private String originalTitle;
        private String slug;
        private String description;
        private String shortDescription;
        private Integer releaseYear;
        private Integer runtimeMinutes;
        private String maturityRating;
        private String posterUrl;
        private String backdropUrl;
        private String trailerUrl;
        private List<String> genres;
        private BigDecimal averageRating;
        private Integer ratingCount;
        private Boolean isOriginal;
        private Boolean isFeatured;
        private List<SeasonResponse> seasons;
        private List<CastMember> cast;
        private List<CrewMember> crew;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private String id;
        private String contentType;
        private String title;
        private String slug;
        private String shortDescription;
        private Integer releaseYear;
        private String maturityRating;
        private String posterUrl;
        private BigDecimal averageRating;
        private Boolean isOriginal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeasonResponse {
        private String id;
        private Integer seasonNumber;
        private String title;
        private String description;
        private String posterUrl;
        private Integer episodeCount;
        private List<EpisodeResponse> episodes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EpisodeResponse {
        private String id;
        private Integer episodeNumber;
        private String title;
        private String description;
        private Integer runtimeMinutes;
        private String thumbnailUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenreResponse {
        private String id;
        private String name;
        private String slug;
        private String description;
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CastMember {
        private String id;
        private String name;
        private String character;
        private String photoUrl;
        private Boolean isLead;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrewMember {
        private String id;
        private String name;
        private String role;
        private String photoUrl;
    }
}
