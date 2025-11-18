package com.sitewatch.crawler;

import com.sitewatch.persistence.entity.Item;
import com.sitewatch.persistence.entity.Source;
import com.sitewatch.persistence.repository.ItemRepository;
import com.sitewatch.persistence.repository.SourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Main crawler service that coordinates fetching and scraping operations.
 * Handles both RSS feeds and HTML pages, applies filters, and stores items.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerService {

    private final FeedFetcher feedFetcher;
    private final HtmlScraper htmlScraper;
    private final SourceRepository sourceRepository;
    private final ItemRepository itemRepository;

    /**
     * Crawls a source and stores discovered items.
     *
     * @param sourceId the UUID of the source to crawl
     * @return number of new items discovered
     */
    @Transactional
    public int crawlSource(UUID sourceId) {
        Source source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + sourceId));

        log.info("Crawling source: {} ({})", source.getName(), source.getMode());

        List<Item> newItems = new ArrayList<>();

        try {
            switch (source.getMode()) {
                case RSS -> newItems = crawlRssFeed(source);
                case HTML -> newItems = crawlHtmlPage(source);
                case AUTO -> newItems = crawlAuto(source);
            }

            // Update source last checked timestamp
            source.setLastChecked(Instant.now());
            sourceRepository.save(source);

            log.info("Crawled source {}: discovered {} new items", source.getName(), newItems.size());
            return newItems.size();

        } catch (Exception e) {
            log.error("Error crawling source {}: {}", source.getName(), e.getMessage(), e);
            source.setLastChecked(Instant.now());
            sourceRepository.save(source);
            return 0;
        }
    }

    /**
     * Crawls an RSS feed source.
     */
    private List<Item> crawlRssFeed(Source source) throws Exception {
        List<FeedFetcher.FeedItem> feedItems = feedFetcher.fetchFeed(source.getUrl());
        List<Item> newItems = new ArrayList<>();

        for (FeedFetcher.FeedItem feedItem : feedItems) {
            // Check if item already exists
            if (itemRepository.existsByLink(feedItem.link())) {
                continue;
            }

            // Apply filters
            if (!matchesFilters(source, feedItem.title(), feedItem.content())) {
                continue;
            }

            // Create and save item
            Item item = new Item();
            item.setSource(source);
            item.setTitle(feedItem.title());
            item.setLink(feedItem.link());
            String content = feedItem.content() != null ? feedItem.content() : feedItem.description();
            item.setSummary(createSnippet(content));
            item.setContentHash(HtmlScraper.generateHash(content));
            item.setPublishedAt(feedItem.publishedDate());
            item.setDiscoveredAt(Instant.now());

            itemRepository.save(item);
            newItems.add(item);
        }

        return newItems;
    }

    /**
     * Crawls an HTML page source.
     */
    private List<Item> crawlHtmlPage(Source source) throws Exception {
        // Check if item already exists by URL
        if (itemRepository.existsByLink(source.getUrl())) {
            log.debug("HTML page already scraped: {}", source.getUrl());
            return List.of();
        }

        HtmlScraper.ScrapedContent scraped = htmlScraper.scrape(source.getUrl());

        // Apply filters
        if (!matchesFilters(source, scraped.title(), scraped.content())) {
            return List.of();
        }

        // Create and save item
        Item item = new Item();
        item.setSource(source);
        item.setTitle(scraped.title());
        item.setLink(source.getUrl());
        item.setSummary(scraped.snippet());
        item.setContentHash(scraped.contentHash());
        item.setDiscoveredAt(Instant.now());

        itemRepository.save(item);
        return List.of(item);
    }

    /**
     * Auto-detects whether to use RSS or HTML mode.
     */
    private List<Item> crawlAuto(Source source) throws Exception {
        // Try RSS first
        try {
            return crawlRssFeed(source);
        } catch (Exception e) {
            log.debug("RSS parsing failed, trying HTML mode: {}", e.getMessage());
            return crawlHtmlPage(source);
        }
    }

    /**
     * Checks if content matches the source's keyword and regex filters.
     */
    private boolean matchesFilters(Source source, String title, String content) {
        String combinedText = (title != null ? title : "") + " " + (content != null ? content : "");

        // Check keyword filters (case-insensitive substring match)
        String[] keywords = source.getFilterKeywords();
        if (keywords != null && keywords.length > 0) {
            boolean matchesAny = false;
            for (String keyword : keywords) {
                if (combinedText.toLowerCase().contains(keyword.toLowerCase())) {
                    matchesAny = true;
                    break;
                }
            }

            if (!matchesAny) {
                log.debug("Content does not match keyword filters");
                return false;
            }
        }

        // Check regex filter
        String[] regexPatterns = source.getFilterRegex();
        if (regexPatterns != null && regexPatterns.length > 0) {
            boolean matchesAny = false;
            for (String regex : regexPatterns) {
                try {
                    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                    if (pattern.matcher(combinedText).find()) {
                        matchesAny = true;
                        break;
                    }
                } catch (Exception e) {
                    log.warn("Invalid regex pattern: {}", regex, e);
                    // If regex is invalid, ignore it and continue
                }
            }

            if (!matchesAny) {
                log.debug("Content does not match regex filters");
                return false;
            }
        }

        return true;
    }

    /**
     * Creates a snippet from content (first 200 characters).
     */
    private String createSnippet(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String text = org.jsoup.Jsoup.parse(content).text();
        if (text.length() <= 200) {
            return text;
        }
        return text.substring(0, 197) + "...";
    }
}
