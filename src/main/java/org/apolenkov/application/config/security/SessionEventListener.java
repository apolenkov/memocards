package org.apolenkov.application.config.security;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

/**
 * Listener for HTTP session lifecycle events.
 * Logs session creation, destruction, and timeout events for security audit trail.
 *
 * <p>Provides visibility into:
 * <ul>
 *   <li>Session creation and destruction</li>
 *   <li>Automatic session timeout (security-relevant)</li>
 *   <li>Explicit logout vs timeout detection</li>
 *   <li>User session duration tracking</li>
 * </ul>
 *
 * <p>Part of comprehensive security monitoring (OWASP compliance).
 */
@Component
public class SessionEventListener implements HttpSessionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionEventListener.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

    private static final String SESSION_CREATED_TIME_ATTR = "sessionCreatedTime";

    /**
     * Handles the HTTP session creation event.
     * Stores the session creation time as an attribute for duration tracking purposes.
     * Logs the session creation event with session ID and maximum inactive interval at DEBUG level.
     * Logs the session creation event with session ID and maximum inactive interval at INFO level for audit purposes.
     *
     * @param se the HttpSessionEvent containing the created session
     */
    @Override
    public void sessionCreated(final HttpSessionEvent se) {
        HttpSession session = se.getSession();

        // Store creation time for duration tracking
        session.setAttribute(SESSION_CREATED_TIME_ATTR, Instant.now());

        LOGGER.debug(
                "HTTP session created: id={}, maxInactiveInterval={}s",
                session.getId(),
                session.getMaxInactiveInterval());

        AUDIT_LOGGER.info(
                "Session created: sessionId={}, maxInactiveInterval={}s",
                session.getId(),
                session.getMaxInactiveInterval());
    }

    /**
     * Handles the destruction of an HTTP session, logging session details and distinguishing
     * between timeout and explicit logout scenarios.
     *
     * <p>When a session is destroyed, this method:
     * <ul>
     *   <li>Extracts the session ID and authenticated user (if available)</li>
     *   <li>Calculates the total duration of the session</li>
     *   <li>Determines whether the session ended due to a timeout or explicit logout</li>
     *   <li>Logs the event to both audit and application logs with appropriate severity</li>
     * </ul>
     *
     * @param se the {@link HttpSessionEvent} containing the session that was destroyed
     */
    @Override
    public void sessionDestroyed(final HttpSessionEvent se) {
        HttpSession session = se.getSession();
        String sessionId = session.getId();

        // Try to get authenticated user from session
        String username = extractUsername(session);

        // Calculate session duration
        String duration = calculateSessionDuration(session);

        // Determine if this was a timeout or explicit logout
        boolean wasTimeout = isSessionTimeout(session);

        if (wasTimeout) {
            AUDIT_LOGGER.warn(
                    "Session timeout: sessionId={}, user={}, duration={}",
                    sessionId,
                    username != null ? username : "anonymous",
                    duration);
            LOGGER.info("Session timed out: id={}, user={}, duration={}", sessionId, username, duration);
        } else {
            AUDIT_LOGGER.info(
                    "Session destroyed (logout): sessionId={}, user={}, duration={}",
                    sessionId,
                    username != null ? username : "anonymous",
                    duration);
            LOGGER.debug("Session destroyed: id={}, user={}, duration={}", sessionId, username, duration);
        }
    }

    /**
     * Extracts authenticated username from session.
     *
     * @param session HTTP session
     * @return username or null if not authenticated
     */
    private String extractUsername(final HttpSession session) {
        try {
            Object securityContextObj =
                    session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

            if (securityContextObj instanceof SecurityContext securityContext) {
                Authentication auth = securityContext.getAuthentication();
                if (auth != null && auth.isAuthenticated()) {
                    return auth.getName();
                }
            }
        } catch (Exception e) {
            LOGGER.trace("Could not extract username from session", e);
        }
        return null;
    }

    /**
     * Calculates session duration from creation to destruction.
     *
     * @param session HTTP session
     * @return formatted duration string (e.g., "5m 30s")
     */
    private String calculateSessionDuration(final HttpSession session) {
        try {
            Object createdTimeObj = session.getAttribute(SESSION_CREATED_TIME_ATTR);
            if (createdTimeObj instanceof Instant createdTime) {
                Duration duration = Duration.between(createdTime, Instant.now());
                return formatDuration(duration);
            }
        } catch (Exception e) {
            LOGGER.trace("Could not calculate session duration", e);
        }
        return "unknown";
    }

    /**
     * Formats duration in human-readable format.
     *
     * @param duration duration to format
     * @return formatted string (e.g., "5m 30s", "2h 15m")
     */
    private String formatDuration(final Duration duration) {
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return String.format("%dm %ds", minutes, remainingSeconds);
        } else {
            long hours = seconds / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            return String.format("%dh %dm", hours, remainingMinutes);
        }
    }

    /**
     * Determines if session destruction was due to timeout.
     * Uses heuristic: if session is being destroyed very close to maxInactiveInterval,
     * it's likely a timeout rather than explicit logout.
     *
     * @param session HTTP session
     * @return true if session timed out, false if explicitly logged out
     */
    private boolean isSessionTimeout(final HttpSession session) {
        try {
            Object createdTimeObj = session.getAttribute(SESSION_CREATED_TIME_ATTR);
            if (createdTimeObj instanceof Instant createdTime) {
                Duration actualDuration = Duration.between(createdTime, Instant.now());
                long maxInactiveSeconds = session.getMaxInactiveInterval();

                // If session lived close to max inactive time (within 5 seconds), consider it timeout
                long difference = Math.abs(actualDuration.getSeconds() - maxInactiveSeconds);
                return difference < 5;
            }
        } catch (Exception e) {
            LOGGER.trace("Could not determine timeout status", e);
        }

        // Default to false (assume explicit logout)
        return false;
    }
}
