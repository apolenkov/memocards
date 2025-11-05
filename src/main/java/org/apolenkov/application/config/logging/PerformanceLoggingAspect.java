package org.apolenkov.application.config.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AOP aspect for logging performance metrics of repository operations.
 * Automatically measures and logs execution time for all adapter methods.
 *
 * <p>Logs warnings for slow queries exceeding configured threshold.
 * Helps identify performance bottlenecks and optimization opportunities.
 */
@Aspect
@Component
public class PerformanceLoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceLoggingAspect.class);

    @Value("${app.monitoring.slow-query-threshold-ms:100}")
    private long slowQueryThresholdMs;

    /**
     * Intercepts all repository adapter method calls to measure execution time.
     * Logs performance metrics and warns about slow queries.
     *
     * @param joinPoint the intercepted method call
     * @return the result of the intercepted method
     * @throws Throwable if the intercepted method throws an exception
     */
    @Around("execution(* org.apolenkov.application.infrastructure.repository.jdbc.adapter.*.*(..))")
    public Object logRepositoryPerformance(final ProceedingJoinPoint joinPoint) throws Throwable {
        final String className = joinPoint.getTarget().getClass().getSimpleName();
        final String methodName = joinPoint.getSignature().getName();
        final String operation = className + "." + methodName;

        final long startTime = System.currentTimeMillis();

        try {
            final Object result = joinPoint.proceed();
            final long duration = System.currentTimeMillis() - startTime;

            logPerformance(operation, duration, null);

            return result;
        } catch (Throwable t) {
            // Catch all exceptions and errors for comprehensive logging
            // All exceptions are rethrown to maintain application state
            final long duration = System.currentTimeMillis() - startTime;
            logPerformance(operation, duration, t);
            throw t;
        }
    }

    /**
     * Logs performance metrics for a repository operation.
     * Warns about slow queries exceeding configured threshold.
     *
     * @param operation the operation name (Class.method)
     * @param durationMs execution time in milliseconds
     * @param exception exception thrown during execution (nullable)
     */
    private void logPerformance(final String operation, final long durationMs, final Throwable exception) {
        if (exception != null) {
            LOGGER.error("Repository operation FAILED: {} took {}ms", operation, durationMs, exception);
        } else if (durationMs > slowQueryThresholdMs) {
            LOGGER.warn(
                    "SLOW QUERY detected: {} took {}ms (threshold: {}ms)", operation, durationMs, slowQueryThresholdMs);
        } else {
            LOGGER.debug("Repository operation: {} completed in {}ms", operation, durationMs);
        }
    }
}
