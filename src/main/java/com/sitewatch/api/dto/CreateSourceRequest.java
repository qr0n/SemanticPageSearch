package com.sitewatch.api.dto;

import com.sitewatch.persistence.entity.Source;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CreateSourceRequest
 *
 * Request DTO for creating a new monitored source.
 * Contains validation constraints for input data.
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSourceRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @NotBlank(message = "URL is required")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    private String url;

    @NotNull(message = "Mode is required")
    private Source.SourceMode mode;

    private String[] filterKeywords;

    private String[] filterRegex;

    @Min(value = 1, message = "Interval must be at least 1 minute")
    @Max(value = 10080, message = "Interval cannot exceed 1 week (10080 minutes)")
    private Integer intervalMinutes = 60;
}
