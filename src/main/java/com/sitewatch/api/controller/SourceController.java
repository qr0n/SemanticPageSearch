package com.sitewatch.api.controller;

import com.sitewatch.api.dto.CreateSourceRequest;
import com.sitewatch.api.dto.SourceDTO;
import com.sitewatch.service.SourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * SourceController
 *
 * REST API endpoints for managing website sources.
 * Provides CRUD operations for creating, retrieving, updating, and deleting
 * sources.
 *
 * Base path: /api/v1/sources
 * Authentication: Required (JWT or API key)
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/v1/sources")
@RequiredArgsConstructor
@Slf4j
public class SourceController {

    private final SourceService sourceService;

    /**
     * Create a new monitored source.
     *
     * @param request the source creation request with validation
     * @return HTTP 201 Created with the created source, or 400 Bad Request if
     *         validation fails
     */
    @PostMapping
    public ResponseEntity<SourceDTO> createSource(@Valid @RequestBody CreateSourceRequest request) {
        log.info("POST /api/v1/sources: name={}, url={}", request.getName(), request.getUrl());
        SourceDTO created = sourceService.createSource(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get all monitored sources.
     *
     * @return HTTP 200 OK with list of all sources
     */
    @GetMapping
    public ResponseEntity<List<SourceDTO>> getAllSources() {
        log.info("GET /api/v1/sources");
        List<SourceDTO> sources = sourceService.getAllSources();
        return ResponseEntity.ok(sources);
    }

    /**
     * Get a specific source by ID.
     *
     * @param id the source UUID
     * @return HTTP 200 OK with the source, or 404 Not Found if not exists
     */
    @GetMapping("/{id}")
    public ResponseEntity<SourceDTO> getSourceById(@PathVariable UUID id) {
        log.info("GET /api/v1/sources/{}", id);
        SourceDTO source = sourceService.getSourceById(id);
        return ResponseEntity.ok(source);
    }

    /**
     * Delete a source by ID.
     *
     * @param id the source UUID
     * @return HTTP 204 No Content if successful, or 404 Not Found if not exists
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSource(@PathVariable UUID id) {
        log.info("DELETE /api/v1/sources/{}", id);
        sourceService.deleteSource(id);
        return ResponseEntity.noContent().build();
    }
}
