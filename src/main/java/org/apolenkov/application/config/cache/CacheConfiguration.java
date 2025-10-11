package org.apolenkov.application.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration using Caffeine for application-level caching.
 * Provides in-memory cache for frequently accessed data like users.
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    /**
     * Cache name constants.
     */
    public static final String USER_CACHE = "users";

    public static final String USER_BY_EMAIL_CACHE = "usersByEmail";

    @Value("${app.cache.user.ttl-minutes:30}")
    private int userCacheTtlMinutes;

    @Value("${app.cache.user.max-size:1000}")
    private int userCacheMaxSize;

    /**
     * Creates and configures Caffeine-based cache manager.
     * Uses TTL (Time-To-Live) and size-based eviction.
     *
     * @return configured cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(USER_CACHE, USER_BY_EMAIL_CACHE);
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Configures Caffeine cache with TTL and maximum size.
     *
     * <p>Configuration:
     * <ul>
     *   <li>TTL: 30 minutes (configurable via property)</li>
     *   <li>Max size: 1000 entries (configurable via property)</li>
     *   <li>Stats: enabled for monitoring</li>
     * </ul>
     *
     * @return Caffeine builder with configuration
     */
    Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(userCacheTtlMinutes, TimeUnit.MINUTES)
                .maximumSize(userCacheMaxSize)
                .recordStats();
    }
}
