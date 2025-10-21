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
import org.apolenkov.application.service.stats.event.CacheInvalidationEvent;
import org.apolenkov.application.service.stats.metrics.CacheMetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
 *   <li>Smart invalidation: FilterOption.ALL is NOT invalidated on progress change (count unchanged)</li>
 *   <li>Debouncing: 300ms cooldown prevents excessive invalidations during rapid clicks (balanced UX/performance)</li>
 *   <li>TTL-based: configurable backup fallback</li>
 * </ul>
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PaginationCountCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaginationCountCache.class);
    private static final long INVALIDATION_COOLDOWN_MS = 300; // 300ms debouncing (reduced from 2s for better UX)
    private static final String CACHE_TYPE = "pagination-count";

    private final Map<CountKey, CachedCount> cache = new ConcurrentHashMap<>();
    private final Map<Long, Instant> lastInvalidationTime = new ConcurrentHashMap<>();
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();
    private final AtomicLong skippedInvalidations = new AtomicLong();

    @Value("${app.cache.pagination-count.ttl-ms:30000}")
    private long ttlMs;

    @Value("${app.cache.pagination-count.max-size:500}")
    private int maxSize;

    // Dependencies for metrics and events
    private final ApplicationEventPublisher eventPublisher;
    private final CacheMetricsCollector metricsCollector;

    /**
     * Creates PaginationCountCache with required dependencies.
     *
     * @param eventPublisherValue the Spring event publisher for cache invalidation events
     * @param metricsCollectorValue the metrics collector for cache statistics
     */
    public PaginationCountCache(
            final ApplicationEventPublisher eventPublisherValue, final CacheMetricsCollector metricsCollectorValue) {
        this.eventPublisher = eventPublisherValue;
        this.metricsCollector = metricsCollectorValue;
    }

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
            // Record cache hit metrics
            metricsCollector.recordCacheHitMiss(CACHE_TYPE, true);

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
        // Record cache miss metrics
        metricsCollector.recordCacheHitMiss(CACHE_TYPE, false);
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
     * Publishes cache invalidation event for metrics collection.
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

        if (removed > 0) {
            // Publish cache invalidation event for metrics
            CacheInvalidationEvent event = CacheInvalidationEvent.of(CACHE_TYPE, deckId, "deck-modified");
            eventPublisher.publishEvent(event);

            // Record metrics
            metricsCollector.recordCacheSize(CACHE_TYPE, cache.size());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("COUNT cache invalidated for deckId={}: {} entries removed", deckId, removed);
            }
        }
    }

    /**
     * Invalidates cache entries for a specific deck and filter option.
     * Smart invalidation: only removes entries that are affected by the change.
     *
     * @param deckId the deck ID
     * @param filterOption the filter option to invalidate
     */
    private void invalidateByFilter(final Long deckId, final FilterOption filterOption) {
        if (deckId == null || filterOption == null) {
            return;
        }

        long removed = cache.keySet().stream()
                .filter(key -> deckId.equals(key.deckId()) && filterOption.equals(key.filterOption()))
                .count();

        cache.keySet().removeIf(key -> deckId.equals(key.deckId()) && filterOption.equals(key.filterOption()));

        if (removed > 0 && LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "COUNT cache invalidated for deckId={}, filter={}: {} entries removed",
                    deckId,
                    filterOption,
                    removed);
        }
    }

    /**
     * Checks if deck is in invalidation cooldown period.
     * Prevents excessive invalidations during rapid user interactions (practice mode).
     *
     * <p>Note: Cooldown reduced to 300ms for better UX (was 2s).
     * This ensures counts update quickly enough while still preventing DB spam.
     * UI-level debouncing (beforeClientResponse/UI.access) provides additional protection.
     *
     * @param deckId the deck ID
     * @return true if in cooldown, false otherwise
     */
    private boolean isInCooldown(final Long deckId) {
        Instant lastInvalidation = lastInvalidationTime.get(deckId);
        if (lastInvalidation == null) {
            return false;
        }

        Instant now = Instant.now();
        return now.isBefore(lastInvalidation.plusMillis(INVALIDATION_COOLDOWN_MS));
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
     * <p>Optimization strategies:
     * <ul>
     *   <li>Smart invalidation: FilterOption.ALL is NOT invalidated (total count unchanged)</li>
     *   <li>Debouncing: 300ms cooldown prevents excessive invalidations during rapid clicks</li>
     *   <li>Selective: Only KNOWN_ONLY and UNKNOWN_ONLY filters are invalidated</li>
     * </ul>
     *
     * <p>Performance note: 300ms cooldown balances UX and performance.
     * UI-level debouncing (UI.access) provides additional layer of protection against excessive updates.
     *
     * @param event progress changed event
     */
    @EventListener
    public void onProgressChanged(final ProgressChangedEvent event) {
        if (event == null) {
            return;
        }

        Long deckId = event.getDeckId();

        // Debouncing: Skip if last invalidation was < 300ms ago
        if (isInCooldown(deckId)) {
            skippedInvalidations.incrementAndGet();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "COUNT cache invalidation skipped (cooldown): deckId={}, changeType={}",
                        deckId,
                        event.getChangeType());
            }
            return;
        }

        // Smart invalidation: Only invalidate filters affected by card status change
        // FilterOption.ALL count doesn't change when card status toggles (total remains same)
        invalidateByFilter(deckId, FilterOption.KNOWN_ONLY);
        invalidateByFilter(deckId, FilterOption.UNKNOWN_ONLY);

        // Update last invalidation time for debouncing
        lastInvalidationTime.put(deckId, Instant.now());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "COUNT cache smart-invalidated on ProgressChangedEvent: deckId={}, changeType={}, filters=[KNOWN_ONLY, UNKNOWN_ONLY]",
                    deckId,
                    event.getChangeType());
        }
    }

    /**
     * Returns cache statistics for monitoring and testing.
     *
     * @return cache statistics including hit/miss counts, current size, and skipped invalidations
     */
    public CacheStats getStats() {
        return new CacheStats(hitCount.get(), missCount.get(), cache.size(), skippedInvalidations.get());
    }

    /**
     * Logs cache statistics at DEBUG level.
     * Useful for monitoring cache effectiveness and debouncing impact.
     */
    public void logStats() {
        if (LOGGER.isDebugEnabled()) {
            CacheStats stats = getStats();
            long total = stats.hits() + stats.misses();
            String hitRate = total > 0 ? String.format("%.1f%%", (double) stats.hits() / total * 100) : "N/A";
            LOGGER.debug(
                    "COUNT cache stats: hits={}, misses={}, hitRate={}, size={}, skippedInvalidations={}",
                    stats.hits(),
                    stats.misses(),
                    hitRate,
                    stats.size(),
                    stats.skippedInvalidations());
        }
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
     * @param skippedInvalidations number of invalidations skipped due to debouncing
     */
    public record CacheStats(long hits, long misses, int size, long skippedInvalidations) {
        /**
         * Calculates cache hit rate.
         *
         * @return hit rate as percentage (0.0 to 1.0)
         */
        public double hitRate() {
            long total = hits + misses;
            return total > 0 ? (double) hits / total : 0.0;
        }
    }
}
