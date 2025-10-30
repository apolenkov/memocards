package org.apolenkov.application.config.security;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * AOP aspect for advanced security audit and suspicious activity detection.
 * Monitors authentication failures, detects brute-force attempts, and tracks security events.
 *
 * <p>Features:
 * <ul>
 *   <li>Multiple failed login attempts detection (rate limiting)</li>
 *   <li>IP address tracking for security audit</li>
 *   <li>Brute-force attack detection</li>
 *   <li>Suspicious activity alerting</li>
 * </ul>
 *
 * <p>Part of comprehensive security monitoring (OWASP A07:2021 compliance).
 */
@Aspect
@Component
public class SecurityAuditAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAuditAspect.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");
    private static final Logger SECURITY_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.security");

    private static final String UNKNOWN_VALUE = "unknown";

    /**
     * Tracks failed login attempts per IP address.
     * Key: IP address, Value: FailedAttemptTracker
     */
    private final Map<String, FailedAttemptTracker> failedLoginAttempts = new ConcurrentHashMap<>();

    @Value("${app.security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.failed-attempts-window-minutes:15}")
    private int failedAttemptsWindowMinutes;

    /**
     * Intercepts authentication failures to detect brute-force attacks.
     * Logs IP address, tracks failure count, and alerts on suspicious activity.
     *
     * @param joinPoint the intercepted authentication method
     * @param exception the exception thrown during authentication
     */
    @AfterThrowing(
            pointcut = "execution(* org.apolenkov.application.service.auth.AuthService.authenticateAndPersist(..))",
            throwing = "exception")
    public void logAuthenticationFailure(final JoinPoint joinPoint, final Exception exception) {
        Object[] args = joinPoint.getArgs();
        if (args.length < 1) {
            return;
        }

        String username = String.valueOf(args[0]);
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();

        // Track failed attempt
        FailedAttemptTracker tracker = failedLoginAttempts.computeIfAbsent(ipAddress, k -> new FailedAttemptTracker());
        int attemptCount = tracker.incrementAndGet();

        // Log with IP address for security audit
        AUDIT_LOGGER.warn(
                "Failed login attempt: username={}, ip={}, attemptNumber={}, reason={}",
                username,
                ipAddress,
                attemptCount,
                exception.getClass().getSimpleName());

        LOGGER.warn(
                "Authentication failure: username={}, ip={}, attempts={}, userAgent={}",
                username,
                ipAddress,
                attemptCount,
                userAgent);

        // Alert on suspicious activity
        if (attemptCount >= maxFailedAttempts) {
            alertSuspiciousActivity(username, ipAddress, attemptCount, userAgent);
        }
    }

    /**
     * Alerts security team about suspicious activity.
     *
     * @param username attempted username
     * @param ipAddress source IP address
     * @param attemptCount number of failed attempts
     * @param userAgent user agent string
     */
    private void alertSuspiciousActivity(
            final String username, final String ipAddress, final int attemptCount, final String userAgent) {

        SECURITY_LOGGER.error(
                "SECURITY ALERT: Potential brute-force attack detected! "
                        + "username={}, ip={}, failedAttempts={}, userAgent={}",
                username,
                ipAddress,
                attemptCount,
                userAgent);

        AUDIT_LOGGER.error(
                "SECURITY ALERT: Brute-force detected: username={}, ip={}, attempts={}",
                username,
                ipAddress,
                attemptCount);

        // Future enhancements for consideration:
        // - Email/Slack notifications to security team
        // - Temporary IP blocking (requires rate limiter integration)
        // - SIEM alert triggering
    }

    /**
     * Extracts client IP address from current HTTP request.
     *
     * @return client IP address or "unknown" if not available
     */
    private String getClientIpAddress() {
        try {
            VaadinServletRequest vaadinRequest = (VaadinServletRequest) VaadinService.getCurrentRequest();
            if (vaadinRequest != null) {
                HttpServletRequest request = vaadinRequest.getHttpServletRequest();

                // Check X-Forwarded-For header (for proxied requests)
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    // X-Forwarded-For can contain multiple IPs, take first one
                    return xForwardedFor.split(",")[0].trim();
                }

                // Fallback to remote address
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            LOGGER.trace("Could not extract client IP address", e);
        }
        return UNKNOWN_VALUE;
    }

    /**
     * Extracts User-Agent header from current HTTP request.
     *
     * @return user agent string or "unknown" if not available
     */
    private String getUserAgent() {
        try {
            VaadinServletRequest vaadinRequest = (VaadinServletRequest) VaadinService.getCurrentRequest();
            if (vaadinRequest != null) {
                HttpServletRequest request = vaadinRequest.getHttpServletRequest();
                String userAgent = request.getHeader("User-Agent");
                return userAgent != null ? userAgent : UNKNOWN_VALUE;
            }
        } catch (Exception e) {
            LOGGER.trace("Could not extract user agent", e);
        }
        return UNKNOWN_VALUE;
    }

    /**
     * Periodically cleans up old failed attempt trackers.
     * Runs every hour to prevent memory leaks from accumulating failed attempts.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupOldAttempts() {
        Instant cutoff = Instant.now().minusSeconds(failedAttemptsWindowMinutes * 60L);

        int removedCount = 0;
        for (Map.Entry<String, FailedAttemptTracker> entry : failedLoginAttempts.entrySet()) {
            if (entry.getValue().isOlderThan(cutoff)) {
                failedLoginAttempts.remove(entry.getKey());
                removedCount++;
            }
        }

        if (removedCount > 0) {
            LOGGER.debug("Cleaned up {} old failed login attempt trackers", removedCount);
        }
    }

    /**
     * Tracks failed authentication attempts for a single IP address.
     */
    private static final class FailedAttemptTracker {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile Instant lastAttempt = Instant.now();

        /**
         * Increments failed attempt count and updates last attempt time.
         *
         * @return new attempt count
         */
        int incrementAndGet() {
            lastAttempt = Instant.now();
            return count.incrementAndGet();
        }

        /**
         * Checks if tracker is older than cutoff time.
         *
         * @param cutoff cutoff time
         * @return true if last attempt was before cutoff
         */
        boolean isOlderThan(final Instant cutoff) {
            return lastAttempt.isBefore(cutoff);
        }
    }
}
