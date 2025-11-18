package com.sitewatch.service;

import com.sitewatch.api.dto.CreateSourceRequest;
import com.sitewatch.api.dto.SourceDTO;
import com.sitewatch.persistence.entity.Source;
import com.sitewatch.persistence.repository.SourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SourceService
 *
 * Business logic layer for managing website sources.
 * Handles CRUD operations, validation, and mapping between entities and DTOs.
 *
 * Key responsibilities:
 * - Create, update, and delete sources
 * - Validate source configuration before persistence
 * - Convert between entity and DTO representations
 * - Enforce business rules (unique URLs, valid intervals)
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SourceService {

    private final SourceRepository sourceRepository;

    /**
     * Create a new monitored source.
     *
     * Validates that no source with the same URL already exists.
     * Defaults to 60-minute interval if not specified.
     *
     * @param request the source creation request
     * @return the created source as DTO
     * @throws IllegalArgumentException if URL already exists or validation fails
     */
    @Transactional
    public SourceDTO createSource(CreateSourceRequest request) {
        log.info("Creating source: name={}, url={}", request.getName(), request.getUrl());

        if (sourceRepository.existsByUrl(request.getUrl())) {
            throw new IllegalArgumentException("Source with URL already exists: " + request.getUrl());
        }

        Source source = Source.builder()
            .name(request.getName())
            .url(request.getUrl())
            .mode(request.getMode())
            .filterKeywords(request.getFilterKeywords())
            .filterRegex(request.getFilterRegex())
            .intervalMinutes(request.getIntervalMinutes() != null ? request.getIntervalMinutes() : 60)
            .build();

        Source saved = sourceRepository.save(source);
        log.info("Source created successfully: id={}, name={}", saved.getId(), saved.getName());

        return toDTO(saved);
    }

    /**
     * Get all sources.
     *
     * @return list of all sources as DTOs
     */
    @Transactional(readOnly = true)
    public List<SourceDTO> getAllSources() {
        return sourceRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get a source by ID.
     *
     * @param id the source UUID
     * @return the source as DTO
     * @throws IllegalArgumentException if source not found
     */
    @Transactional(readOnly = true)
    public SourceDTO getSourceById(UUID id) {
        Source source = sourceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Source not found: " + id));
        return toDTO(source);
    }

    /**
     * Delete a source by ID.
     *
     * @param id the source UUID
     * @throws IllegalArgumentException if source not found
     */
    @Transactional
    public void deleteSource(UUID id) {
        if (!sourceRepository.existsById(id)) {
            throw new IllegalArgumentException("Source not found: " + id);
        }
        sourceRepository.deleteById(id);
        log.info("Source deleted: id={}", id);
    }

    /**
     * Convert Source entity to DTO.
     *
     * @param source the source entity
     * @return the source DTO
     */
    private SourceDTO toDTO(Source source) {
        return SourceDTO.builder()
            .id(source.getId())
            .name(source.getName())
            .url(source.getUrl())
            .mode(source.getMode())
            .filterKeywords(source.getFilterKeywords())
            .filterRegex(source.getFilterRegex())
            .intervalMinutes(source.getIntervalMinutes())
            .lastChecked(source.getLastChecked())
            .createdAt(source.getCreatedAt())
            .updatedAt(source.getUpdatedAt())
            .build();
    }
}
