package com.streamflix.search.service;

import com.streamflix.search.document.ContentDocument;
import com.streamflix.search.dto.SearchDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Search service using Elasticsearch.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * Full-text search for content.
     */
    public SearchDto.SearchResponse search(SearchDto.SearchRequest request) {
        log.debug("Searching for: {}", request.getQuery());

        long startTime = System.currentTimeMillis();

        // Build multi-match query
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            // Main search on title and description
                            b.should(s -> s
                                    .match(m -> m
                                            .field("title")
                                            .query(request.getQuery())
                                            .boost(3.0f)));
                            b.should(s -> s
                                    .match(m -> m
                                            .field("description")
                                            .query(request.getQuery())
                                            .boost(1.0f)));
                            b.should(s -> s
                                    .match(m -> m
                                            .field("cast")
                                            .query(request.getQuery())
                                            .boost(2.0f)));

                            // Apply filters
                            if (request.getContentType() != null) {
                                b.filter(f -> f
                                        .term(t -> t
                                                .field("contentType")
                                                .value(request.getContentType())));
                            }
                            if (request.getGenre() != null) {
                                b.filter(f -> f
                                        .term(t -> t
                                                .field("genres")
                                                .value(request.getGenre())));
                            }
                            if (request.getReleaseYear() != null) {
                                b.filter(f -> f
                                        .term(t -> t
                                                .field("releaseYear")
                                                .value(request.getReleaseYear())));
                            }

                            return b;
                        }))
                .withPageable(PageRequest.of(request.getPage(), request.getSize()))
                .build();

        SearchHits<ContentDocument> hits = elasticsearchOperations.search(query, ContentDocument.class);

        long responseTimeMs = System.currentTimeMillis() - startTime;

        List<SearchDto.SearchResult> results = hits.getSearchHits().stream()
                .map(this::toSearchResult)
                .collect(Collectors.toList());

        return SearchDto.SearchResponse.builder()
                .query(request.getQuery())
                .results(results)
                .totalResults(hits.getTotalHits())
                .page(request.getPage())
                .size(request.getSize())
                .responseTimeMs(responseTimeMs)
                .build();
    }

    /**
     * Autocomplete suggestions.
     */
    public List<SearchDto.AutocompleteSuggestion> autocomplete(String query, int limit) {
        log.debug("Autocomplete for: {}", query);

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .match(m -> m
                                .field("titleAutocomplete")
                                .query(query)))
                .withPageable(PageRequest.of(0, limit))
                .build();

        SearchHits<ContentDocument> hits = elasticsearchOperations.search(nativeQuery, ContentDocument.class);

        return hits.getSearchHits().stream()
                .map(hit -> SearchDto.AutocompleteSuggestion.builder()
                        .id(hit.getContent().getId())
                        .title(hit.getContent().getTitle())
                        .contentType(hit.getContent().getContentType())
                        .posterUrl(hit.getContent().getPosterUrl())
                        .build())
                .collect(Collectors.toList());
    }

    private SearchDto.SearchResult toSearchResult(SearchHit<ContentDocument> hit) {
        ContentDocument doc = hit.getContent();
        return SearchDto.SearchResult.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .slug(doc.getSlug())
                .contentType(doc.getContentType())
                .description(doc.getDescription())
                .genres(doc.getGenres())
                .releaseYear(doc.getReleaseYear())
                .maturityRating(doc.getMaturityRating())
                .posterUrl(doc.getPosterUrl())
                .averageRating(doc.getAverageRating())
                .score(hit.getScore())
                .build();
    }
}
