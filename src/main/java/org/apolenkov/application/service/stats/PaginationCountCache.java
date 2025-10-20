package org.apolenkov.application.service.stats;

import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import org.apolenkov.application.domain.event.DeckModifiedEvent;
import org.apolenkov.application.domain.event.ProgressChangedEvent;
import org.apolenkov.application.domain.model.FilterOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * Session-scoped cache for pagination COUNT queries.
 * Reduces redundant COUNT queries during rapid page navigation.
 * Cache lifetime is bound to HTTP session (survives page navigation, shared across tabs).
 *
 * <p>Configuration:
 * <ul>
 *   <li>TTL: configurable via app.cache.pagination-count.ttl-ms (default: 30 seconds)</li>
 *   <li>Max size: configurable via app.cache.pagination-count.max-size (default: 500 entries)</li>
 *   <li>Eviction: LRU-style when cache reaches max size</li>
 *   <li>Scope: @SessionScope - shared across requests in same HTTP session</li>
 * </ul>
 *
 * <p>Invalidation strategy:
 * <ul>
 *   <li>Event-driven: DeckModifiedEvent (deck delete invalidates all counts)</li>
 *   <li>Event-driven: ProgressChangedEvent (known/unknown status change)</li>
 *   <li>TTL-based: 30 seconds backup fallback</li>
 * </ul>
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PaginationCountCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaginationCountCache.class);

    private final Map<CountKey, CachedCount> cache = new ConcurrentHashMap<>();
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();

    @Value("${app.cache.pagination-count.ttl-ms:30000}")
    private long ttlMs;

    @Value("${app.cache.pagination-count.max-size:500}")
    private int maxSize;

    /**
     * Gets cached count or loads fresh count if cache miss.
     *
     * @param deckId the deck ID
     * @param searchQuery the search query (null or empty for no search)
     * @param filterOption the filter option
     * @param loader supplier to load count when cache miss occurs
     * @return count of flashcards matching criteria
     */
    public long getCount(
            final Long deckId, final String searchQuery, final FilterOption filterOption, final LongSupplier loader) {
        if (deckId == null || filterOption == null) {
            LOGGER.warn("Cannot get count: deckId or filterOption is null");
            return loader.getAsLong();
        }

        CountKey key = new CountKey(deckId, normalizeSearch(searchQuery), filterOption);
        CachedCount cached = cache.get(key);

        if (cached != null && cached.isValid(ttlMs)) {
            hitCount.incrementAndGet();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "COUNT cache HIT: deckId={}, filter={}, search='{}', count={}",
                        deckId,
                        filterOption,
                        key.searchQuery(),
                        cached.count());
            }
            return cached.count();
        }

        missCount.incrementAndGet();
        long count = loader.getAsLong();

        // Evict the oldest entry if cache is full
        if (cache.size() >= maxSize) {
            evictOldest();
        }

        cache.put(key, new CachedCount(count, Instant.now()));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "COUNT cache MISS: deckId={}, filter={}, search='{}', count={} (cached)",
                    deckId,
                    filterOption,
                    key.searchQuery(),
                    count);
        }

        return count;
    }

    /**
     * Invalidates all cache entries for a specific deck.
     * Called after flashcard create/update/delete or progress changes.
     *
     * @param deckId the deck ID
     */
    public void invalidate(final Long deckId) {
        if (deckId == null) {
            return;
        }

        long removed = cache.keySet().stream()
                .filter(key -> deckId.equals(key.deckId()))
                .count();

        cache.keySet().removeIf(key -> deckId.equals(key.deckId()));

        if (removed > 0 && LOGGER.isDebugEnabled()) {
            LOGGER.debug("COUNT cache invalidated for deckId={}: {} entries removed", deckId, removed);
        }
    }

    /**
     * Handles deck modified events for automatic cache invalidation.
     * Event-driven approach ensures data consistency after deck deletion.
     *
     * @param event deck modified event
     */
    @EventListener
    public void onDeckModified(final DeckModifiedEvent event) {
        if (event == null) {
            return;
        }

        invalidate(event.getDeckId());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "COUNT cache auto-invalidated on DeckModifiedEvent: deckId={}, modificationType={}",
                    event.getDeckId(),
                    event.getType());
        }
    }

    /**
     * Handles progress changed events for automatic cache invalidation.
     * Event-driven approach ensures data consistency after known/unknown status changes.
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
                    "COUNT cache auto-invalidated on ProgressChangedEvent: deckId={}, changeType={}",
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
     * Normalizes search query for cache key consistency.
     * Null and empty strings are treated as equivalent (no search).
     *
     * @param searchQuery the search query
     * @return normalized search query
     */
    private String normalizeSearch(final String searchQuery) {
        return (searchQuery == null || searchQuery.trim().isEmpty()) ? "" : searchQuery.trim();
    }

    /**
     * Evicts oldest cache entry (LRU-style).
     * Called when cache reaches max size.
     */
    private void evictOldest() {
        cache.entrySet().stream()
                .min(Comparator.comparing(e -> e.getValue().cachedAt()))
                .ifPresent(oldest -> {
                    cache.remove(oldest.getKey());
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("COUNT cache eviction: removed oldest entry for key={}", oldest.getKey());
                    }
                });
    }

    // ==================== Inner Classes ====================

    /**
     * Cache key for pagination count queries.
     * Composite key: (deckId, searchQuery, filterOption).
     *
     * @param deckId the deck ID
     * @param searchQuery the normalized search query (never null)
     * @param filterOption the filter option
     */
    record CountKey(Long deckId, String searchQuery, FilterOption filterOption) {
        CountKey {
            Objects.requireNonNull(deckId, "deckId cannot be null");
            Objects.requireNonNull(searchQuery, "searchQuery cannot be null");
            Objects.requireNonNull(filterOption, "filterOption cannot be null");
        }
    }

    /**
     * Cached count with TTL.
     *
     * @param count the cached count
     * @param cachedAt the time when cached
     */
    record CachedCount(long count, Instant cachedAt) {
        boolean isValid(final long ttlMillis) {
            return Instant.now().isBefore(cachedAt.plusMillis(ttlMillis));
        }
    }

    /**
     * Cache statistics record for monitoring and testing.
     *
     * @param hits number of cache hits
     * @param misses number of cache misses
     * @param size current cache size
     */
    public record CacheStats(long hits, long misses, int size) {}
}
