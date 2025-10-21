package org.apolenkov.application.service.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.concurrent.atomic.AtomicInteger;
import org.apolenkov.application.domain.event.DeckModifiedEvent;
import org.apolenkov.application.domain.event.DeckModifiedEvent.ModificationType;
import org.apolenkov.application.domain.event.ProgressChangedEvent;
import org.apolenkov.application.domain.event.ProgressChangedEvent.ChangeType;
import org.apolenkov.application.domain.model.FilterOption;
import org.apolenkov.application.service.stats.metrics.CacheMetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for PaginationCountCache.
 * Tests smart invalidation, debouncing, TTL behavior, and cache statistics.
 */
@DisplayName("PaginationCountCache Unit Tests")
class PaginationCountCacheTest {

    private PaginationCountCache cache;
    private AtomicInteger loaderCallCount;

    @BeforeEach
    void setUp() {
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        CacheMetricsCollector metricsCollector = mock(CacheMetricsCollector.class);
        cache = new PaginationCountCache(eventPublisher, metricsCollector);
        // Set @Value fields manually for unit tests
        ReflectionTestUtils.setField(cache, "ttlMs", 60000L); // 1 minute
        ReflectionTestUtils.setField(cache, "maxSize", 500);
        loaderCallCount = new AtomicInteger(0);
    }

    /**
     * Helper method to throw assertion error when loader should not be called.
     * Used for testing cache HIT scenarios where data should already be cached.
     *
     * @return never returns (always throws)
     */
    private long throwShouldBeCached() {
        throw new AssertionError("Should be cached");
    }

    // ========== BASIC CACHING TESTS ==========

    @Test
    @DisplayName("Should cache count on first access")
    void shouldCacheCountOnFirstAccess() {
        // Given: Empty cache
        long expectedCount = 100L;

        // When: First access (cache MISS)
        long result = cache.getCount(1L, "", FilterOption.ALL, () -> {
            loaderCallCount.incrementAndGet();
            return expectedCount;
        });

        // Then: Loader called, count cached
        assertThat(result).isEqualTo(expectedCount);
        assertThat(loaderCallCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return cached count on second access (cache HIT)")
    void shouldReturnCachedCountOnSecondAccess() {
        // Given: Cached count
        cache.getCount(1L, "", FilterOption.ALL, () -> 100L);

        // When: Second access (cache HIT)
        long result = cache.getCount(1L, "", FilterOption.ALL, this::throwShouldBeCached);

        // Then: Cached value returned
        assertThat(result).isEqualTo(100L);

        // And: Cache stats show 1 hit, 1 miss
        PaginationCountCache.CacheStats stats = cache.getStats();
        assertThat(stats.hits()).isEqualTo(1);
        assertThat(stats.misses()).isEqualTo(1);
        assertThat(stats.hitRate()).isEqualTo(0.5); // 50%
    }

    @Test
    @DisplayName("Should cache different counts for different filter options")
    void shouldCacheDifferentCountsForDifferentFilters() {
        // Given: Same deck, different filters
        long deckId = 1L;

        // When: Cache counts for different filters
        long allCount = cache.getCount(deckId, "", FilterOption.ALL, () -> 100L);
        long knownCount = cache.getCount(deckId, "", FilterOption.KNOWN_ONLY, () -> 30L);
        long unknownCount = cache.getCount(deckId, "", FilterOption.UNKNOWN_ONLY, () -> 70L);

        // Then: Different counts cached independently
        assertThat(allCount).isEqualTo(100L);
        assertThat(knownCount).isEqualTo(30L);
        assertThat(unknownCount).isEqualTo(70L);

        // And: All cached (subsequent calls are HITs)
        assertThat(cache.getCount(deckId, "", FilterOption.ALL, this::throwShouldBeCached))
                .isEqualTo(100L);

        assertThat(cache.getCount(deckId, "", FilterOption.KNOWN_ONLY, this::throwShouldBeCached))
                .isEqualTo(30L);
    }

    @Test
    @DisplayName("Should cache different counts for different search queries")
    void shouldCacheDifferentCountsForDifferentSearches() {
        // Given: Same deck, same filter, different searches
        long deckId = 1L;

        // When: Cache counts for different search queries
        long noSearchCount = cache.getCount(deckId, "", FilterOption.ALL, () -> 100L);
        long searchACount = cache.getCount(deckId, "test", FilterOption.ALL, () -> 10L);
        long searchBCount = cache.getCount(deckId, "hello", FilterOption.ALL, () -> 5L);

        // Then: Different counts cached
        assertThat(noSearchCount).isEqualTo(100L);
        assertThat(searchACount).isEqualTo(10L);
        assertThat(searchBCount).isEqualTo(5L);

        // And: Cache stats show 3 misses
        PaginationCountCache.CacheStats stats = cache.getStats();
        assertThat(stats.misses()).isEqualTo(3);
    }

    // ========== SMART INVALIDATION TESTS ==========

    @Test
    @DisplayName("Smart invalidation: Should NOT invalidate FilterOption.ALL on progress change")
    void shouldNotInvalidateAllFilterOnProgressChange() {
        // Given: Cached counts for all filter types
        long deckId = 1L;
        cache.getCount(deckId, "", FilterOption.ALL, () -> 100L);
        cache.getCount(deckId, "", FilterOption.KNOWN_ONLY, () -> 30L);
        cache.getCount(deckId, "", FilterOption.UNKNOWN_ONLY, () -> 70L);

        // When: ProgressChangedEvent published (card status toggle)
        ProgressChangedEvent event = new ProgressChangedEvent(this, deckId, ChangeType.CARD_STATUS_CHANGED);
        cache.onProgressChanged(event);

        // Then: FilterOption.ALL still cached (HIT)
        long allCount = cache.getCount(deckId, "", FilterOption.ALL, this::throwShouldBeCached);
        assertThat(allCount).isEqualTo(100L);

        // And: KNOWN_ONLY and UNKNOWN_ONLY invalidated (MISS)
        long knownCount = cache.getCount(deckId, "", FilterOption.KNOWN_ONLY, () -> {
            loaderCallCount.incrementAndGet();
            return 31L; // Updated count
        });
        assertThat(knownCount).isEqualTo(31L);
        assertThat(loaderCallCount.get()).isEqualTo(1); // Fresh load
    }

    @Test
    @DisplayName("Smart invalidation: Should invalidate only KNOWN_ONLY and UNKNOWN_ONLY")
    void shouldInvalidateOnlyKnownAndUnknownFilters() {
        // Given: Multiple cached counts
        long deckId = 1L;
        cache.getCount(deckId, "", FilterOption.ALL, () -> 100L);
        cache.getCount(deckId, "search", FilterOption.ALL, () -> 50L); // Different search
        cache.getCount(deckId, "", FilterOption.KNOWN_ONLY, () -> 30L);
        cache.getCount(deckId, "", FilterOption.UNKNOWN_ONLY, () -> 70L);

        // When: ProgressChangedEvent published
        ProgressChangedEvent event = new ProgressChangedEvent(this, deckId, ChangeType.CARD_STATUS_CHANGED);
        cache.onProgressChanged(event);

        // Then: Both ALL filters still cached
        assertThat(cache.getCount(deckId, "", FilterOption.ALL, this::throwShouldBeCached))
                .isEqualTo(100L);

        assertThat(cache.getCount(deckId, "search", FilterOption.ALL, this::throwShouldBeCached))
                .isEqualTo(50L);

        // And: KNOWN_ONLY and UNKNOWN_ONLY invalidated
        cache.getCount(deckId, "", FilterOption.KNOWN_ONLY, () -> {
            loaderCallCount.incrementAndGet();
            return 31L;
        });
        cache.getCount(deckId, "", FilterOption.UNKNOWN_ONLY, () -> {
            loaderCallCount.incrementAndGet();
            return 69L;
        });

        assertThat(loaderCallCount.get()).isEqualTo(2); // Both reloaded
    }

    // ========== DEBOUNCING TESTS ==========

    @Test
    @DisplayName("Debouncing: Should skip invalidation within cooldown period")
    void shouldSkipInvalidationWithinCooldown() {
        // Given: Cached counts
        long deckId = 1L;
        cache.getCount(deckId, "", FilterOption.KNOWN_ONLY, () -> 30L);

        // When: First progress change event
        ProgressChangedEvent event1 = new ProgressChangedEvent(this, deckId, ChangeType.CARD_STATUS_CHANGED);
        cache.onProgressChanged(event1);

        // And: Second event immediately after (within 2 sec cooldown)
        ProgressChangedEvent event2 = new ProgressChangedEvent(this, deckId, ChangeType.CARD_STATUS_CHANGED);
        cache.onProgressChanged(event2);

        // Then: Second invalidation skipped
        PaginationCountCache.CacheStats stats = cache.getStats();
        assertThat(stats.skippedInvalidations()).isEqualTo(1);

        // And: Cache still invalidated from first event
        cache.getCount(deckId, "", FilterOption.KNOWN_ONLY, () -> {
            loaderCallCount.incrementAndGet();
            return 31L;
        });
        assertThat(loaderCallCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debouncing: Should work independently for different decks (no cooldown interference)")
    void shouldDebounceIndependentlyForDifferentDecks() {
        // Given: Two different decks
        cache.getCount(1L, "", FilterOption.KNOWN_ONLY, () -> 30L);
        cache.getCount(2L, "", FilterOption.KNOWN_ONLY, () -> 40L);

        // When: Invalidate deck 1
        ProgressChangedEvent event1 = new ProgressChangedEvent(this, 1L, ChangeType.CARD_STATUS_CHANGED);
        cache.onProgressChanged(event1);

        // And: Immediately invalidate deck 2 (different deck, no cooldown)
        ProgressChangedEvent event2 = new ProgressChangedEvent(this, 2L, ChangeType.CARD_STATUS_CHANGED);
        cache.onProgressChanged(event2);

        // Then: Both invalidations succeed (no skips - different decks)
        PaginationCountCache.CacheStats stats = cache.getStats();
        assertThat(stats.skippedInvalidations()).isZero(); // No skips because different decks
    }

    @Test
    @DisplayName("Debouncing: Should track skipped invalidations count")
    void shouldTrackSkippedInvalidationsCount() {
        // Given: Cached count
        long deckId = 1L;
        cache.getCount(deckId, "", FilterOption.KNOWN_ONLY, () -> 30L);

        // When: Rapid fire events (5 events)
        ProgressChangedEvent event = new ProgressChangedEvent(this, deckId, ChangeType.CARD_STATUS_CHANGED);
        cache.onProgressChanged(event); // 1st: invalidated
        cache.onProgressChanged(event); // 2nd: skipped
        cache.onProgressChanged(event); // 3rd: skipped
        cache.onProgressChanged(event); // 4th: skipped
        cache.onProgressChanged(event); // 5th: skipped

        // Then: 4 invalidations skipped
        PaginationCountCache.CacheStats stats = cache.getStats();
        assertThat(stats.skippedInvalidations()).isEqualTo(4);
    }

    @Test
    @DisplayName("Debouncing: Should debounce per deck independently")
    void shouldDebouncePerDeckIndependently() {
        // Given: Cached counts for two decks
        cache.getCount(1L, "", FilterOption.KNOWN_ONLY, () -> 30L);
        cache.getCount(2L, "", FilterOption.KNOWN_ONLY, () -> 40L);

        // When: Rapid events for deck 1
        ProgressChangedEvent event1 = new ProgressChangedEvent(this, 1L, ChangeType.CARD_STATUS_CHANGED);
        cache.onProgressChanged(event1); // Deck 1: invalidated
        cache.onProgressChanged(event1); // Deck 1: skipped

        // And: Event for deck 2 (different deck, no cooldown)
        ProgressChangedEvent event2 = new ProgressChangedEvent(this, 2L, ChangeType.CARD_STATUS_CHANGED);
        cache.onProgressChanged(event2); // Deck 2: invalidated (NOT skipped)

        // Then: Only deck 1 has skipped invalidation
        PaginationCountCache.CacheStats stats = cache.getStats();
        assertThat(stats.skippedInvalidations()).isEqualTo(1); // Only deck 1's second event skipped
    }

    // ========== DECK MODIFIED EVENT TESTS ==========

    @Test
    @DisplayName("Should invalidate ALL filters on DeckModifiedEvent")
    void shouldInvalidateAllFiltersOnDeckModified() {
        // Given: Cached counts for all filters
        long deckId = 1L;
        cache.getCount(deckId, "", FilterOption.ALL, () -> 100L);
        cache.getCount(deckId, "", FilterOption.KNOWN_ONLY, () -> 30L);
        cache.getCount(deckId, "", FilterOption.UNKNOWN_ONLY, () -> 70L);

        // When: DeckModifiedEvent (flashcard added/deleted)
        DeckModifiedEvent event = new DeckModifiedEvent(this, null, deckId, ModificationType.UPDATED);
        cache.onDeckModified(event);

        // Then: ALL filters invalidated (including FilterOption.ALL)
        cache.getCount(deckId, "", FilterOption.ALL, () -> {
            loaderCallCount.incrementAndGet();
            return 101L;
        });
        cache.getCount(deckId, "", FilterOption.KNOWN_ONLY, () -> {
            loaderCallCount.incrementAndGet();
            return 31L;
        });

        assertThat(loaderCallCount.get()).isEqualTo(2); // Both reloaded
    }

    // ========== CACHE STATS TESTS ==========

    @Test
    @DisplayName("Should track cache hits and misses correctly")
    void shouldTrackCacheHitsAndMisses() {
        // Given: First access (MISS)
        cache.getCount(1L, "", FilterOption.ALL, () -> 100L);

        PaginationCountCache.CacheStats stats = cache.getStats();
        assertThat(stats.misses()).isEqualTo(1);
        assertThat(stats.hits()).isZero();
        assertThat(stats.hitRate()).isZero();

        // When: Three cache hits
        cache.getCount(1L, "", FilterOption.ALL, this::throwShouldBeCached);
        cache.getCount(1L, "", FilterOption.ALL, this::throwShouldBeCached);
        cache.getCount(1L, "", FilterOption.ALL, this::throwShouldBeCached);

        // Then: Hit rate is 75% (3 hits / 4 total)
        stats = cache.getStats();
        assertThat(stats.hits()).isEqualTo(3);
        assertThat(stats.misses()).isEqualTo(1);
        assertThat(stats.hitRate()).isEqualTo(0.75);
    }

    @Test
    @DisplayName("Should calculate hit rate correctly")
    void shouldCalculateHitRateCorrectly() {
        // Given: 1 miss + 9 hits = 90% hit rate
        cache.getCount(1L, "", FilterOption.ALL, () -> 100L); // MISS

        for (int i = 0; i < 9; i++) {
            cache.getCount(1L, "", FilterOption.ALL, this::throwShouldBeCached); // HIT
        }

        // Then: 90% hit rate
        PaginationCountCache.CacheStats stats = cache.getStats();
        assertThat(stats.hitRate()).isEqualTo(0.9);
    }

    @Test
    @DisplayName("Should include skippedInvalidations in stats")
    void shouldIncludeSkippedInvalidationsInStats() {
        // Given: Rapid invalidations
        cache.getCount(1L, "", FilterOption.KNOWN_ONLY, () -> 30L);

        ProgressChangedEvent event = new ProgressChangedEvent(this, 1L, ChangeType.CARD_STATUS_CHANGED);
        cache.onProgressChanged(event);
        cache.onProgressChanged(event);
        cache.onProgressChanged(event);

        // Then: Stats show skipped count
        PaginationCountCache.CacheStats stats = cache.getStats();
        assertThat(stats.skippedInvalidations()).isEqualTo(2);
    }

    // ========== EDGE CASES ==========

    @Test
    @DisplayName("Should handle null deck ID gracefully")
    void shouldHandleNullDeckIdGracefully() {
        // When: Null deck ID
        long result = cache.getCount(null, "", FilterOption.ALL, () -> {
            loaderCallCount.incrementAndGet();
            return 100L;
        });

        // Then: Loader called (no caching for null)
        assertThat(result).isEqualTo(100L);
        assertThat(loaderCallCount.get()).isEqualTo(1);

        // And: No cache stats recorded
        PaginationCountCache.CacheStats stats = cache.getStats();
        assertThat(stats.hits()).isZero();
        assertThat(stats.misses()).isZero();
    }

    @Test
    @DisplayName("Should handle null filter option gracefully")
    void shouldHandleNullFilterOptionGracefully() {
        // When: Null filter option
        long result = cache.getCount(1L, "", null, () -> {
            loaderCallCount.incrementAndGet();
            return 100L;
        });

        // Then: Loader called (no caching for null)
        assertThat(result).isEqualTo(100L);
        assertThat(loaderCallCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should normalize search query (null and empty treated same)")
    void shouldNormalizeSearchQuery() {
        // Given: Cache with null search
        cache.getCount(1L, null, FilterOption.ALL, () -> 100L);

        // When: Access with empty string
        long result = cache.getCount(1L, "", FilterOption.ALL, this::throwShouldBeCached);

        // Then: Cache HIT (normalized)
        assertThat(result).isEqualTo(100L);

        PaginationCountCache.CacheStats stats = cache.getStats();
        assertThat(stats.hits()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should cache zero count correctly")
    void shouldCacheZeroCountCorrectly() {
        // Given: Zero count
        long result1 = cache.getCount(1L, "", FilterOption.ALL, () -> 0L);

        // Then: Zero cached
        assertThat(result1).isZero();

        // When: Second access
        long result2 = cache.getCount(1L, "", FilterOption.ALL, this::throwShouldBeCached);

        // Then: Cached zero returned
        assertThat(result2).isZero();

        PaginationCountCache.CacheStats stats = cache.getStats();
        assertThat(stats.hits()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle cache eviction when max size reached")
    void shouldHandleEvictionWhenMaxSizeReached() {
        // Given: Small cache for testing
        ReflectionTestUtils.setField(cache, "maxSize", 3);

        // When: Fill cache to max
        cache.getCount(1L, "", FilterOption.ALL, () -> 100L);
        cache.getCount(2L, "", FilterOption.ALL, () -> 200L);
        cache.getCount(3L, "", FilterOption.ALL, () -> 300L);

        PaginationCountCache.CacheStats stats = cache.getStats();
        assertThat(stats.size()).isEqualTo(3);

        // When: Add 4th entry (eviction)
        cache.getCount(4L, "", FilterOption.ALL, () -> 400L);

        // Then: Cache size still 3 (oldest evicted)
        stats = cache.getStats();
        assertThat(stats.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should handle null event gracefully")
    void shouldHandleNullEventGracefully() {
        // When: Null event
        cache.onProgressChanged(null);
        cache.onDeckModified(null);

        // Then: No exception, stats unchanged
        PaginationCountCache.CacheStats stats = cache.getStats();
        assertThat(stats.skippedInvalidations()).isZero();
    }

    // ========== LOGGING TESTS ==========

    @Test
    @DisplayName("Should log stats without exception")
    void shouldLogStatsWithoutException() {
        // Given: Some cached data
        cache.getCount(1L, "", FilterOption.ALL, () -> 100L);
        cache.getCount(1L, "", FilterOption.ALL, this::throwShouldBeCached);

        // When: Get stats before logging
        PaginationCountCache.CacheStats statsBefore = cache.getStats();

        // And: Call logStats
        cache.logStats();

        // Then: Stats unchanged (logging is read-only operation)
        PaginationCountCache.CacheStats statsAfter = cache.getStats();
        assertThat(statsAfter.hits()).isEqualTo(statsBefore.hits());
        assertThat(statsAfter.misses()).isEqualTo(statsBefore.misses());
        assertThat(statsAfter.size()).isEqualTo(statsBefore.size());
        assertThat(statsAfter.skippedInvalidations()).isEqualTo(statsBefore.skippedInvalidations());
    }
}
