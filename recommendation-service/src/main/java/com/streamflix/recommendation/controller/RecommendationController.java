package com.streamflix.recommendation.controller;

import com.streamflix.common.dto.ApiResponse;
import com.streamflix.common.security.SecurityConstants;
import com.streamflix.recommendation.dto.RecommendationDto;
import com.streamflix.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<RecommendationDto.TrendingItem>>> getTrending(
            @RequestParam(defaultValue = "OVERALL") String category,
            @RequestParam(defaultValue = "20") int limit) {

        List<RecommendationDto.TrendingItem> trending = recommendationService.getTrending(category, limit);
        return ResponseEntity.ok(ApiResponse.success(trending));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<RecommendationDto.RecommendedItem>>> getRecommendations(
            @RequestHeader(SecurityConstants.PROFILE_ID_HEADER) String profileId,
            @RequestParam(defaultValue = "20") int limit) {

        List<RecommendationDto.RecommendedItem> recommendations =
                recommendationService.getPersonalizedRecommendations(UUID.fromString(profileId), limit);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    @GetMapping("/recommendations/because-you-watched")
    public ResponseEntity<ApiResponse<List<RecommendationDto.BecauseYouWatched>>> getBecauseYouWatched(
            @RequestHeader(SecurityConstants.PROFILE_ID_HEADER) String profileId,
            @RequestParam(defaultValue = "3") int limit) {

        List<RecommendationDto.BecauseYouWatched> recommendations =
                recommendationService.getBecauseYouWatched(UUID.fromString(profileId), limit);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }
}
