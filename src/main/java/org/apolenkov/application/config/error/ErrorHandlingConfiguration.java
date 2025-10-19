package org.apolenkov.application.config.error;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.VaadinSession;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Centralized error handling for Vaadin applications.
 * Provides secure error navigation with cycle protection and profile-based error details.
 */
@Component
public class ErrorHandlingConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandlingConfiguration.class);
    private final Environment environment;

    // Logic details
    private static final String ATTR_ERROR_NAV_GUARD = "errorNavigationInProgress";

    /**
     * Creates error handling configuration with environment support.
     *
     * @param env the Spring environment for profile detection
     */
    public ErrorHandlingConfiguration(final Environment env) {
        this.environment = env;
    }

    /**
     * Installs error handler for UI instance with secure navigation.
     *
     * @param ui the UI instance to configure error handling for
     */
    public void installErrorHandler(final UI ui) {
        if (ui == null) {
            throw new IllegalArgumentException("UI cannot be null");
        }
        ui.getSession().setErrorHandler(errorEvent -> handleUiError(ui, errorEvent));
        LOGGER.debug("Error handler installed [uiId={}]", ui.getUIId());
    }

    /**
     * Handles UI errors with cycle protection and secure navigation.
     * Filters out normal client disconnection exceptions (ClientAbortException, BrokenPipe).
     *
     * @param ui the UI instance where error occurred
     * @param errorEvent the error event containing error details
     */
    private void handleUiError(final UI ui, final ErrorEvent errorEvent) {
        VaadinSession session = ui.getSession();
        String currentRoute = getCurrentRoute(ui);
        Throwable error = errorEvent.getThrowable();

        // Filter out normal client disconnection events - not actual errors
        if (isClientAbortException(error)) {
            LOGGER.debug("Client disconnected [uiId={}, route={}]: {}", ui.getUIId(), currentRoute, error.getMessage());
            return;
        }

        LOGGER.error("UI error [uiId={}, route={}]", ui.getUIId(), currentRoute, error);

        // Skip if session is invalid (shutdown scenario)
        if (session == null || !isSessionValid(session)) {
            LOGGER.debug("Session invalid or null, skipping error navigation [uiId={}]", ui.getUIId());
            return;
        }

        if (shouldSkipNavigation(session, currentRoute)) {
            return;
        }

        navigateToErrorPage(ui, session, currentRoute, error);
    }

    /**
     * Checks if the exception is a normal client disconnect (not an actual error).
     * Filters ClientAbortException, BrokenPipe, and ConnectionReset exceptions.
     *
     * @param throwable the exception to check
     * @return true if this is a client disconnection event, false if it's a real error
     */
    private boolean isClientAbortException(final Throwable throwable) {
        if (throwable == null) {
            return false;
        }

        // Check exception class name (handles multiple classloader scenarios)
        String className = throwable.getClass().getName();
        if (className.contains("ClientAbortException") || className.contains("BrokenPipeException")) {
            return true;
        }

        // Check exception message for common disconnect patterns
        String message = throwable.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase(Locale.ROOT);
            if (lowerMessage.contains("broken pipe")
                    || lowerMessage.contains("connection reset")
                    || lowerMessage.contains("connection was aborted")) {
                return true;
            }
        }

        // Check cause recursively (exceptions are often wrapped)
        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            return isClientAbortException(cause);
        }

        return false;
    }

    /**
     * Safely retrieves current route from UI.
     *
     * @param ui the UI instance to get route from
     * @return the current route path or null if unavailable
     */
    private String getCurrentRoute(final UI ui) {
        try {
            return ui.getInternals().getActiveViewLocation().getPath();
        } catch (Exception e) {
            LOGGER.debug("Failed to get current route [uiId={}]", ui.getUIId(), e);
            return null;
        }
    }

    /**
     * Checks if session is valid and can be safely accessed.
     *
     * @param session the Vaadin session to check
     * @return true if session is valid, false otherwise
     */
    private boolean isSessionValid(final VaadinSession session) {
        try {
            // Try to access session - will fail if invalidated
            session.hasLock();
            return true;
        } catch (Exception e) {
            LOGGER.trace("Session validation failed", e);
            return false;
        }
    }

    /**
     * Determines if error navigation should be skipped to prevent cycles.
     *
     * @param session the Vaadin session to check navigation state
     * @param currentRoute the current route path
     * @return true if navigation should be skipped, false otherwise
     */
    private boolean shouldSkipNavigation(final VaadinSession session, final String currentRoute) {
        try {
            boolean navigationInProgress = Boolean.TRUE.equals(session.getAttribute(ATTR_ERROR_NAV_GUARD));
            boolean onErrorView = RouteConstants.ERROR_ROUTE.equals(currentRoute);

            if (navigationInProgress) {
                LOGGER.warn("Skipping error navigation - already in progress");
                return true;
            }

            if (onErrorView) {
                LOGGER.warn("Error on error page - avoiding cycle");
                return true;
            }

            return false;
        } catch (IllegalStateException e) {
            // Session invalidated during check
            LOGGER.debug("Session invalidated during navigation check", e);
            return true;
        }
    }

    /**
     * Navigates to error page with secure parameters and navigation guard.
     *
     * @param ui the UI instance to navigate
     * @param session the Vaadin session for navigation guard
     * @param currentRoute the current route path
     * @param error the error that occurred
     */
    private void navigateToErrorPage(
            final UI ui, final VaadinSession session, final String currentRoute, final Throwable error) {
        try {
            session.setAttribute(ATTR_ERROR_NAV_GUARD, Boolean.TRUE);
        } catch (IllegalStateException e) {
            LOGGER.debug("Cannot set navigation guard - session invalidated [uiId={}]", ui.getUIId());
            return;
        }

        String errorId = UUID.randomUUID().toString();

        try {
            ui.access(() -> {
                try {
                    QueryParameters params = createErrorParameters(currentRoute, error, errorId);
                    NavigationHelper.navigateToError(RouteConstants.ERROR_ROUTE, params);
                    LOGGER.debug("Navigated to error page [uiId={}, errorId={}]", ui.getUIId(), errorId);
                } finally {
                    try {
                        session.setAttribute(ATTR_ERROR_NAV_GUARD, Boolean.FALSE);
                    } catch (IllegalStateException e) {
                        LOGGER.trace("Cannot clear navigation guard - session invalidated", e);
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.warn("Failed to navigate to error page [uiId={}]", ui.getUIId(), e);
            try {
                session.setAttribute(ATTR_ERROR_NAV_GUARD, Boolean.FALSE);
            } catch (IllegalStateException ex) {
                LOGGER.trace("Cannot clear navigation guard - session invalidated", ex);
            }
        }
    }

    /**
     * Creates secure error parameters based on active profile.
     *
     * @param currentRoute the current route path
     * @param error the error that occurred
     * @param errorId the unique error identifier
     * @return QueryParameters for error page navigation
     */
    private QueryParameters createErrorParameters(
            final String currentRoute, final Throwable error, final String errorId) {
        if (currentRoute == null || currentRoute.isEmpty()) {
            return QueryParameters.empty();
        }

        if (isDevProfile()) {
            return QueryParameters.simple(Map.of(
                    "from",
                    currentRoute,
                    "error",
                    error.getClass().getSimpleName(),
                    "message",
                    error.getMessage() != null ? error.getMessage() : "Unknown error",
                    "id",
                    errorId));
        } else {
            return QueryParameters.simple(Map.of(
                    "from", currentRoute,
                    "error", "500",
                    "id", errorId));
        }
    }

    /**
     * Checks if application is running in development profile.
     *
     * @return true if in development profile, false otherwise
     */
    private boolean isDevProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        return Arrays.asList(activeProfiles).contains("dev");
    }
}
