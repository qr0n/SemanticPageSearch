package com.sitewatch.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * ItemDTO
 *
 * Data transfer object for Item entity.
 * Used in API responses to expose discovered content items.
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {
    private UUID id;
    private UUID sourceId;
    private String sourceName;
    private String title;
    private String link;
    private String summary;
    private Instant publishedAt;
    private Instant discoveredAt;
    private String contentHash;
}
