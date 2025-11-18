package com.sitewatch.api.controller;

import com.sitewatch.crawler.CrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for triggering crawler operations.
 */
@RestController
@RequestMapping("/api/v1/crawler")
@RequiredArgsConstructor
public class CrawlerController {

    private final CrawlerService crawlerService;

    /**
     * Triggers an immediate crawl of a specific source.
     *
     * @param sourceId the source UUID
     * @return response with number of items discovered
     */
    @PostMapping("/sources/{sourceId}/crawl")
    public ResponseEntity<Map<String, Object>> crawlSource(@PathVariable UUID sourceId) {
        int itemsDiscovered = crawlerService.crawlSource(sourceId);

        return ResponseEntity.ok(Map.of(
                "sourceId", sourceId,
                "itemsDiscovered", itemsDiscovered,
                "status", "completed"));
    }
}
