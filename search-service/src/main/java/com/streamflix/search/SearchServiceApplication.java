package com.streamflix.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Streamflix Search Service.
 *
 * Responsibilities:
 * - Full-text search for content
 * - Autocomplete suggestions
 * - Search result ranking
 * - Index synchronization from content events
 *
 * Elasticsearch Features Used:
 * - Multi-match queries for fuzzy search
 * - Edge n-gram tokenizer for autocomplete
 * - Boosting for title matches
 *
 * @author Streamflix Engineering
 */
@SpringBootApplication
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
