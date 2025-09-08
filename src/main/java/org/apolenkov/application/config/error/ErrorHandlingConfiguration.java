package org.apolenkov.application.config.error;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.VaadinSession;
import java.util.Map;
import java.util.UUID;
import org.apolenkov.application.config.constants.RouteConstants;
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
    private static final String ERROR_ROUTE = RouteConstants.ERROR_ROUTE;
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
     *
     * @param ui the UI instance where error occurred
     * @param errorEvent the error event containing error details
     */
    private void handleUiError(final UI ui, final ErrorEvent errorEvent) {
        VaadinSession session = ui.getSession();
        String currentRoute = getCurrentRoute(ui);
        Throwable error = errorEvent.getThrowable();

        LOGGER.error("UI error [uiId={}, route={}]", ui.getUIId(), currentRoute, error);

        if (shouldSkipNavigation(session, currentRoute)) {
            return;
        }

        navigateToErrorPage(ui, session, currentRoute, error);
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
     * Determines if error navigation should be skipped to prevent cycles.
     *
     * @param session the Vaadin session to check navigation state
     * @param currentRoute the current route path
     * @return true if navigation should be skipped, false otherwise
     */
    private boolean shouldSkipNavigation(final VaadinSession session, final String currentRoute) {
        boolean navigationInProgress = Boolean.TRUE.equals(session.getAttribute(ATTR_ERROR_NAV_GUARD));
        boolean onErrorView = ERROR_ROUTE.equals(currentRoute);

        if (navigationInProgress) {
            LOGGER.warn("Skipping error navigation - already in progress");
            return true;
        }

        if (onErrorView) {
            LOGGER.warn("Error on error page - avoiding cycle");
            return true;
        }

        return false;
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
        session.setAttribute(ATTR_ERROR_NAV_GUARD, Boolean.TRUE);
        String errorId = UUID.randomUUID().toString();

        try {
            ui.access(() -> {
                try {
                    QueryParameters params = createErrorParameters(currentRoute, error, errorId);
                    ui.navigate(ERROR_ROUTE, params);
                    LOGGER.debug("Navigated to error page [uiId={}, errorId={}]", ui.getUIId(), errorId);
                } finally {
                    session.setAttribute(ATTR_ERROR_NAV_GUARD, Boolean.FALSE);
                }
            });
        } catch (Exception e) {
            LOGGER.warn("Failed to navigate to error page [uiId={}]", ui.getUIId(), e);
            session.setAttribute(ATTR_ERROR_NAV_GUARD, Boolean.FALSE);
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
        return java.util.Arrays.asList(activeProfiles).contains("dev");
    }
}
