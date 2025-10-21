package org.apolenkov.application.service.stats.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.apolenkov.application.service.stats.event.CacheInvalidationEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for CacheMetricsCollector component.
 * Verifies proper metrics collection for cache invalidation events.
 */
class CacheMetricsCollectorTest {

    private MeterRegistry meterRegistry;
    private CacheMetricsCollector metricsCollector;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsCollector = new CacheMetricsCollector(meterRegistry);
    }

    @Test
    @DisplayName("Should record cache invalidation event metrics")
    void shouldRecordCacheInvalidationEventMetrics() {
        // Given
        CacheInvalidationEvent event = CacheInvalidationEvent.of("pagination-count", 123L, "card-created");

        // When
        metricsCollector.onCacheInvalidation(event);

        // Then
        Counter invalidationCounter = meterRegistry
                .find("cache.invalidation.count")
                .tag("cache_type", "pagination-count")
                .tag("reason", "card-created")
                .counter();
        assertThat(invalidationCounter).isNotNull();
        assertThat(invalidationCounter.count()).isEqualTo(1.0);

        Counter totalCounter = meterRegistry.find("cache.invalidation.total").counter();
        assertThat(totalCounter).isNotNull();
        assertThat(totalCounter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should record cache invalidation timing")
    void shouldRecordCacheInvalidationTiming() {
        // Given
        Duration duration = Duration.ofMillis(150);

        // When
        metricsCollector.recordInvalidationTiming("pagination-count", "card-created", () -> duration);

        // Then
        Timer timer = meterRegistry
                .find("cache.invalidation.timing")
                .tag("cache_type", "pagination-count")
                .tag("reason", "card-created")
                .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should record cache hit metrics")
    void shouldRecordCacheHitMetrics() {
        // When
        metricsCollector.recordCacheHitMiss("pagination-count", true);

        // Then
        Counter counter = meterRegistry
                .find("cache.access.total")
                .tag("cache_type", "pagination-count")
                .tag("result", "hit")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should record cache miss metrics")
    void shouldRecordCacheMissMetrics() {
        // When
        metricsCollector.recordCacheHitMiss("pagination-count", false);

        // Then
        Counter counter = meterRegistry
                .find("cache.access.total")
                .tag("cache_type", "pagination-count")
                .tag("result", "miss")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should record cache size metrics")
    void shouldRecordCacheSizeMetrics() {
        // When
        metricsCollector.recordCacheSize("pagination-count", 42);

        // Then
        var gauge = meterRegistry
                .find("cache.size")
                .tag("cache_type", "pagination-count")
                .gauge();
        assertThat(gauge).isNotNull();
        assertThat(gauge.value()).isEqualTo(42.0);
    }

    @Test
    @DisplayName("Should handle multiple invalidation events")
    void shouldHandleMultipleInvalidationEvents() {
        // Given
        CacheInvalidationEvent event1 = CacheInvalidationEvent.of("cache1", 1L, "reason1");
        CacheInvalidationEvent event2 = CacheInvalidationEvent.of("cache2", 2L, "reason2");

        // When
        metricsCollector.onCacheInvalidation(event1);
        metricsCollector.onCacheInvalidation(event2);

        // Then
        Counter totalCounter = meterRegistry.find("cache.invalidation.total").counter();
        Assertions.assertNotNull(totalCounter);
        assertThat(totalCounter.count()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("Should handle exception in timing recording gracefully")
    void shouldHandleExceptionInTimingRecordingGracefully() {
        // Given
        RuntimeException exception = new RuntimeException("Test exception");

        // When & Then - should not throw exception
        metricsCollector.recordInvalidationTiming("cache", "reason", () -> {
            throw exception;
        });

        // Verify no exception was propagated
        assertThat(metricsCollector.getInvalidationCount()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should get invalidation count for testing")
    void shouldGetInvalidationCountForTesting() {
        // Given
        CacheInvalidationEvent event = CacheInvalidationEvent.of("cache", 123L, "reason");
        metricsCollector.onCacheInvalidation(event);

        // When
        double count = metricsCollector.getInvalidationCount();

        // Then
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should get invalidation timer for testing")
    void shouldGetInvalidationTimerForTesting() {
        // When
        Timer timer = metricsCollector.getInvalidationTimer();

        // Then
        assertThat(timer).isNotNull();
    }
}
