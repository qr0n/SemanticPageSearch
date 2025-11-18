package com.sitewatch.api.dto;

import com.sitewatch.persistence.entity.Source;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * SourceDTO
 *
 * Data transfer object for Source entity.
 * Used in API responses to expose source information.
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceDTO {
    private UUID id;
    private String name;
    private String url;
    private Source.SourceMode mode;
    private String[] filterKeywords;
    private String[] filterRegex;
    private Integer intervalMinutes;
    private Instant lastChecked;
    private Instant createdAt;
    private Instant updatedAt;
}
