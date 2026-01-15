package com.streamflix.playback.controller;

import com.streamflix.common.dto.ApiResponse;
import com.streamflix.common.security.SecurityConstants;
import com.streamflix.playback.dto.PlaybackDto;
import com.streamflix.playback.service.PlaybackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/playback")
@RequiredArgsConstructor
public class PlaybackController {

    private final PlaybackService playbackService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<PlaybackDto.StartResponse>> startPlayback(
            @RequestHeader(SecurityConstants.PROFILE_ID_HEADER) String profileId,
            @Valid @RequestBody PlaybackDto.StartRequest request) {

        PlaybackDto.StartResponse response = playbackService.startPlayback(
                UUID.fromString(profileId), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/progress")
    public ResponseEntity<ApiResponse<Void>> updateProgress(
            @RequestHeader(SecurityConstants.PROFILE_ID_HEADER) String profileId,
            @Valid @RequestBody PlaybackDto.ProgressUpdate request) {

        playbackService.updateProgress(UUID.fromString(profileId), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/continue-watching")
    public ResponseEntity<ApiResponse<List<PlaybackDto.ContinueWatching>>> getContinueWatching(
            @RequestHeader(SecurityConstants.PROFILE_ID_HEADER) String profileId,
            @RequestParam(defaultValue = "10") int limit) {

        List<PlaybackDto.ContinueWatching> items = playbackService.getContinueWatching(
                UUID.fromString(profileId), limit);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<PlaybackDto.WatchHistoryItem>>> getWatchHistory(
            @RequestHeader(SecurityConstants.PROFILE_ID_HEADER) String profileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<PlaybackDto.WatchHistoryItem> history = playbackService.getWatchHistory(
                UUID.fromString(profileId), page, size);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
