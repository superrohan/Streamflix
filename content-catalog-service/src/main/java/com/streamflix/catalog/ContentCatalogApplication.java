package com.streamflix.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Streamflix Content Catalog Service.
 *
 * Responsibilities:
 * - Manage movie and series catalog
 * - Handle content metadata (cast, crew, descriptions)
 * - Organize content by genres and categories
 * - Provide read-optimized APIs with caching
 *
 * Design Principles:
 * - Read-heavy optimization (cache-aside pattern)
 * - Eventual consistency for catalog updates
 * - Denormalized views for fast retrieval
 *
 * @author Streamflix Engineering
 */
@SpringBootApplication
@EnableCaching
public class ContentCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentCatalogApplication.class, args);
    }
}
