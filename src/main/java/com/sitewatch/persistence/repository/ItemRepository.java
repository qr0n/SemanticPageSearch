package com.sitewatch.persistence.repository;

import com.sitewatch.persistence.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ItemRepository
 *
 * Spring Data JPA repository for {@link Item} entities.
 * Provides query methods for retrieving discovered content items.
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

    /**
     * Find all items for a specific source, ordered by published date descending.
     *
     * @param sourceId the UUID of the source
     * @param pageable pagination and sorting parameters
     * @return page of items, never null (may be empty)
     */
    Page<Item> findBySourceIdOrderByPublishedAtDesc(UUID sourceId, Pageable pageable);

    /**
     * Find all items discovered after a specific timestamp.
     *
     * @param after find items discovered after this instant
     * @param pageable pagination and sorting parameters
     * @return page of recent items
     */
    Page<Item> findByDiscoveredAtAfterOrderByDiscoveredAtDesc(Instant after, Pageable pageable);

    /**
     * Find an item by source ID and link (unique constraint).
     *
     * @param sourceId the source UUID
     * @param link the item URL
     * @return optional containing the item if found
     */
    Optional<Item> findBySourceIdAndLink(UUID sourceId, String link);

    /**
     * Find items by content hash (for deduplication across sources).
     *
     * @param contentHash the SHA-256 hash
     * @return list of items with matching hash
     */
    List<Item> findByContentHash(String contentHash);

    /**
     * Check if an item exists for a given source and link.
     *
     * @param sourceId the source UUID
     * @param link the item URL
     * @return true if exists, false otherwise
     */
    boolean existsBySourceIdAndLink(UUID sourceId, String link);

    /**
     * Count items discovered for a specific source.
     *
     * @param sourceId the source UUID
     * @return count of items
     */
    long countBySourceId(UUID sourceId);
}
