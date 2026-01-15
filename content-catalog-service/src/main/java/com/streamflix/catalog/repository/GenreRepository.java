package com.streamflix.catalog.repository;

import com.streamflix.catalog.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GenreRepository extends JpaRepository<Genre, UUID> {

    Optional<Genre> findBySlug(String slug);

    @Query("SELECT g FROM Genre g WHERE g.isActive = true ORDER BY g.displayOrder ASC")
    List<Genre> findAllActive();
}
