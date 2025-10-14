package org.apolenkov.application.views.deck.cache;

import com.vaadin.flow.spring.annotation.UIScope;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.apolenkov.application.domain.event.DeckModifiedEvent;
import org.apolenkov.application.model.Deck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * UI-scoped cache for user decks.
 * Cache lifetime is bound to UI session and automatically cleared when UI is destroyed.
 * Data freshness is ensured through automatic expiration and event-based invalidation.
 *
 * <p>Configuration:
 * <ul>
 *   <li>TTL: configurable via app.cache.decks.ttl-ms (default: 60 seconds)</li>
 *   <li>Max size: configurable via app.cache.decks.max-size (default: 1000 entries)</li>
 *   <li>Eviction: LRU-style when cache reaches max size</li>
 * </ul>
 */
@Component
@UIScope
public class UserDecksCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDecksCache.class);

    private final Map<Long, CachedDecks> cache = new ConcurrentHashMap<>();
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();

    @Value("${app.cache.decks.ttl-ms:60000}")
    private long ttlMs;

    @Value("${app.cache.decks.max-size:1000}")
    private int maxSize;

    /**
     * Gets decks for a user.
     * Uses cached data when available and valid, otherwise loads fresh data.
     *
     * @param userId the user ID
     * @param loader supplier to load decks when cache miss occurs
     * @return list of user's decks, never null
     */
    public List<Deck> getDecks(final Long userId, final Supplier<List<Deck>> loader) {
        if (userId == null) {
            LOGGER.warn("Cannot get decks: userId is null");
            return List.of();
        }

        CachedDecks cached = cache.get(userId);

        if (cached != null && cached.isValid(ttlMs)) {
            hitCount.incrementAndGet();
            LOGGER.debug("Cache HIT: Returning {} decks for userId={}", cached.decks.size(), userId);
            return cached.decks;
        }

        missCount.incrementAndGet();
        LOGGER.debug("Cache MISS: Loading decks for userId={}", userId);
        List<Deck> decks = loader.get();

        // Evict the oldest entry if cache is full
        if (cache.size() >= maxSize) {
            evictOldest();
        }

        cache.put(userId, new CachedDecks(decks));
        LOGGER.debug("Cache updated: {} decks cached for userId={}", decks.size(), userId);

        return decks;
    }

    /**
     * Invalidates cache for a specific user.
     * Call after deck modifications to ensure data freshness.
     *
     * @param userId the user ID
     */
    public void invalidate(final Long userId) {
        if (userId != null) {
            cache.remove(userId);
            LOGGER.debug("Cache invalidated for userId={}", userId);
        }
    }

    /**
     * Clears all cache entries.
     * Use for testing or when global cache invalidation is needed.
     */
    public void clear() {
        cache.clear();
        LOGGER.debug("Cache cleared: all entries removed");
    }

    /**
     * Returns cache statistics for monitoring and testing.
     *
     * @return cache statistics including hit/miss counts and current size
     */
    public CacheStats getStats() {
        return new CacheStats(hitCount.get(), missCount.get(), cache.size());
    }

    /**
     * Logs cache statistics at DEBUG level.
     * Call periodically or on demand for monitoring.
     */
    public void logStats() {
        if (LOGGER.isDebugEnabled()) {
            CacheStats stats = getStats();
            String hitRate = String.format("%.1f%%", stats.hitRate() * 100);
            LOGGER.debug(
                    "Cache stats: hits={}, misses={}, hitRate={}, size={}, maxSize={}",
                    stats.hits(),
                    stats.misses(),
                    hitRate,
                    stats.size(),
                    maxSize);
        }
    }

    /**
     * Evicts oldest cache entry (LRU-style).
     * Called when cache reaches max size.
     */
    private void evictOldest() {
        cache.entrySet().stream()
                .min(Comparator.comparing(e -> e.getValue().cachedAt))
                .ifPresent(oldest -> {
                    cache.remove(oldest.getKey());
                    LOGGER.debug("Cache eviction: removed oldest entry for userId={}", oldest.getKey());
                });
    }

    /**
     * Handles deck modification events to invalidate cache.
     * Ensures UI displays current data after deck changes.
     *
     * @param event the deck modified event
     */
    @EventListener
    public void onDeckModified(final DeckModifiedEvent event) {
        LOGGER.debug(
                "Deck modified event received: userId={}, deckId={}, type={}",
                event.getUserId(),
                event.getDeckId(),
                event.getType());
        invalidate(event.getUserId());
    }

    // ==================== Inner Classes ====================

    /**
     * Cached decks with TTL.
     */
    private static final class CachedDecks {
        private final List<Deck> decks;
        private final Instant cachedAt;

        CachedDecks(final List<Deck> decksList) {
            this.decks = List.copyOf(decksList); // Immutable copy
            this.cachedAt = Instant.now();
        }

        boolean isValid(final long ttlMs) {
            return Instant.now().isBefore(cachedAt.plusMillis(ttlMs));
        }
    }

    /**
     * Cache statistics record for monitoring and testing.
     *
     * @param hits number of cache hits
     * @param misses number of cache misses
     * @param size current cache size
     */
    public record CacheStats(long hits, long misses, int size) {
        /**
         * Calculates hit rate (0.0 to 1.0).
         *
         * @return hit rate percentage as double
         */
        public double hitRate() {
            long total = hits + misses;
            return total > 0 ? (double) hits / total : 0.0;
        }
    }
}
