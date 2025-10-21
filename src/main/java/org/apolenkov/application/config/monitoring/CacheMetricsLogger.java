package org.apolenkov.application.config.monitoring;

import org.apolenkov.application.service.stats.PaginationCountCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled logger for PaginationCountCache metrics.
 * Monitors cache effectiveness and logs statistics at regular intervals.
 *
 * <p>Helps identify cache hit rates, optimization opportunities, and debouncing effectiveness.
 * Can be disabled via application properties.
 *
 * <p>Note: VaadinSessionScope caches (KnownCardsCache, UserDecksCache) cannot be injected
 * into singleton @Component due to scope mismatch. They should be monitored via their own
 * logStats() methods when needed.
 */
@Component
@ConditionalOnProperty(name = "app.monitoring.cache.enabled", havingValue = "true")
public class CacheMetricsLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheMetricsLogger.class);
    private static final double LOW_HIT_RATE_THRESHOLD = 0.5; // 50%
    private static final String HIT_RATE_FORMAT = "%.1f%%";

    private final ObjectProvider<PaginationCountCache> paginationCountCacheProvider;

    /**
     * Creates CacheMetricsLogger with session-scoped cache.
     *
     * @param paginationCountCacheProviderValue provider for pagination count cache (SessionScope)
     */
    public CacheMetricsLogger(final ObjectProvider<PaginationCountCache> paginationCountCacheProviderValue) {
        this.paginationCountCacheProvider = paginationCountCacheProviderValue;
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

        // PaginationCountCache metrics (SessionScope - injected via ObjectProvider)
        try {
            PaginationCountCache cache = paginationCountCacheProvider.getIfAvailable();
            if (cache != null) {
                PaginationCountCache.CacheStats stats = cache.getStats();
                logPaginationCacheStats(stats);
            } else {
                LOGGER.debug("PaginationCountCache not available (no active session)");
            }
        } catch (org.springframework.beans.factory.support.ScopeNotActiveException e) {
            LOGGER.debug("PaginationCountCache not available (session scope not active)");
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve PaginationCountCache metrics", e);
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
}
