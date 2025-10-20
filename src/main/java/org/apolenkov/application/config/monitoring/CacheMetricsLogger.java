package org.apolenkov.application.config.monitoring;

import org.apolenkov.application.service.stats.KnownCardsCache;
import org.apolenkov.application.service.stats.PaginationCountCache;
import org.apolenkov.application.views.deck.cache.UserDecksCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled logger for cache metrics across all application caches.
 * Monitors cache effectiveness and logs statistics at regular intervals.
 *
 * <p>Helps identify cache hit rates, optimization opportunities, and debouncing effectiveness.
 * Can be disabled via application properties.
 *
 * <p>Monitored caches:
 * <ul>
 *   <li>PaginationCountCache - COUNT query results with debouncing</li>
 *   <li>KnownCardsCache - Known card IDs per deck</li>
 *   <li>UserDecksCache - User deck lists</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(name = "app.monitoring.cache.enabled", havingValue = "true")
public class CacheMetricsLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheMetricsLogger.class);
    private static final double LOW_HIT_RATE_THRESHOLD = 0.5; // 50%
    private static final String HIT_RATE_FORMAT = "%.1f%%";

    private final PaginationCountCache paginationCountCache;
    private final KnownCardsCache knownCardsCache;
    private final UserDecksCache userDecksCache;

    /**
     * Creates CacheMetricsLogger with all application caches.
     *
     * @param paginationCountCacheValue the pagination count cache
     * @param knownCardsCacheValue the known cards cache
     * @param userDecksCacheValue the user decks cache
     */
    public CacheMetricsLogger(
            final PaginationCountCache paginationCountCacheValue,
            final KnownCardsCache knownCardsCacheValue,
            final UserDecksCache userDecksCacheValue) {
        this.paginationCountCache = paginationCountCacheValue;
        this.knownCardsCache = knownCardsCacheValue;
        this.userDecksCache = userDecksCacheValue;
    }

    /**
     * Logs cache metrics at configured intervals.
     * Runs every 5 minutes by default (configurable via app.monitoring.cache.log-interval-ms).
     */
    @Scheduled(fixedDelayString = "${app.monitoring.cache.log-interval-ms:300000}")
    public void logCacheMetrics() {
        if (!LOGGER.isDebugEnabled()) {
            return; // Skip if DEBUG logging is disabled
        }

        LOGGER.debug("=== Cache Metrics Report ===");

        // PaginationCountCache metrics
        try {
            PaginationCountCache.CacheStats paginationStats = paginationCountCache.getStats();
            logPaginationCacheStats(paginationStats);
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve PaginationCountCache metrics", e);
        }

        // KnownCardsCache metrics
        try {
            KnownCardsCache.CacheStats knownCardsStats = knownCardsCache.getStats();
            logKnownCardsCacheStats(knownCardsStats);
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve KnownCardsCache metrics", e);
        }

        // UserDecksCache metrics
        try {
            UserDecksCache.CacheStats userDecksStats = userDecksCache.getStats();
            logUserDecksCacheStats(userDecksStats);
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve UserDecksCache metrics", e);
        }

        LOGGER.debug("=== End Cache Metrics ===");
    }

    /**
     * Logs PaginationCountCache statistics with debouncing metrics.
     *
     * @param stats the cache statistics
     */
    private void logPaginationCacheStats(final PaginationCountCache.CacheStats stats) {
        double hitRate = stats.hitRate();
        String hitRateFormatted = String.format(HIT_RATE_FORMAT, hitRate * 100);

        LOGGER.debug(
                "PaginationCountCache: hits={}, misses={}, hitRate={}, size={}, skippedInvalidations={}",
                stats.hits(),
                stats.misses(),
                hitRateFormatted,
                stats.size(),
                stats.skippedInvalidations());

        // Warn if hit rate is low
        if (hitRate < LOW_HIT_RATE_THRESHOLD && (stats.hits() + stats.misses()) > 10) {
            LOGGER.warn(
                    "PaginationCountCache has low hit rate: {} (consider increasing TTL or investigating invalidation patterns)",
                    hitRateFormatted);
        }

        // Info if debouncing is working well
        if (stats.skippedInvalidations() > 0) {
            LOGGER.debug(
                    "PaginationCountCache debouncing prevented {} excessive invalidations",
                    stats.skippedInvalidations());
        }
    }

    /**
     * Logs KnownCardsCache statistics.
     *
     * @param stats the cache statistics
     */
    private void logKnownCardsCacheStats(final KnownCardsCache.CacheStats stats) {
        double hitRate = stats.hitRate();
        String hitRateFormatted = String.format(HIT_RATE_FORMAT, hitRate * 100);

        LOGGER.debug(
                "KnownCardsCache: hits={}, misses={}, hitRate={}, size={}",
                stats.hits(),
                stats.misses(),
                hitRateFormatted,
                stats.size());

        // Warn if hit rate is low
        if (hitRate < LOW_HIT_RATE_THRESHOLD && (stats.hits() + stats.misses()) > 10) {
            LOGGER.warn(
                    "KnownCardsCache has low hit rate: {} (consider increasing TTL or investigating invalidation patterns)",
                    hitRateFormatted);
        }
    }

    /**
     * Logs UserDecksCache statistics.
     *
     * @param stats the cache statistics
     */
    private void logUserDecksCacheStats(final UserDecksCache.CacheStats stats) {
        double hitRate = stats.hitRate();
        String hitRateFormatted = String.format(HIT_RATE_FORMAT, hitRate * 100);

        LOGGER.debug(
                "UserDecksCache: hits={}, misses={}, hitRate={}, size={}",
                stats.hits(),
                stats.misses(),
                hitRateFormatted,
                stats.size());

        // Warn if hit rate is low
        if (hitRate < LOW_HIT_RATE_THRESHOLD && (stats.hits() + stats.misses()) > 10) {
            LOGGER.warn(
                    "UserDecksCache has low hit rate: {} (consider increasing TTL or investigating invalidation patterns)",
                    hitRateFormatted);
        }
    }
}
