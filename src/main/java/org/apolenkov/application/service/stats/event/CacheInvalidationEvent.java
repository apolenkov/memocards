package org.apolenkov.application.service.stats.event;

import java.time.Instant;

/**
 * Event published when cache invalidation occurs.
 * Used for metrics collection and monitoring cache behavior.
 *
 * @param cacheType the type of cache being invalidated
 * @param key the cache key being invalidated
 * @param reason the reason for invalidation (save, delete, etc.)
 * @param timestamp when the invalidation occurred
 */
public record CacheInvalidationEvent(String cacheType, Object key, String reason, Instant timestamp) {

    /**
     * Creates a new CacheInvalidationEvent with current timestamp.
     *
     * @param cacheType the type of cache being invalidated
     * @param key the cache key being invalidated
     * @param reason the reason for invalidation
     * @return new CacheInvalidationEvent
     */
    public static CacheInvalidationEvent of(final String cacheType, final Object key, final String reason) {
        return new CacheInvalidationEvent(cacheType, key, reason, Instant.now());
    }

    /**
     * Gets the cache type for metrics tagging.
     *
     * @return cache type string
     */
    public String getCacheType() {
        return cacheType;
    }

    /**
     * Gets the invalidation reason for metrics tagging.
     *
     * @return reason string
     */
    public String getReason() {
        return reason;
    }

    /**
     * Gets the cache key as string for logging.
     *
     * @return key as string
     */
    public String getKeyAsString() {
        return key != null ? key.toString() : "null";
    }
}
