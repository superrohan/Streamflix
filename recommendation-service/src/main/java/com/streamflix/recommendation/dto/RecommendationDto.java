package com.streamflix.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class RecommendationDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendingItem {
        private String contentId;
        private Integer rank;
        private Long viewCount24h;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedItem {
        private String contentId;
        private Double score;
        private String reason;
        private String reasonContentId;  // For "Because you watched X"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BecauseYouWatched {
        private String sourceContentId;
        private String sourceContentTitle;
        private List<RecommendedItem> recommendations;
    }
}
