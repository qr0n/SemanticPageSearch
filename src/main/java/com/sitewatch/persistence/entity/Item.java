package com.sitewatch.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Item
 *
 * Represents a discovered content item from a monitored source.
 * Items are deduplicated by link and content hash.
 *
 * Key responsibilities:
 * - Store discovered content metadata (title, link, summary)
 * - Track published and discovery timestamps
 * - Maintain content hash for deduplication
 * - Reference parent source
 *
 * Persistence: Mapped to the 'items' table with unique constraint on
 * (source_id, link) and indexes on content_hash and published_at.
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@Entity
@Table(name = "items",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_items_source_link", columnNames = {"source_id", "link"})
    },
    indexes = {
        @Index(name = "idx_items_content_hash", columnList = "content_hash"),
        @Index(name = "idx_items_published_at", columnList = "published_at"),
        @Index(name = "idx_items_discovered_at", columnList = "discovered_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    /**
     * Unique identifier for the item.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Reference to the source that discovered this item.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    /**
     * Title of the discovered content.
     */
    @Column(columnDefinition = "text")
    private String title;

    /**
     * URL link to the content.
     */
    @Column(nullable = false, columnDefinition = "text")
    private String link;

    /**
     * Summary or snippet of the content.
     */
    @Column(columnDefinition = "text")
    private String summary;

    /**
     * Original published timestamp from the source (if available).
     */
    @Column(name = "published_at")
    private Instant publishedAt;

    /**
     * Timestamp when this item was discovered by SiteWatch.
     */
    @CreationTimestamp
    @Column(name = "discovered_at", nullable = false, updatable = false)
    private Instant discoveredAt;

    /**
     * SHA-256 hash of the content for deduplication.
     */
    @Column(name = "content_hash", columnDefinition = "text")
    private String contentHash;
}
