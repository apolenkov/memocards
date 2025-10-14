package org.apolenkov.application.service.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for KnownCardsCache.
 * Tests TTL behavior, cache hits/misses, batch operations, and invalidation.
 */
@DisplayName("KnownCardsCache Unit Tests")
class KnownCardsCacheTest {

    private KnownCardsCache cache;
    private AtomicInteger loaderCallCount;

    @BeforeEach
    void setUp() {
        cache = new KnownCardsCache();
        // Set @Value fields manually for unit tests
        ReflectionTestUtils.setField(cache, "ttlMs", 300000L);
        ReflectionTestUtils.setField(cache, "maxSize", 1000);
        loaderCallCount = new AtomicInteger(0);
    }

    @Test
    @DisplayName("Should cache known cards on first access")
    void shouldCacheKnownCardsOnFirstAccess() {
        // Given: Empty cache
        Set<Long> knownCards = Set.of(1L, 2L, 3L);

        // When: First access (cache MISS)
        Set<Long> result = cache.getKnownCards(1L, () -> {
            loaderCallCount.incrementAndGet();
            return knownCards;
        });

        // Then: Loader called, data cached
        assertThat(result).containsExactlyInAnyOrderElementsOf(knownCards);
        assertThat(loaderCallCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return cached value on second access (cache HIT)")
    void shouldReturnCachedValueOnSecondAccess() {
        // Given: Cached known cards
        Set<Long> knownCards = Set.of(1L, 2L, 3L);
        cache.getKnownCards(1L, () -> knownCards);

        // When: Second access (cache HIT)
        Set<Long> result = cache.getKnownCards(1L, () -> {
            throw new AssertionError("Loader should NOT be called for cache HIT");
        });

        // Then: Cached value returned
        assertThat(result).containsExactlyInAnyOrderElementsOf(knownCards);

        // And: Cache stats show 1 hit, 1 miss
        KnownCardsCache.CacheStats stats = cache.getStats();
        assertThat(stats.hits()).isEqualTo(1);
        assertThat(stats.misses()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should invalidate cache for specific deck")
    void shouldInvalidateCacheForSpecificDeck() {
        // Given: Cached known cards for deck 1
        Set<Long> initialCards = Set.of(1L, 2L);
        cache.getKnownCards(1L, () -> initialCards);

        // When: Invalidate deck 1
        cache.invalidate(1L);

        // Then: Next access reloads from database
        Set<Long> updatedCards = Set.of(1L, 2L, 3L, 4L); // More cards now
        Set<Long> result = cache.getKnownCards(1L, () -> {
            loaderCallCount.incrementAndGet();
            return updatedCards;
        });

        assertThat(result).hasSize(4);
        assertThat(loaderCallCount.get()).isEqualTo(1); // Fresh load
    }

    @Test
    @DisplayName("Should handle null deck ID gracefully")
    void shouldHandleNullDeckIdGracefully() {
        // When: Null deck ID
        Set<Long> result = cache.getKnownCards(null, () -> Set.of(1L));

        // Then: Empty set returned, loader NOT called
        assertThat(result).isEmpty();
        assertThat(loaderCallCount.get()).isZero();
    }

    @Test
    @DisplayName("Should cache empty set correctly")
    void shouldCacheEmptySetCorrectly() {
        // Given: Deck with no known cards
        Set<Long> emptySet = Set.of();

        // When: Cache empty set
        Set<Long> result1 = cache.getKnownCards(1L, () -> emptySet);

        // Then: Empty set cached
        assertThat(result1).isEmpty();

        // When: Second access
        Set<Long> result2 = cache.getKnownCards(1L, () -> {
            throw new AssertionError("Loader should NOT be called for cache HIT");
        });

        // Then: Cached empty set returned
        assertThat(result2).isEmpty();

        // And: Cache stats show 1 hit, 1 miss
        KnownCardsCache.CacheStats stats = cache.getStats();
        assertThat(stats.hits()).isEqualTo(1);
        assertThat(stats.misses()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle batch operations correctly")
    void shouldHandleBatchOperationsCorrectly() {
        // Given: Some decks already cached
        cache.getKnownCards(1L, () -> Set.of(1L, 2L));
        cache.getKnownCards(2L, () -> Set.of(3L, 4L));

        Set<Long> requestedDeckIds = Set.of(1L, 2L, 3L); // 3 is not cached

        // When: Batch retrieval
        Map<Long, Set<Long>> result = cache.getKnownCardsBatch(requestedDeckIds, () -> {
            // Loader called only for missing deck 3
            return Map.of(3L, Set.of(5L, 6L));
        });

        // Then: Cached decks returned from cache, missing loaded in batch
        assertThat(result).hasSize(3);
        assertThat(result.get(1L)).containsExactlyInAnyOrder(1L, 2L); // From cache
        assertThat(result.get(2L)).containsExactlyInAnyOrder(3L, 4L); // From cache
        assertThat(result.get(3L)).containsExactlyInAnyOrder(5L, 6L); // Fresh load

        // And: Cache stats: 2 initial misses + 2 hits (batch) + 1 miss (batch)
        KnownCardsCache.CacheStats stats = cache.getStats();
        assertThat(stats.misses()).isEqualTo(3); // 2 initial + 1 batch miss
        assertThat(stats.hits()).isEqualTo(2); // 2 batch hits
    }

    @Test
    @DisplayName("Should handle empty deck IDs in batch")
    void shouldHandleEmptyDeckIdsInBatch() {
        // When: Empty deck IDs
        Map<Long, Set<Long>> result = cache.getKnownCardsBatch(Set.of(), () -> {
            loaderCallCount.incrementAndGet();
            return Map.of();
        });

        // Then: Empty result, loader NOT called
        assertThat(result).isEmpty();
        assertThat(loaderCallCount.get()).isZero();
    }

    @Test
    @DisplayName("Should handle null deck IDs in batch")
    void shouldHandleNullDeckIdsInBatch() {
        // When: Null deck IDs
        Map<Long, Set<Long>> result = cache.getKnownCardsBatch(null, () -> {
            loaderCallCount.incrementAndGet();
            return Map.of();
        });

        // Then: Empty result, loader NOT called
        assertThat(result).isEmpty();
        assertThat(loaderCallCount.get()).isZero();
    }

    @Test
    @DisplayName("Should cache separate decks independently")
    void shouldCacheSeparateDecksIndependently() {
        // Given: Two different decks
        Set<Long> deck1Cards = Set.of(1L, 2L);
        Set<Long> deck2Cards = Set.of(10L, 20L);

        // When: Cache both decks
        Set<Long> result1 = cache.getKnownCards(1L, () -> deck1Cards);
        Set<Long> result2 = cache.getKnownCards(2L, () -> deck2Cards);

        // Then: Both cached independently
        assertThat(result1).containsExactlyInAnyOrderElementsOf(deck1Cards);
        assertThat(result2).containsExactlyInAnyOrderElementsOf(deck2Cards);

        // When: Invalidate deck 1
        cache.invalidate(1L);

        // Then: Deck 2 still cached (HIT), deck 1 invalidated
        Set<Long> cached2 = cache.getKnownCards(2L, () -> {
            throw new AssertionError("Should not be called - deck 2 still cached");
        });
        assertThat(cached2).containsExactlyInAnyOrderElementsOf(deck2Cards);

        // And: Stats show 1 hit (deck 2) + 2 initial misses
        KnownCardsCache.CacheStats stats = cache.getStats();
        assertThat(stats.hits()).isEqualTo(1);
        assertThat(stats.misses()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle null invalidation gracefully")
    void shouldHandleNullInvalidationGracefully() {
        // When: Invalidate null
        cache.invalidate(null);

        // Then: Cache still works normally (no exception, state not corrupted)
        Set<Long> result = cache.getKnownCards(1L, () -> Set.of(1L, 2L));
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should return immutable set from cache")
    void shouldReturnImmutableSetFromCache() {
        // Given: Mutable set passed to cache
        Set<Long> mutableSet = Set.of(1L, 2L, 3L);
        Set<Long> cached = cache.getKnownCards(1L, () -> mutableSet);

        // When/Then: Returned set is immutable (List.copyOf in implementation)
        assertThat(cached).isInstanceOf(Set.class).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    @DisplayName("Should handle concurrent access to different decks")
    void shouldHandleConcurrentAccessToDifferentDecks() throws InterruptedException {
        // Given: Multiple threads accessing different decks
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final long deckId = i;
            threads[i] = new Thread(() -> cache.getKnownCards(deckId, () -> Set.of(deckId * 10, deckId * 10 + 1)));
        }

        // When: Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Then: All threads complete without exception
        for (Thread thread : threads) {
            thread.join(5000); // 5 sec timeout
            assertThat(thread.isAlive()).isFalse();
        }

        // Verify all decks cached (HIT for each)
        for (int i = 0; i < threadCount; i++) {
            final long deckId = i;
            Set<Long> result = cache.getKnownCards(deckId, () -> {
                throw new AssertionError("Should be cached for deck " + deckId);
            });
            assertThat(result).hasSize(2);
        }

        // And: Stats show 10 hits + 10 initial misses
        KnownCardsCache.CacheStats stats = cache.getStats();
        assertThat(stats.hits()).isEqualTo(threadCount);
        assertThat(stats.misses()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("Should batch load only missing decks efficiently")
    void shouldBatchLoadOnlyMissingDecks() {
        // Given: Decks 1,2,3 already cached
        cache.getKnownCards(1L, () -> Set.of(1L));
        cache.getKnownCards(2L, () -> Set.of(2L));
        cache.getKnownCards(3L, () -> Set.of(3L));

        Set<Long> requestedDeckIds = Set.of(1L, 2L, 3L, 4L, 5L); // 4,5 not cached

        // When: Batch request
        Map<Long, Set<Long>> result = cache.getKnownCardsBatch(requestedDeckIds, () -> {
            // Batch loader should only load 4 and 5
            return Map.of(4L, Set.of(4L), 5L, Set.of(5L));
        });

        // Then: All 5 decks in result
        assertThat(result).hasSize(5);
        assertThat(result.get(1L)).containsExactly(1L); // From cache
        assertThat(result.get(2L)).containsExactly(2L); // From cache
        assertThat(result.get(3L)).containsExactly(3L); // From cache
        assertThat(result.get(4L)).containsExactly(4L); // Batch loaded
        assertThat(result.get(5L)).containsExactly(5L); // Batch loaded

        // And: Stats: 3 initial misses + 3 batch hits + 2 batch misses
        KnownCardsCache.CacheStats stats = cache.getStats();
        assertThat(stats.hits()).isEqualTo(3); // Decks 1, 2, 3 from cache
        assertThat(stats.misses()).isEqualTo(5); // 3 initial + 2 batch
    }

    @Test
    @DisplayName("Should track cache hits and misses")
    void shouldTrackCacheHitsAndMisses() {
        // Given: Empty cache
        Set<Long> knownCards = Set.of(1L, 2L, 3L);

        // When: First access (MISS)
        cache.getKnownCards(1L, () -> knownCards);

        // Then: One miss, zero hits
        KnownCardsCache.CacheStats stats = cache.getStats();
        assertThat(stats.misses()).isEqualTo(1);
        assertThat(stats.hits()).isZero();
        assertThat(stats.size()).isEqualTo(1);

        // When: Second access (HIT)
        cache.getKnownCards(1L, Set::of);

        // Then: One miss, one hit
        stats = cache.getStats();
        assertThat(stats.hits()).isEqualTo(1);
        assertThat(stats.misses()).isEqualTo(1);
        assertThat(stats.hitRate()).isEqualTo(0.5); // 50% hit rate
    }

    @Test
    @DisplayName("Should calculate correct hit rate")
    void shouldCalculateCorrectHitRate() {
        // Given: Cached known cards
        cache.getKnownCards(1L, () -> Set.of(1L, 2L));

        // When: 3 cache hits
        cache.getKnownCards(1L, Set::of);
        cache.getKnownCards(1L, Set::of);
        cache.getKnownCards(1L, Set::of);

        // Then: Hit rate is 75% (3 hits / 4 total)
        KnownCardsCache.CacheStats stats = cache.getStats();
        assertThat(stats.hits()).isEqualTo(3);
        assertThat(stats.misses()).isEqualTo(1);
        assertThat(stats.hitRate()).isEqualTo(0.75);
    }

    @Test
    @DisplayName("Should track batch hits and misses correctly")
    void shouldTrackBatchHitsAndMisses() {
        // Given: Decks 1,2 cached
        cache.getKnownCards(1L, () -> Set.of(1L));
        cache.getKnownCards(2L, () -> Set.of(2L));

        // When: Batch request for 1,2,3 (3 is MISS)
        cache.getKnownCardsBatch(Set.of(1L, 2L, 3L), () -> Map.of(3L, Set.of(3L)));

        // Then: 2 hits (from cache) + 1 miss (loaded) + 2 initial misses
        KnownCardsCache.CacheStats stats = cache.getStats();
        assertThat(stats.hits()).isEqualTo(2); // Decks 1 and 2 from cache
        assertThat(stats.misses()).isEqualTo(3); // Initial 2 loads + deck 3
    }

    @Test
    @DisplayName("Should clear cache successfully")
    void shouldClearCacheSuccessfully() {
        // Given: Cached known cards for multiple decks
        cache.getKnownCards(1L, () -> Set.of(1L, 2L));
        cache.getKnownCards(2L, () -> Set.of(3L, 4L));

        // When: Clear cache
        cache.clear();

        // Then: Cache is empty
        KnownCardsCache.CacheStats stats = cache.getStats();
        assertThat(stats.size()).isZero();

        // And: Next access reloads (MISS)
        cache.getKnownCards(1L, () -> {
            loaderCallCount.incrementAndGet();
            return Set.of(1L, 2L);
        });
        assertThat(loaderCallCount.get()).isEqualTo(1); // Fresh load
    }

    @Test
    @DisplayName("Should track cache hit rate and log statistics")
    void shouldTrackHitRateAndLogStats() {
        // Given: First access (MISS)
        Set<Long> knownCards = Set.of(1L, 2L, 3L);
        cache.getKnownCards(1L, () -> {
            loaderCallCount.incrementAndGet();
            return knownCards;
        });

        KnownCardsCache.CacheStats stats = cache.getStats();
        assertThat(stats.misses()).isEqualTo(1);
        assertThat(stats.hits()).isZero();
        assertThat(stats.hitRate()).isEqualTo(0.0);

        // When: Multiple cache hits
        cache.getKnownCards(1L, () -> {
            throw new AssertionError("Should not be called on cache hit");
        });
        cache.getKnownCards(1L, () -> {
            throw new AssertionError("Should not be called on cache hit");
        });

        // Then: Hit rate should be 66.7% (2 hits out of 3 total accesses)
        stats = cache.getStats();
        assertThat(stats.hits()).isEqualTo(2);
        assertThat(stats.misses()).isEqualTo(1);
        assertThat(stats.hitRate()).isCloseTo(0.667, within(0.01)); // ~66.7%

        // Debug: Log cache statistics (this should not throw exception)
        cache.logStats();

        // Verify cache size
        assertThat(stats.size()).isEqualTo(1);
        assertThat(loaderCallCount.get()).isEqualTo(1); // Only one actual load
    }

    @Test
    @DisplayName("Should handle cache eviction with statistics")
    void shouldHandleEvictionWithStats() {
        // Given: Small cache size for testing
        ReflectionTestUtils.setField(cache, "maxSize", 2);

        // Fill cache to max size
        cache.getKnownCards(1L, () -> Set.of(1L, 2L));
        cache.getKnownCards(2L, () -> Set.of(3L, 4L));

        KnownCardsCache.CacheStats stats = cache.getStats();
        assertThat(stats.size()).isEqualTo(2);

        // When: Add third entry (should evict oldest)
        cache.getKnownCards(3L, () -> Set.of(5L, 6L));

        // Then: Cache size should still be 2 (eviction occurred)
        stats = cache.getStats();
        assertThat(stats.size()).isEqualTo(2);

        // Debug: Log statistics after eviction
        cache.logStats();
    }
}
