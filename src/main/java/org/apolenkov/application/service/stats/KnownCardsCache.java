package org.apolenkov.application.service.stats;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.apolenkov.application.domain.event.ProgressChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Session-scoped cache for known card IDs.
 * Cache lifetime is bound to Vaadin session (survives page navigation, shared across tabs).
 * Data freshness is ensured through automatic expiration and manual invalidation.
 *
 * <p>Configuration:
 * <ul>
 *   <li>TTL: configurable via app.cache.known-cards.ttl-ms (default: 5 minutes)</li>
 *   <li>Max size: configurable via app.cache.known-cards.max-size (default: 1000 entries)</li>
 *   <li>Eviction: LRU-style when cache reaches max size</li>
 *   <li>Scope: @VaadinSessionScope - shared across UI instances in same session</li>
 * </ul>
 */
@Component
@VaadinSessionScope
public class KnownCardsCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(KnownCardsCache.class);

    private final Map<Long, CachedKnownCards> cache = new ConcurrentHashMap<>();
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();

    @Value("${app.cache.known-cards.ttl-ms:300000}")
    private long ttlMs;

    @Value("${app.cache.known-cards.max-size:1000}")
    private int maxSize;

    /**
     * Gets known card IDs for a deck.
     * Uses cached data when available and valid, otherwise loads fresh data.
     *
     * @param deckId the deck ID
     * @param loader supplier to load known card IDs when cache miss occurs
     * @return set of known card IDs, never null
     */
    public Set<Long> getKnownCards(final Long deckId, final Supplier<Set<Long>> loader) {
        if (deckId == null) {
            LOGGER.warn("Cannot get known cards: deckId is null");
            return Set.of();
        }

        CachedKnownCards cached = cache.get(deckId);

        if (cached != null && cached.isValid(ttlMs)) {
            hitCount.incrementAndGet();
            LOGGER.debug("Cache HIT: Returning {} known cards for deckId={}", cached.cardIds.size(), deckId);
            return cached.cardIds;
        }

        missCount.incrementAndGet();
        LOGGER.debug("Cache MISS: Loading known cards for deckId={}", deckId);
        Set<Long> cardIds = loader.get();

        // Evict the oldest entry if cache is full
        if (cache.size() >= maxSize) {
            evictOldest();
        }

        cache.put(deckId, new CachedKnownCards(cardIds));
        LOGGER.debug("Cache updated: {} known cards cached for deckId={}", cardIds.size(), deckId);

        return cardIds;
    }

    /**
     * Gets known card IDs for multiple decks in batch.
     * Uses cache for individual decks, loads missing ones in batch.
     *
     * @param deckIds deck IDs to get known cards for
     * @param batchLoader supplier to load known cards for missing deck IDs in batch
     * @return map of deck ID to known card IDs
     */
    public Map<Long, Set<Long>> getKnownCardsBatch(
            final Set<Long> deckIds, final Supplier<Map<Long, Set<Long>>> batchLoader) {
        if (deckIds == null || deckIds.isEmpty()) {
            return Map.of();
        }

        // Check which decks are already cached
        Map<Long, Set<Long>> result = new ConcurrentHashMap<>();
        Set<Long> missingDeckIds = ConcurrentHashMap.newKeySet();

        for (Long deckId : deckIds) {
            CachedKnownCards cached = cache.get(deckId);
            if (cached != null && cached.isValid(ttlMs)) {
                hitCount.incrementAndGet();
                result.put(deckId, cached.cardIds);
            } else {
                missCount.incrementAndGet();
                missingDeckIds.add(deckId);
            }
        }

        // Load missing decks in batch
        if (!missingDeckIds.isEmpty()) {
            LOGGER.debug("Cache MISS: Loading known cards for {} decks in batch", missingDeckIds.size());
            Map<Long, Set<Long>> loaded = batchLoader.get();

            // Cache newly loaded data
            loaded.forEach((deckId, cardIds) -> {
                // Evict the oldest entry if cache is full
                if (cache.size() >= maxSize) {
                    evictOldest();
                }
                cache.put(deckId, new CachedKnownCards(cardIds));
                result.put(deckId, cardIds);
            });
        }

        LOGGER.debug(
                "Batch retrieval: {} decks from cache, {} loaded from DB",
                deckIds.size() - missingDeckIds.size(),
                missingDeckIds.size());

        return result;
    }

    /**
     * Invalidates cache for a specific deck.
     * Call this after practice sessions or progress reset.
     *
     * @param deckId the deck ID
     */
    public void invalidate(final Long deckId) {
        if (deckId != null) {
            cache.remove(deckId);
            LOGGER.debug("Cache invalidated for deckId={}", deckId);
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
     * Handles progress changed events for automatic cache invalidation.
     * Event-driven approach decouples service layer from infrastructure (cache).
     *
     * <p>This method is automatically invoked by Spring when ProgressChangedEvent is published.
     * Supports both single card status changes and full deck resets.
     *
     * @param event progress changed event
     */
    @EventListener
    public void onProgressChanged(final ProgressChangedEvent event) {
        if (event == null) {
            return;
        }

        invalidate(event.getDeckId());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Cache auto-invalidated on event: deckId={}, changeType={}",
                    event.getDeckId(),
                    event.getChangeType());
        }
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
                    LOGGER.debug("Cache eviction: removed oldest entry for deckId={}", oldest.getKey());
                });
    }

    // ==================== Inner Classes ====================

    /**
     * Cached known cards with TTL.
     */
    private static final class CachedKnownCards {
        private final Set<Long> cardIds;
        private final Instant cachedAt;

        CachedKnownCards(final Set<Long> ids) {
            this.cardIds = Set.copyOf(ids); // Immutable copy
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
