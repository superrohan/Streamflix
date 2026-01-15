package com.streamflix.search.controller;

import com.streamflix.common.dto.ApiResponse;
import com.streamflix.search.dto.SearchDto;
import com.streamflix.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<SearchDto.SearchResponse>> search(
            @RequestParam String q,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer releaseYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        SearchDto.SearchRequest request = SearchDto.SearchRequest.builder()
                .query(q)
                .contentType(contentType)
                .genre(genre)
                .releaseYear(releaseYear)
                .page(page)
                .size(size)
                .build();

        SearchDto.SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<SearchDto.AutocompleteSuggestion>>> autocomplete(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {

        List<SearchDto.AutocompleteSuggestion> suggestions = searchService.autocomplete(q, limit);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }
}
