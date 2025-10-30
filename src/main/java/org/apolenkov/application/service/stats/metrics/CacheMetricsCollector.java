package org.apolenkov.application.service.stats.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import org.apolenkov.application.service.stats.event.CacheInvalidationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Service for collecting cache invalidation metrics.
 * Uses Micrometer for metrics collection and Spring Events for decoupled monitoring.
 * Follows Spring Boot metrics best practices:
 * - Uses @EventListener for decoupled metrics collection
 * - Provides detailed metrics with tags for filtering
 * - Includes performance timing for cache operations
 */
@Component
public class CacheMetricsCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheMetricsCollector.class);

    // Constants for metrics tags
    private static final String APPLICATION_TAG = "application";
    private static final String APPLICATION_VALUE = "memo";
    private static final String CACHE_TYPE_TAG = "cache_type";
    private static final String REASON_TAG = "reason";
    private static final String RESULT_TAG = "result";

    // ==================== Fields ====================

    private final MeterRegistry meterRegistry;
    private final Counter invalidationCounter;
    private final Timer invalidationTimer;

    // ==================== Constructor ====================

    /**
     * Creates CacheMetricsCollector with required dependencies.
     *
     * @param meterRegistryValue the Micrometer meter registry for metrics collection
     */
    public CacheMetricsCollector(final MeterRegistry meterRegistryValue) {
        this.meterRegistry = meterRegistryValue;

        // Initialize counters and timers
        this.invalidationCounter = Counter.builder("cache.invalidation.total")
                .description("Total number of cache invalidations")
                .tag(APPLICATION_TAG, APPLICATION_VALUE)
                .register(meterRegistry);

        this.invalidationTimer = Timer.builder("cache.invalidation.duration")
                .description("Time taken for cache invalidation operations")
                .tag(APPLICATION_TAG, APPLICATION_VALUE)
                .register(meterRegistry);

        LOGGER.info("CacheMetricsCollector initialized with MeterRegistry");
    }

    // ==================== Event Listeners ====================

    /**
     * Handles cache invalidation events and records metrics.
     * Uses @EventListener for decoupled metrics collection.
     *
     * @param event the cache invalidation event
     */
    @EventListener
    public void onCacheInvalidation(final CacheInvalidationEvent event) {
        try {
            // Record invalidation counter with tags
            Counter.builder("cache.invalidation.count")
                    .description("Cache invalidation events by type and reason")
                    .tag(CACHE_TYPE_TAG, event.getCacheType())
                    .tag(REASON_TAG, event.getReason())
                    .tag(APPLICATION_TAG, APPLICATION_VALUE)
                    .register(meterRegistry)
                    .increment();

            // Record total invalidations
            invalidationCounter.increment();

            // Log for debugging (DEBUG level to avoid performance impact)
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "Cache invalidation recorded: type={}, key={}, reason={}",
                        event.getCacheType(),
                        event.getKeyAsString(),
                        event.getReason());
            }

        } catch (Exception e) {
            LOGGER.error("Failed to record cache invalidation metrics for event: {}", event, e);
        }
    }

    // ==================== Public API ====================

    /**
     * Records cache invalidation timing.
     * Use this method to measure how long cache invalidation takes.
     *
     * @param cacheType the type of cache
     * @param reason the reason for invalidation
     * @param durationSupplier supplier that performs the invalidation and returns duration
     */
    public void recordInvalidationTiming(
            final String cacheType, final String reason, final Supplier<Duration> durationSupplier) {
        try {
            Duration duration = durationSupplier.get();

            Timer.builder("cache.invalidation.timing")
                    .description("Detailed timing for cache invalidation operations")
                    .tag(CACHE_TYPE_TAG, cacheType)
                    .tag(REASON_TAG, reason)
                    .tag(APPLICATION_TAG, APPLICATION_VALUE)
                    .register(meterRegistry)
                    .record(duration);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "Cache invalidation timing recorded: type={}, reason={}, duration={}ms",
                        cacheType,
                        reason,
                        duration.toMillis());
            }

        } catch (Exception e) {
            LOGGER.error("Failed to record cache invalidation timing: type={}, reason={}", cacheType, reason, e);
        }
    }

    /**
     * Records cache hit/miss metrics.
     *
     * @param cacheType the type of cache
     * @param hit true if cache hit, false if miss
     */
    public void recordCacheHitMiss(final String cacheType, final boolean hit) {
        try {
            Counter.builder("cache.access.total")
                    .description("Cache access events")
                    .tag(CACHE_TYPE_TAG, cacheType)
                    .tag(RESULT_TAG, hit ? "hit" : "miss")
                    .tag(APPLICATION_TAG, APPLICATION_VALUE)
                    .register(meterRegistry)
                    .increment();

        } catch (Exception e) {
            LOGGER.error("Failed to record cache hit/miss metrics: type={}, hit={}", cacheType, hit, e);
        }
    }

    /**
     * Records cache size metrics.
     *
     * @param cacheType the type of cache
     * @param size current cache size
     */
    public void recordCacheSize(final String cacheType, final int size) {
        try {
            List<Tag> tags = List.of(Tag.of(CACHE_TYPE_TAG, cacheType), Tag.of(APPLICATION_TAG, APPLICATION_VALUE));

            meterRegistry.gauge("cache.size", tags, size);

        } catch (Exception e) {
            LOGGER.error("Failed to record cache size metrics: type={}, size={}", cacheType, size, e);
        }
    }

    /**
     * Gets current invalidation count for testing.
     *
     * @return current invalidation count
     */
    public double getInvalidationCount() {
        return invalidationCounter.count();
    }

    /**
     * Gets current invalidation timer for testing.
     *
     * @return invalidation timer
     */
    public Timer getInvalidationTimer() {
        return invalidationTimer;
    }
}
