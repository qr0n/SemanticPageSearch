package com.sitewatch.api.controller;

import com.sitewatch.api.dto.ItemDTO;
import com.sitewatch.persistence.entity.Item;
import com.sitewatch.persistence.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for item operations.
 */
@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;

    /**
     * Retrieves all items, optionally filtered by source.
     *
     * @param sourceId optional source UUID filter
     * @return list of items
     */
    @GetMapping
    public ResponseEntity<List<ItemDTO>> getItems(
            @RequestParam(required = false) UUID sourceId) {

        List<Item> items = sourceId != null
                ? itemRepository.findBySourceIdOrderByPublishedAtDesc(
                        sourceId, org.springframework.data.domain.Pageable.unpaged()).getContent()
                : itemRepository.findAll();

        List<ItemDTO> dtos = items.stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    /**
     * Retrieves a specific item by ID.
     *
     * @param id the item UUID
     * @return item details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItem(@PathVariable UUID id) {
        return itemRepository.findById(id)
                .map(item -> ResponseEntity.ok(toDTO(item)))
                .orElse(ResponseEntity.notFound().build());
    }

    private ItemDTO toDTO(Item item) {
        return new ItemDTO(
                item.getId(),
                item.getSource().getId(),
                item.getSource().getName(),
                item.getTitle(),
                item.getLink(),
                item.getSummary(),
                item.getPublishedAt(),
                item.getDiscoveredAt(),
                item.getContentHash());
    }
}
