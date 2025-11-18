package com.sitewatch.persistence.repository;

import com.sitewatch.persistence.entity.Source;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SourceRepositoryTest
 *
 * Integration tests for SourceRepository using in-memory H2 database.
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@DataJpaTest
@ActiveProfiles("test")
class SourceRepositoryTest {

    @Autowired
    private SourceRepository sourceRepository;

    /**
     * Test saving and retrieving a source.
     */
    @Test
    void shouldSaveAndRetrieveSource() {
        // Given
        Source source = Source.builder()
            .name("Test Blog")
            .url("https://example.com/feed")
            .mode(Source.SourceMode.AUTO)
            .intervalMinutes(30)
            .filterKeywords(new String[]{"java", "spring"})
            .build();

        // When
        Source saved = sourceRepository.save(source);
        Optional<Source> retrieved = sourceRepository.findById(saved.getId());

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Test Blog");
        assertThat(retrieved.get().getUrl()).isEqualTo("https://example.com/feed");
        assertThat(retrieved.get().getMode()).isEqualTo(Source.SourceMode.AUTO);
    }

    /**
     * Test finding source by URL.
     */
    @Test
    void shouldFindSourceByUrl() {
        // Given
        Source source = Source.builder()
            .name("Tech News")
            .url("https://technews.com/rss")
            .mode(Source.SourceMode.RSS)
            .intervalMinutes(60)
            .build();
        sourceRepository.save(source);

        // When
        Optional<Source> found = sourceRepository.findByUrl("https://technews.com/rss");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Tech News");
    }

    /**
     * Test checking if source exists by URL.
     */
    @Test
    void shouldCheckIfSourceExistsByUrl() {
        // Given
        Source source = Source.builder()
            .name("Dev Blog")
            .url("https://devblog.io/feed")
            .mode(Source.SourceMode.AUTO)
            .intervalMinutes(120)
            .build();
        sourceRepository.save(source);

        // When
        boolean exists = sourceRepository.existsByUrl("https://devblog.io/feed");
        boolean notExists = sourceRepository.existsByUrl("https://nonexistent.com");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    /**
     * Test finding sources that need checking.
     */
    @Test
    void shouldFindSourcesNeedingCheck() {
        // Given
        Source recentlyChecked = Source.builder()
            .name("Recent")
            .url("https://recent.com/feed")
            .mode(Source.SourceMode.RSS)
            .intervalMinutes(60)
            .lastChecked(Instant.now())
            .build();

        Source needsCheck = Source.builder()
            .name("Old")
            .url("https://old.com/feed")
            .mode(Source.SourceMode.RSS)
            .intervalMinutes(60)
            .lastChecked(Instant.now().minusSeconds(7200)) // 2 hours ago
            .build();

        Source neverChecked = Source.builder()
            .name("Never")
            .url("https://never.com/feed")
            .mode(Source.SourceMode.RSS)
            .intervalMinutes(60)
            .lastChecked(null)
            .build();

        sourceRepository.save(recentlyChecked);
        sourceRepository.save(needsCheck);
        sourceRepository.save(neverChecked);

        // When
        var sources = sourceRepository.findByLastCheckedBeforeOrLastCheckedIsNull(
            Instant.now().minusSeconds(3600) // 1 hour ago
        );

        // Then
        assertThat(sources).hasSize(2);
        assertThat(sources).extracting(Source::getName).containsExactlyInAnyOrder("Old", "Never");
    }
}
