package com.sitewatch.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

/**
 * Scrapes and parses HTML content from web pages.
 * Implements content extraction heuristics and sanitization.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HtmlScraper {

    private final HttpClient httpClient;

    /**
     * Represents scraped HTML content with metadata.
     */
    public record ScrapedContent(
            String title,
            String content,
            String contentHash,
            String description,
            String author,
            String snippet) {
    }

    /**
     * Scrapes content from a web page URL.
     *
     * @param url the URL to scrape
     * @return scraped content with metadata
     * @throws IOException if network or parsing error occurs
     */
    public ScrapedContent scrape(String url) throws IOException {
        log.debug("Scraping content from: {}", url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "Mozilla/5.0 (compatible; SiteWatch/1.0)")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode() + " scraping: " + url);
            }

            return parseHtml(response.body(), url);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while scraping", e);
        }
    }

    /**
     * Parses HTML content and extracts relevant information.
     *
     * @param html    raw HTML content
     * @param baseUrl base URL for resolving relative links
     * @return parsed content
     */
    private ScrapedContent parseHtml(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);

        // Extract title
        String title = extractTitle(doc);

        // Extract main content using multiple strategies
        String content = extractMainContent(doc);

        // Extract meta description
        String description = extractMetaDescription(doc);

        // Extract author
        String author = extractAuthor(doc);

        // Create snippet (first 200 chars of content)
        String snippet = createSnippet(content);

        // Generate content hash
        String contentHash = generateContentHash(content);

        log.debug("Scraped content from {}: title={}, contentLength={}",
                baseUrl, title, content.length());

        return new ScrapedContent(title, content, contentHash, description, author, snippet);
    }

    /**
     * Extracts the page title using multiple strategies.
     */
    private String extractTitle(Document doc) {
        // Try Open Graph title
        Element ogTitle = doc.selectFirst("meta[property=og:title]");
        if (ogTitle != null && !ogTitle.attr("content").isBlank()) {
            return ogTitle.attr("content");
        }

        // Try Twitter title
        Element twitterTitle = doc.selectFirst("meta[name=twitter:title]");
        if (twitterTitle != null && !twitterTitle.attr("content").isBlank()) {
            return twitterTitle.attr("content");
        }

        // Fall back to HTML title
        return doc.title();
    }

    /**
     * Extracts main content using multiple heuristics.
     */
    private String extractMainContent(Document doc) {
        // Strategy 1: Look for semantic HTML5 elements
        Element article = doc.selectFirst("article");
        if (article != null) {
            return sanitizeHtml(article.html());
        }

        Element main = doc.selectFirst("main");
        if (main != null) {
            return sanitizeHtml(main.html());
        }

        // Strategy 2: Look for common content containers
        Element content = doc.selectFirst(".content, .post-content, .article-content, .entry-content");
        if (content != null) {
            return sanitizeHtml(content.html());
        }

        // Strategy 3: Find the longest text block
        Elements paragraphs = doc.select("p");
        StringBuilder longestBlock = new StringBuilder();
        StringBuilder currentBlock = new StringBuilder();

        for (Element p : paragraphs) {
            String text = p.text();
            if (text.length() > 50) { // Filter out short paragraphs
                currentBlock.append(text).append("\n\n");
                if (currentBlock.length() > longestBlock.length()) {
                    longestBlock = new StringBuilder(currentBlock);
                }
            }
        }

        if (longestBlock.length() > 0) {
            return longestBlock.toString().trim();
        }

        // Fallback: return body text
        return doc.body().text();
    }

    /**
     * Extracts meta description from Open Graph or standard meta tags.
     */
    private String extractMetaDescription(Document doc) {
        Element ogDesc = doc.selectFirst("meta[property=og:description]");
        if (ogDesc != null && !ogDesc.attr("content").isBlank()) {
            return ogDesc.attr("content");
        }

        Element metaDesc = doc.selectFirst("meta[name=description]");
        if (metaDesc != null && !metaDesc.attr("content").isBlank()) {
            return metaDesc.attr("content");
        }

        return null;
    }

    /**
     * Extracts author information from meta tags.
     */
    private String extractAuthor(Document doc) {
        Element ogAuthor = doc.selectFirst("meta[property=article:author]");
        if (ogAuthor != null && !ogAuthor.attr("content").isBlank()) {
            return ogAuthor.attr("content");
        }

        Element metaAuthor = doc.selectFirst("meta[name=author]");
        if (metaAuthor != null && !metaAuthor.attr("content").isBlank()) {
            return metaAuthor.attr("content");
        }

        return null;
    }

    /**
     * Sanitizes HTML content by removing scripts, styles, and potentially dangerous
     * elements.
     */
    private String sanitizeHtml(String html) {
        return Jsoup.clean(html, Safelist.relaxed());
    }

    /**
     * Creates a snippet from content (first 200 characters).
     */
    private String createSnippet(String content) {
        String text = Jsoup.parse(content).text(); // Strip HTML tags
        if (text.length() <= 200) {
            return text;
        }
        return text.substring(0, 197) + "...";
    }

    /**
     * Generates SHA-256 hash of content for deduplication.
     */
    private String generateContentHash(String content) {
        return generateHash(content);
    }

    /**
     * Public static method to generate SHA-256 hash.
     *
     * @param content the content to hash
     * @return hex-encoded hash
     */
    public static String generateHash(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}
