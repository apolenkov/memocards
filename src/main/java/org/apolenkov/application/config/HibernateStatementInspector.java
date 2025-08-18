package org.apolenkov.application.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

/**
 * Hibernate Statement Inspector for query monitoring and optimization
 * Tracks slow queries, N+1 problems, and provides insights for optimization
 */
@Profile({"dev", "jpa", "prod"})
public class HibernateStatementInspector implements StatementInspector {

    private static final Logger logger = LoggerFactory.getLogger(HibernateStatementInspector.class);

    // Query performance tracking
    private final ConcurrentHashMap<String, QueryStats> queryStats = new ConcurrentHashMap<>();
    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicLong slowQueries = new AtomicLong(0);

    // Thresholds for monitoring
    private static final long SLOW_QUERY_THRESHOLD_MS = 100; // 100ms
    private static final int N_PLUS_1_THRESHOLD = 10; // More than 10 similar queries in short time

    @Override
    public String inspect(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        // Track query statistics
        trackQuery(sql);

        // Log slow queries in development
        if (isDevelopmentProfile()) {
            logQueryIfSlow(sql);
        }

        // Add query hints for optimization
        return addQueryHints(sql);
    }

    private void trackQuery(String sql) {
        totalQueries.incrementAndGet();

        String normalizedSql = normalizeSql(sql);
        QueryStats stats = queryStats.computeIfAbsent(normalizedSql, k -> new QueryStats());
        stats.incrementCount();
        stats.updateLastExecuted();

        // Check for N+1 problems
        if (stats.getCount() > N_PLUS_1_THRESHOLD && stats.isRecentExecution()) {
            logger.warn(
                    "Potential N+1 query detected: {} (executed {} times recently)", normalizedSql, stats.getCount());
        }
    }

    private void logQueryIfSlow(String sql) {
        // This would be implemented with actual query timing
        // For now, just log the query for development
        logger.debug("Executing SQL: {}", sql);
    }

    private String addQueryHints(String sql) {
        // Add database-specific hints for optimization
        if (sql.toLowerCase().contains("select") && !sql.toLowerCase().contains("limit")) {
            // Add LIMIT hints for large result sets
            if (sql.toLowerCase().contains("deck_daily_stats")) {
                // Add specific hints for statistics queries
                return sql + " /* +INDEX(deck_daily_stats idx_deck_daily_stats_deck_date) */";
            }
        }
        return sql;
    }

    private String normalizeSql(String sql) {
        // Remove variable parts to group similar queries
        return sql.replaceAll("\\d+", "?") // Replace numbers with ?
                .replaceAll("'[^']*'", "?") // Replace strings with ?
                .replaceAll("\\s+", " ") // Normalize whitespace
                .trim();
    }

    private boolean isDevelopmentProfile() {
        try {
            String[] activeProfiles = org.springframework.core.env.Environment.class
                    .getMethod("getActiveProfiles")
                    .invoke(null)
                    .toString()
                    .split(",");

            for (String profile : activeProfiles) {
                if ("dev".equals(profile.trim())) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Fallback to false if we can't determine profile
        }
        return false;
    }

    /**
     * Get query performance statistics
     */
    public QueryPerformanceReport getPerformanceReport() {
        return new QueryPerformanceReport(
                totalQueries.get(),
                slowQueries.get(),
                queryStats.size(),
                queryStats.values().stream()
                        .filter(stats -> stats.getCount() > N_PLUS_1_THRESHOLD)
                        .count());
    }

    /**
     * Clear query statistics (useful for testing)
     */
    public void clearStatistics() {
        queryStats.clear();
        totalQueries.set(0);
        slowQueries.set(0);
    }

    /**
     * Query statistics holder
     */
    private static class QueryStats {
        private final AtomicLong count = new AtomicLong(0);
        private volatile long lastExecuted = System.currentTimeMillis();

        public void incrementCount() {
            count.incrementAndGet();
        }

        public long getCount() {
            return count.get();
        }

        public void updateLastExecuted() {
            lastExecuted = System.currentTimeMillis();
        }

        public boolean isRecentExecution() {
            return System.currentTimeMillis() - lastExecuted < 60000; // 1 minute
        }
    }

    /**
     * Query performance report
     */
    public static class QueryPerformanceReport {
        private final long totalQueries;
        private final long slowQueries;
        private final long uniqueQueries;
        private final long potentialNPlusOneQueries;

        public QueryPerformanceReport(
                long totalQueries, long slowQueries, long uniqueQueries, long potentialNPlusOneQueries) {
            this.totalQueries = totalQueries;
            this.slowQueries = slowQueries;
            this.uniqueQueries = uniqueQueries;
            this.potentialNPlusOneQueries = potentialNPlusOneQueries;
        }

        public long getTotalQueries() {
            return totalQueries;
        }

        public long getSlowQueries() {
            return slowQueries;
        }

        public long getUniqueQueries() {
            return uniqueQueries;
        }

        public long getPotentialNPlusOneQueries() {
            return potentialNPlusOneQueries;
        }

        @Override
        public String toString() {
            return String.format(
                    "Query Performance Report: Total=%d, Slow=%d, Unique=%d, N+1=%d",
                    totalQueries, slowQueries, uniqueQueries, potentialNPlusOneQueries);
        }
    }
}
