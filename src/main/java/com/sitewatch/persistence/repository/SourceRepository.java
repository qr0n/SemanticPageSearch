package com.sitewatch.persistence.repository;

import com.sitewatch.persistence.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SourceRepository
 *
 * Spring Data JPA repository for {@link Source} entities.
 * Provides query methods for managing monitored sources.
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@Repository
public interface SourceRepository extends JpaRepository<Source, UUID> {

    /**
     * Find a source by its URL.
     *
     * @param url the source URL
     * @return optional containing the source if found
     */
    Optional<Source> findByUrl(String url);

    /**
     * Find all sources that need checking (last checked before the given timestamp).
     *
     * @param before find sources last checked before this instant
     * @return list of sources needing checks
     */
    List<Source> findByLastCheckedBeforeOrLastCheckedIsNull(Instant before);

    /**
     * Find all sources by name containing the given string (case-insensitive).
     *
     * @param name the name fragment to search for
     * @return list of matching sources
     */
    List<Source> findByNameContainingIgnoreCase(String name);

    /**
     * Check if a source with the given URL exists.
     *
     * @param url the source URL
     * @return true if exists, false otherwise
     */
    boolean existsByUrl(String url);
}
