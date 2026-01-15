package com.streamflix.recommendation.repository;

import com.streamflix.recommendation.entity.TrendingContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrendingContentRepository extends JpaRepository<TrendingContent, UUID> {

    @Query(value = "SELECT * FROM trending_content WHERE category = :category ORDER BY rank ASC LIMIT :limit", nativeQuery = true)
    List<TrendingContent> findByCategory(@Param("category") String category, @Param("limit") int limit);

    @Modifying
    @Query("DELETE FROM TrendingContent t WHERE t.category = :category")
    void deleteByCategory(@Param("category") String category);
}
