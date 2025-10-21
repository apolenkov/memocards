package org.apolenkov.application.service.stats.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for CacheInvalidationEvent record.
 * Verifies proper event creation and data access.
 */
class CacheInvalidationEventTest {

    @Test
    @DisplayName("Should create event with current timestamp")
    void shouldCreateEventWithCurrentTimestamp() {
        // When
        CacheInvalidationEvent event = CacheInvalidationEvent.of("cache-type", 123L, "reason");

        // Then
        assertThat(event.cacheType()).isEqualTo("cache-type");
        assertThat(event.key()).isEqualTo(123L);
        assertThat(event.reason()).isEqualTo("reason");
        assertThat(event.timestamp()).isNotNull();
        assertThat(event.timestamp()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    @DisplayName("Should create event with custom timestamp")
    void shouldCreateEventWithCustomTimestamp() {
        // Given
        Instant customTimestamp = Instant.parse("2023-01-01T12:00:00Z");

        // When
        CacheInvalidationEvent event = new CacheInvalidationEvent("cache-type", 123L, "reason", customTimestamp);

        // Then
        assertThat(event.cacheType()).isEqualTo("cache-type");
        assertThat(event.key()).isEqualTo(123L);
        assertThat(event.reason()).isEqualTo("reason");
        assertThat(event.timestamp()).isEqualTo(customTimestamp);
    }

    @Test
    @DisplayName("Should handle null key")
    void shouldHandleNullKey() {
        // When
        CacheInvalidationEvent event = CacheInvalidationEvent.of("cache-type", null, "reason");

        // Then
        assertThat(event.key()).isNull();
        assertThat(event.getKeyAsString()).isEqualTo("null");
    }

    @Test
    @DisplayName("Should convert key to string")
    void shouldConvertKeyToString() {
        // When
        CacheInvalidationEvent event = CacheInvalidationEvent.of("cache-type", 123L, "reason");

        // Then
        assertThat(event.getKeyAsString()).isEqualTo("123");
    }

    @Test
    @DisplayName("Should handle string key")
    void shouldHandleStringKey() {
        // When
        CacheInvalidationEvent event = CacheInvalidationEvent.of("cache-type", "deck-123", "reason");

        // Then
        assertThat(event.key()).isEqualTo("deck-123");
        assertThat(event.getKeyAsString()).isEqualTo("deck-123");
    }

    @Test
    @DisplayName("Should provide cache type for metrics tagging")
    void shouldProvideCacheTypeForMetricsTagging() {
        // When
        CacheInvalidationEvent event = CacheInvalidationEvent.of("pagination-count", 123L, "card-created");

        // Then
        assertThat(event.getCacheType()).isEqualTo("pagination-count");
    }

    @Test
    @DisplayName("Should provide reason for metrics tagging")
    void shouldProvideReasonForMetricsTagging() {
        // When
        CacheInvalidationEvent event = CacheInvalidationEvent.of("cache-type", 123L, "card-deleted");

        // Then
        assertThat(event.getReason()).isEqualTo("card-deleted");
    }

    @Test
    @DisplayName("Should be immutable")
    void shouldBeImmutable() {
        // Given
        CacheInvalidationEvent event = CacheInvalidationEvent.of("cache-type", 123L, "reason");

        // When & Then - record is immutable by design
        assertThat(event.cacheType()).isEqualTo("cache-type");
        assertThat(event.key()).isEqualTo(123L);
        assertThat(event.reason()).isEqualTo("reason");
    }

    @Test
    @DisplayName("Should support different cache types")
    void shouldSupportDifferentCacheTypes() {
        // When
        CacheInvalidationEvent paginationEvent = CacheInvalidationEvent.of("pagination-count", 123L, "reason");
        CacheInvalidationEvent sessionEvent = CacheInvalidationEvent.of("session-cache", 456L, "reason");

        // Then
        assertThat(paginationEvent.getCacheType()).isEqualTo("pagination-count");
        assertThat(sessionEvent.getCacheType()).isEqualTo("session-cache");
    }

    @Test
    @DisplayName("Should support different invalidation reasons")
    void shouldSupportDifferentInvalidationReasons() {
        // When
        CacheInvalidationEvent createEvent = CacheInvalidationEvent.of("cache", 123L, "card-created");
        CacheInvalidationEvent updateEvent = CacheInvalidationEvent.of("cache", 123L, "card-updated");
        CacheInvalidationEvent deleteEvent = CacheInvalidationEvent.of("cache", 123L, "card-deleted");

        // Then
        assertThat(createEvent.getReason()).isEqualTo("card-created");
        assertThat(updateEvent.getReason()).isEqualTo("card-updated");
        assertThat(deleteEvent.getReason()).isEqualTo("card-deleted");
    }
}
