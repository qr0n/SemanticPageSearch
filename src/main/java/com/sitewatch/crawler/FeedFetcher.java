package com.sitewatch.crawler;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Fetches and parses RSS/Atom feeds using Rome library.
 * Extracts feed entries and converts them to FeedItem objects.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeedFetcher {

    private final HttpClient httpClient;

    /**
     * Represents a parsed feed item with content and metadata.
     */
    public record FeedItem(
            String title,
            String link,
            String description,
            String content,
            Instant publishedDate,
            String author) {
    }

    /**
     * Fetches and parses an RSS/Atom feed from the given URL.
     *
     * @param feedUrl the URL of the RSS/Atom feed
     * @return list of parsed feed items
     * @throws IOException if network or parsing error occurs
     */
    public List<FeedItem> fetchFeed(String feedUrl) throws IOException {
        log.debug("Fetching feed from: {}", feedUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(feedUrl))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "SiteWatch/1.0 (RSS Reader)")
                .GET()
                .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode() + " fetching feed: " + feedUrl);
            }

            return parseFeed(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while fetching feed", e);
        }
    }

    /**
     * Parses feed content into FeedItem objects.
     *
     * @param feedContent raw feed content as bytes
     * @return list of parsed feed items
     * @throws IOException if parsing fails
     */
    private List<FeedItem> parseFeed(byte[] feedContent) throws IOException {
        try (XmlReader reader = new XmlReader(new java.io.ByteArrayInputStream(feedContent))) {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(reader);

            List<FeedItem> items = new ArrayList<>();
            for (SyndEntry entry : feed.getEntries()) {
                items.add(convertEntry(entry));
            }

            log.debug("Parsed {} items from feed", items.size());
            return items;
        } catch (Exception e) {
            throw new IOException("Failed to parse feed", e);
        }
    }

    /**
     * Converts a Rome SyndEntry to our FeedItem record.
     *
     * @param entry the Rome feed entry
     * @return converted FeedItem
     */
    private FeedItem convertEntry(SyndEntry entry) {
        String content = null;
        if (entry.getContents() != null && !entry.getContents().isEmpty()) {
            content = entry.getContents().get(0).getValue();
        } else if (entry.getDescription() != null) {
            content = entry.getDescription().getValue();
        }

        Date pubDate = entry.getPublishedDate() != null
                ? entry.getPublishedDate()
                : entry.getUpdatedDate();

        Instant publishedDate = pubDate != null ? pubDate.toInstant() : Instant.now();

        return new FeedItem(
                entry.getTitle(),
                entry.getLink(),
                entry.getDescription() != null ? entry.getDescription().getValue() : null,
                content,
                publishedDate,
                entry.getAuthor());
    }
}
