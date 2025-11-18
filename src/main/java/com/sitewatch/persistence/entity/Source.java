package com.sitewatch.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Source
 *
 * Represents a monitored website or feed source with associated filter rules
 * and scheduling configuration.
 *
 * Key responsibilities:
 * - Store source URL and monitoring mode (RSS/HTML/AUTO)
 * - Maintain filter keywords and regex patterns for content matching
 * - Track scheduling interval and last check timestamp
 * - Persist source lifecycle metadata
 *
 * Persistence: Mapped to the 'sources' table in PostgreSQL with indexes on
 * url and last_checked for efficient querying.
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@Entity
@Table(name = "sources", indexes = {
    @Index(name = "idx_sources_url", columnList = "url"),
    @Index(name = "idx_sources_last_checked", columnList = "last_checked")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Source {

    /**
     * Unique identifier for the source.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Human-readable name for the source.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Source URL to monitor (RSS feed or webpage).
     */
    @Column(nullable = false)
    private String url;

    /**
     * Monitoring mode: RSS, HTML, or AUTO (detect best method).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceMode mode;

    /**
     * Array of keywords for content filtering (case-insensitive substring matching).
     */
    @Column(name = "filter_keywords", columnDefinition = "text[]")
    private String[] filterKeywords;

    /**
     * Array of regex patterns for advanced content filtering.
     */
    @Column(name = "filter_regex", columnDefinition = "text[]")
    private String[] filterRegex;

    /**
     * Check interval in minutes.
     */
    @Column(name = "interval_minutes", nullable = false)
    @Builder.Default
    private Integer intervalMinutes = 60;

    /**
     * Timestamp of the last successful check.
     */
    @Column(name = "last_checked")
    private Instant lastChecked;

    /**
     * Timestamp when this source was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp when this source was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * SourceMode
     *
     * Enumeration of supported monitoring modes for sources.
     */
    public enum SourceMode {
        /**
         * Parse as RSS/Atom feed.
         */
        RSS,

        /**
         * Parse HTML content.
         */
        HTML,

        /**
         * Auto-detect best parsing method.
         */
        AUTO
    }
}
