package org.apolenkov.application.config.monitoring;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled logger for HikariCP connection pool metrics.
 * Monitors pool health and logs statistics at regular intervals.
 *
 * <p>Helps identify connection pool exhaustion, leaks, and optimization opportunities.
 * Can be disabled via application properties.
 */
@Component
@ConditionalOnProperty(name = "app.monitoring.hikari.enabled", havingValue = "true", matchIfMissing = true)
public class HikariMetricsLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(HikariMetricsLogger.class);

    private final DataSource dataSource;

    /**
     * Creates HikariMetricsLogger with datasource.
     *
     * @param ds the HikariCP datasource to monitor
     */
    public HikariMetricsLogger(final DataSource ds) {
        this.dataSource = ds;
    }

    /**
     * Logs HikariCP pool metrics at configured intervals.
     * Runs every minute by default (configurable via app.monitoring.hikari.log-interval-ms).
     */
    @Scheduled(fixedDelayString = "${app.monitoring.hikari.log-interval-ms:60000}")
    public void logPoolMetrics() {
        if (!(dataSource instanceof HikariDataSource hikariDataSource)) {
            LOGGER.debug("DataSource is not HikariDataSource, skipping metrics");
            return;
        }

        try {
            HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();

            if (poolMXBean == null) {
                LOGGER.warn("HikariPoolMXBean is null, cannot retrieve metrics");
                return;
            }

            int active = poolMXBean.getActiveConnections();
            int idle = poolMXBean.getIdleConnections();
            int total = poolMXBean.getTotalConnections();
            int waiting = poolMXBean.getThreadsAwaitingConnection();

            // Log normal metrics at DEBUG level
            LOGGER.debug("HikariCP metrics: active={}, idle={}, total={}, waiting={}", active, idle, total, waiting);

            // Warn if pool is under pressure
            if (waiting > 0) {
                LOGGER.warn("Connection pool under pressure: {} threads waiting for connections", waiting);
            }

            // Warn if pool is near exhaustion (>90% utilized)
            int maxPoolSize = hikariDataSource.getHikariConfigMXBean().getMaximumPoolSize();
            double utilization = (double) active / maxPoolSize * 100;
            if (utilization > 90 && LOGGER.isWarnEnabled()) {
                // Format inside conditional to avoid String.format() evaluation overhead
                LOGGER.warn(
                        "Connection pool high utilization: {}% ({}/{})",
                        String.format("%.1f", utilization), active, maxPoolSize);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to retrieve HikariCP metrics", e);
        }
    }
}
