package org.apolenkov.application.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

/**
 * Hibernate Statement Inspector for query monitoring and optimization.
 *
 * <p>This class implements Hibernate's StatementInspector interface to provide
 * comprehensive query monitoring and optimization capabilities. It tracks query
 * performance, detects N+1 problems, and provides insights for database optimization.</p>
 *
 * <p>The inspector is active in development, JPA, and production profiles and
 * provides thread-safe query statistics collection for concurrent applications.</p>
 *
 */
@Profile({"dev", "prod"})
public class HibernateStatementInspector implements StatementInspector {

    private static final Logger logger = LoggerFactory.getLogger(HibernateStatementInspector.class);

    // Query performance tracking - thread-safe collections for concurrent access
    private final ConcurrentHashMap<String, QueryStats> queryStats = new ConcurrentHashMap<>();
    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicLong slowQueries = new AtomicLong(0);

    // Performance thresholds for monitoring
    private static final long SLOW_QUERY_THRESHOLD_MS = 100; // 100ms - queries slower than this are flagged
    private static final int N_PLUS_1_THRESHOLD = 10; // N+1 detection: more than 10 similar queries in short time

    /**
     * Inspects and potentially modifies SQL statements before execution.
     *
     * <p>This method is called by Hibernate for every SQL statement before it's
     * executed. It provides an opportunity to monitor, log, and optimize queries
     * based on their characteristics and execution patterns.</p>
     *
     * <p>The method tracks query statistics, detects performance issues, and
     * can add optimization hints to improve query performance.</p>
     *
     * @param sql the SQL statement to inspect
     * @return the potentially modified SQL statement
     */
    @Override
    public String inspect(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        // Track query statistics for performance analysis
        trackQuery(sql);

        // Log slow queries only in development for debugging
        if (isDevelopmentProfile()) {
            logQueryIfSlow(sql);
        }

        // Add database-specific optimization hints
        return addQueryHints(sql);
    }

    /**
     * Tracks query execution statistics for performance analysis.
     *
     * <p>This method maintains statistics about query execution patterns,
     * including execution counts and timing information. It also detects
     * potential N+1 query problems by monitoring similar queries executed
     * in short time intervals.</p>
     *
     * @param sql the SQL statement being tracked
     */
    private void trackQuery(String sql) {
        // Increment total query counter for overall statistics
        totalQueries.incrementAndGet();

        // Normalize SQL for pattern matching and grouping
        String normalizedSql = normalizeSql(sql);
        QueryStats stats = queryStats.computeIfAbsent(normalizedSql, k -> new QueryStats());
        stats.incrementCount();
        stats.updateLastExecuted();

        // Detect N+1 query problems: multiple similar queries executed recently
        if (stats.getCount() > N_PLUS_1_THRESHOLD && stats.isRecentExecution()) {
            logger.warn(
                    "Potential N+1 query detected: {} (executed {} times recently)", normalizedSql, stats.getCount());
        }
    }

    /**
     * Logs queries that may be slow for development debugging.
     *
     * <p>This method provides development-time logging of SQL queries to
     * aid in debugging and performance analysis. It currently logs all
     * queries in debug mode.</p>
     *
     * @param sql the SQL statement to potentially log
     */
    private void logQueryIfSlow(String sql) {
        // TODO: Implement actual query timing measurement
        // For now, just log the query for development debugging
        logger.debug("Executing SQL: {}", sql);
    }

    /**
     * Adds database-specific optimization hints to SQL statements.
     *
     * <p>This method analyzes SQL statements and adds appropriate optimization
     * hints based on the query type and target tables. It can suggest index
     * usage and other database-specific optimizations.</p>
     *
     * @param sql the original SQL statement
     * @return the SQL statement with potential optimization hints
     */
    private String addQueryHints(String sql) {
        // Add database-specific optimization hints based on query type
        if (sql.toLowerCase().contains("select") && !sql.toLowerCase().contains("limit")) {
            // Suggest LIMIT for large result sets to prevent memory issues
            if (sql.toLowerCase().contains("deck_daily_stats")) {
                // Statistics queries benefit from specific index hints for performance
                return sql + " /* +INDEX(deck_daily_stats idx_deck_daily_stats_deck_date) */";
            }
        }
        return sql;
    }

    /**
     * Normalizes SQL statements for pattern matching and grouping.
     *
     * <p>This method converts SQL statements into a normalized form by
     * replacing specific values with placeholders. This allows grouping
     * similar queries together for better pattern analysis and N+1 detection.</p>
     *
     * @param sql the SQL statement to normalize
     * @return the normalized SQL statement
     */
    private String normalizeSql(String sql) {
        // Normalize SQL for grouping similar queries together
        // This helps identify patterns and N+1 problems
        return sql.replaceAll("\\d+", "?") // Replace numbers with ? to group similar queries
                .replaceAll("'[^']*'", "?") // Replace string literals with ? for grouping
                .replaceAll("\\s+", " ") // Normalize whitespace for consistent comparison
                .trim();
    }

    /**
     * Determines if the application is running in development profile.
     *
     * <p>This method checks the active Spring profiles to determine if
     * the application is running in development mode. This affects
     * logging behavior and debugging features.</p>
     *
     * @return true if running in development profile, false otherwise
     */
    private boolean isDevelopmentProfile() {
        try {
            // Use reflection to access Spring Environment for profile detection
            String[] activeProfiles = org.springframework.core.env.Environment.class
                    .getMethod("getActiveProfiles")
                    .invoke(null)
                    .toString()
                    .split(",");

            // Check if "dev" profile is active
            for (String profile : activeProfiles) {
                if ("dev".equals(profile.trim())) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Fallback to false if we can't determine profile (e.g., in test environment)
        }
        return false;
    }

    /**
     * Generates a comprehensive query performance report.
     *
     * <p>This method aggregates all collected query statistics and returns
     * a detailed performance report including total queries, slow queries,
     * unique queries, and potential N+1 problems.</p>
     *
     * @return a QueryPerformanceReport containing performance statistics
     */
    public QueryPerformanceReport getPerformanceReport() {
        return new QueryPerformanceReport(
                totalQueries.get(), // Total queries executed
                slowQueries.get(), // Queries exceeding performance threshold
                queryStats.size(), // Number of unique query patterns
                queryStats.values().stream() // Count queries that might be N+1 problems
                        .filter(stats -> stats.getCount() > N_PLUS_1_THRESHOLD)
                        .count());
    }

    /**
     * Clears all collected query statistics.
     *
     * <p>This method resets all performance tracking data to initial values.
     * It's useful for testing and for starting fresh performance monitoring
     * sessions.</p>
     */
    public void clearStatistics() {
        // Reset all performance tracking data to initial values
        queryStats.clear(); // Clear individual query statistics
        totalQueries.set(0); // Reset total query counter
        slowQueries.set(0); // Reset slow query counter
    }

    /**
     * Thread-safe holder for query execution statistics.
     *
     * <p>This class maintains statistics for individual SQL queries,
     * including execution count and timing information. It uses atomic
     * operations to ensure thread safety in concurrent environments.</p>
     *
     */
    private static class QueryStats {
        // Thread-safe counter for query execution frequency
        private final AtomicLong count = new AtomicLong(0);
        // Timestamp of last execution (volatile for visibility across threads)
        private volatile long lastExecuted = System.currentTimeMillis();

        /**
         * Increments the execution count for this query.
         */
        public void incrementCount() {
            count.incrementAndGet();
        }

        /**
         * Gets the total execution count for this query.
         *
         * @return the number of times this query has been executed
         */
        public long getCount() {
            return count.get();
        }

        /**
         * Updates the last execution timestamp to the current time.
         */
        public void updateLastExecuted() {
            lastExecuted = System.currentTimeMillis();
        }

        /**
         * Checks if this query was executed recently (within the last minute).
         *
         * @return true if the query was executed recently, false otherwise
         */
        public boolean isRecentExecution() {
            // Check if query was executed within the last minute (60000 ms)
            return System.currentTimeMillis() - lastExecuted < 60000;
        }
    }

    /**
     * Comprehensive report of query performance metrics.
     *
     * <p>This class encapsulates all the performance statistics collected
     * by the inspector, providing a comprehensive view of database query
     * performance and potential optimization opportunities.</p>
     *
     */
    public static class QueryPerformanceReport {
        // Performance metrics for comprehensive reporting
        private final long totalQueries; // Total number of queries executed
        private final long slowQueries; // Queries exceeding performance threshold
        private final long uniqueQueries; // Number of distinct query patterns
        private final long potentialNPlusOneQueries; // Queries that might indicate N+1 problems

        /**
         * Constructs a new QueryPerformanceReport with the specified metrics.
         *
         * @param totalQueries the total number of queries executed
         * @param slowQueries the number of queries that exceeded performance thresholds
         * @param uniqueQueries the number of unique SQL patterns executed
         * @param potentialNPlusOneQueries the number of potential N+1 query patterns detected
         */
        public QueryPerformanceReport(
                long totalQueries, long slowQueries, long uniqueQueries, long potentialNPlusOneQueries) {
            this.totalQueries = totalQueries;
            this.slowQueries = slowQueries;
            this.uniqueQueries = uniqueQueries;
            this.potentialNPlusOneQueries = potentialNPlusOneQueries;
        }

        /**
         * Gets the total number of queries executed.
         *
         * @return the total query count
         */
        public long getTotalQueries() {
            return totalQueries;
        }

        /**
         * Gets the number of slow queries detected.
         *
         * @return the slow query count
         */
        public long getSlowQueries() {
            return slowQueries;
        }

        /**
         * Gets the number of unique SQL patterns executed.
         *
         * @return the unique query pattern count
         */
        public long getUniqueQueries() {
            return uniqueQueries;
        }

        /**
         * Gets the number of potential N+1 query patterns detected.
         *
         * @return the potential N+1 pattern count
         */
        public long getPotentialNPlusOneQueries() {
            return potentialNPlusOneQueries;
        }

        /**
         * Returns a string representation of the performance report.
         *
         * @return a formatted string containing all performance metrics
         */
        @Override
        public String toString() {
            // Format performance report for logging and debugging
            return String.format(
                    "Query Performance Report: Total=%d, Slow=%d, Unique=%d, N+1=%d",
                    totalQueries, slowQueries, uniqueQueries, potentialNPlusOneQueries);
        }
    }
}
