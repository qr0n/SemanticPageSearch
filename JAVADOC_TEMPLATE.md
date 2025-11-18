# Javadoc Template and Examples

This document provides templates and examples for writing Javadoc comments in the SiteWatch project.

## Table of Contents

1. [Class Documentation](#class-documentation)
2. [Method Documentation](#method-documentation)
3. [Complex Logic Documentation](#complex-logic-documentation)
4. [Common Javadoc Tags](#common-javadoc-tags)
5. [Examples by Component](#examples-by-component)

---

## Class Documentation

### Template

```java
/**
 * [ClassName]
 *
 * [Brief description of the class's purpose and responsibility]
 *
 * Key responsibilities:
 * - [Responsibility 1]
 * - [Responsibility 2]
 * - [Responsibility 3]
 *
 * [Optional: Usage example or important notes]
 *
 * @author [Your Name]
 * @since [Version when introduced, e.g., 0.1.0]
 * @see [Related classes]
 */
```

### Example: Service Class

```java
/**
 * SourceService
 *
 * Manages CRUD operations and orchestration for website sources.
 * Handles creation, update, deletion, and scheduled checking of sources.
 *
 * Key responsibilities:
 * - Validate source configuration before persistence
 * - Trigger immediate or scheduled content checks
 * - Manage source lifecycle and state transitions
 *
 * Thread-safe: This service can be called concurrently from multiple threads.
 *
 * @author Infinity Iron
 * @since 0.1.0
 * @see Source
 * @see SourceRepository
 */
@Service
public class SourceService {
    // ...
}
```

### Example: Repository Interface

```java
/**
 * ItemRepository
 *
 * Spring Data JPA repository for {@link Item} entities.
 * Provides query methods for retrieving discovered content items.
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
public interface ItemRepository extends JpaRepository<Item, UUID> {
    // ...
}
```

### Example: Entity Class

```java
/**
 * Source
 *
 * Represents a monitored website or feed source.
 * Contains configuration for crawling behavior, filtering rules,
 * and scheduling parameters.
 *
 * Persistence: Mapped to the 'sources' table in PostgreSQL.
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@Entity
@Table(name = "sources")
public class Source {
    // ...
}
```

---

## Method Documentation

### Template

```java
/**
 * [Brief description of what the method does]
 *
 * [Optional: More detailed explanation, algorithm notes, or important behavior]
 *
 * @param [paramName] [description of parameter, including constraints or format]
 * @param [paramName2] [description]
 * @return [description of return value, including possible states like null/empty]
 * @throws [ExceptionType] [when/why this exception is thrown]
 * @throws [ExceptionType2] [when/why this exception is thrown]
 */
```

### Example: Service Method

```java
/**
 * Create a new source and schedule it for periodic content checks.
 *
 * Validates the source URL, filter rules, and interval before persisting.
 * If validation fails, throws IllegalArgumentException with details.
 *
 * @param request the source creation request containing URL, filters, and schedule
 * @return the created source with generated ID and timestamps
 * @throws IllegalArgumentException if URL is invalid or interval is out of range
 * @throws DuplicateSourceException if a source with the same URL already exists
 */
public Source createSource(CreateSourceRequest request) {
    // ...
}
```

### Example: Repository Query Method

```java
/**
 * Find all items for a specific source, ordered by published date descending.
 *
 * @param sourceId the UUID of the source
 * @param pageable pagination and sorting parameters
 * @return page of items, never null (may be empty)
 */
Page<Item> findBySourceIdOrderByPublishedAtDesc(UUID sourceId, Pageable pageable);
```

### Example: Crawler Method

```java
/**
 * Fetch and parse an RSS/Atom feed from the given URL.
 *
 * Performs HTTP GET request with configured timeout and user-agent.
 * Parses feed entries using Rome library and maps to Item objects.
 * Network failures trigger retry logic with exponential backoff.
 *
 * @param feedUrl the RSS/Atom feed URL (must be valid HTTP/HTTPS)
 * @param source the source configuration containing filters and metadata
 * @return list of parsed items (never null, may be empty if feed is empty)
 * @throws IOException if network request fails after all retries
 * @throws FeedParseException if feed format is invalid or unsupported
 */
public List<Item> fetchFromRss(String feedUrl, Source source) throws IOException {
    // ...
}
```

---

## Complex Logic Documentation

For complex private methods or non-obvious logic blocks, use inline comments:

### Example: Hash Computation

```java
/**
 * Compute SHA-256 hash of the main content text for deduplication.
 *
 * The hash is computed after:
 * 1. Removing HTML tags and scripts
 * 2. Normalizing whitespace (collapse multiple spaces)
 * 3. Converting to lowercase for case-insensitive comparison
 *
 * This approach catches minor edits (typos, formatting) while detecting
 * substantive content changes.
 *
 * @param mainText the extracted article text (may contain some HTML)
 * @return lowercase hex string of SHA-256 hash (64 characters)
 */
private String computeContentHash(String mainText) {
    // Strip HTML tags using Jsoup to get plain text
    String plainText = Jsoup.parse(mainText).text();
    
    // Normalize: lowercase, collapse whitespace, trim
    String normalized = plainText.toLowerCase()
        .replaceAll("\\s+", " ")
        .trim();
    
    // Compute SHA-256 and return as hex
    return DigestUtils.sha256Hex(normalized);
}
```

### Example: Retry Logic

```java
// Retry failed HTTP requests with exponential backoff
// - Start delay: 1 second
// - Max retries: 3
// - Backoff multiplier: 2x
// Throws IOException if all retries exhausted
private Response fetchWithRetry(String url) throws IOException {
    int attempt = 0;
    long delay = 1000; // 1 second
    
    while (attempt < maxRetries) {
        try {
            return httpClient.get(url);
        } catch (IOException e) {
            attempt++;
            if (attempt >= maxRetries) {
                throw e;
            }
            // Exponential backoff: wait delay * 2^attempt milliseconds
            Thread.sleep(delay * (1L << attempt));
        }
    }
}
```

---

## Common Javadoc Tags

| Tag | Usage | Example |
|-----|-------|---------|
| `@author` | Author of the class | `@author Infinity Iron` |
| `@since` | Version when introduced | `@since 0.1.0` |
| `@param` | Method parameter | `@param url the target URL` |
| `@return` | Return value | `@return list of items (never null)` |
| `@throws` | Exception thrown | `@throws IOException on network failure` |
| `@see` | Related class/method | `@see ArticleCrawler` |
| `@deprecated` | Marks deprecated API | `@deprecated use {@link #newMethod()} instead` |
| `@link` | Link to class/method | `{@link Source}` |
| `@code` | Inline code | `{@code String value}` |

---

## Examples by Component

### API Controller

```java
/**
 * SourceController
 *
 * REST API endpoints for managing website sources.
 * Provides CRUD operations and manual check triggering.
 *
 * Base path: /api/v1/sources
 * Authentication: Required (JWT or API key)
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/v1/sources")
public class SourceController {

    /**
     * Create a new monitored source.
     *
     * @param request the source creation request
     * @return HTTP 201 with created source, or 400 if validation fails
     */
    @PostMapping
    public ResponseEntity<SourceDTO> createSource(@Valid @RequestBody CreateSourceRequest request) {
        // ...
    }
}
```

### Configuration Class

```java
/**
 * SecurityConfig
 *
 * Configures Spring Security for the application.
 * Sets up JWT authentication, API key support, and CORS.
 *
 * Security features:
 * - JWT token validation for user authentication
 * - API key authentication for server-to-server calls
 * - CSRF protection for state-changing operations
 * - Rate limiting per IP address
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // ...
}
```

### Notifier Interface

```java
/**
 * Notifier
 *
 * Interface for sending notifications about discovered items.
 * Implementations include email, webhooks, and chat integrations.
 *
 * Implementations must be thread-safe and handle failures gracefully
 * with appropriate retry logic.
 *
 * @author Infinity Iron
 * @since 0.1.0
 * @see EmailNotifier
 * @see WebhookNotifier
 */
public interface Notifier {

    /**
     * Send a notification for a newly discovered item.
     *
     * @param item the item to notify about
     * @param channel the notification channel configuration (email, webhook URL, etc.)
     * @return notification result with status and message
     * @throws NotificationException if notification fails after all retries
     */
    NotificationResult send(Item item, NotificationChannel channel) throws NotificationException;
}
```

---

## Tips for Good Javadoc

1. **Be concise but complete**: First sentence should be a summary (appears in listings)
2. **Use present tense**: "Returns" not "Will return"
3. **Document null behavior**: Specify if parameters can be null or if method returns null
4. **Include examples for complex APIs**: Show typical usage in `<pre>` tags
5. **Link to related classes**: Use `{@link}` for cross-references
6. **Describe side effects**: If method modifies state, mention it
7. **Specify thread safety**: If relevant for concurrent usage
8. **Document validation**: What parameter values are acceptable

## Automated Checks

Our CI pipeline enforces:
- ✅ All public classes have Javadoc
- ✅ All public methods have Javadoc with `@param` and `@return`
- ✅ No missing parameter documentation
- ✅ Javadoc references valid classes/methods

Use `mvn checkstyle:check` to validate locally before committing.
