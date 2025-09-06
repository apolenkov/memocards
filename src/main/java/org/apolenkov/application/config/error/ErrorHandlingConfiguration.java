package org.apolenkov.application.config.error;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.VaadinSession;
import java.util.Map;
import org.apolenkov.application.config.constants.RouteConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Configures error handling for Vaadin UI instances.
 *
 * <p>This component is responsible for setting up error handlers
 * and managing error navigation with cycle protection.
 */
@Component
public class ErrorHandlingConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandlingConfiguration.class);
    private static final String ERROR_ROUTE = RouteConstants.ERROR_ROUTE;
    private static final String ATTR_ERROR_NAV_GUARD = "errorNavigationInProgress";

    /**
     * Sets up error handler for safe navigation to error route.
     *
     * @param ui the UI instance to configure error handling for (non-null)
     * @throws IllegalArgumentException if ui is null
     */
    public void installErrorHandler(final UI ui) {
        if (ui == null) {
            throw new IllegalArgumentException("UI cannot be null");
        }
        ui.getSession().setErrorHandler(errorEvent -> handleUiError(ui, errorEvent));
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("UI error handler installed [uiId={}]", ui.getUIId());
        }
    }

    /**
     * Processes UI errors with safe navigation and cycle protection.
     *
     * @param ui the UI instance where the error occurred
     * @param errorEvent the error event containing error details
     */
    private void handleUiError(final UI ui, final ErrorEvent errorEvent) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("UI error handled [uiId={}]", ui.getUIId(), errorEvent.getThrowable());
        }

        VaadinSession session = ui.getSession();
        String currentRoute = null;
        try {
            currentRoute = ui.getInternals().getActiveViewLocation().getPath();
        } catch (Exception e) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Failed to read current route for error handling [uiId={}]", ui.getUIId(), e);
            }
        }

        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(
                    "Unhandled UI error [uiId={}, route={}]", ui.getUIId(), currentRoute, errorEvent.getThrowable());
        }

        // Check for navigation guards to prevent infinite error loops
        boolean navigationInProgress = Boolean.TRUE.equals(session.getAttribute(ATTR_ERROR_NAV_GUARD));
        boolean onErrorView = ERROR_ROUTE.equals(currentRoute);

        if (navigationInProgress) {
            LOGGER.warn(
                    "Skipping error navigation because previous navigation is still in progress [uiId={}, route={}]",
                    ui.getUIId(),
                    currentRoute);
            return;
        }

        if (onErrorView) {
            LOGGER.warn(
                    "Error occurred on '{}' route; avoiding cyclic navigation [uiId={}]", ERROR_ROUTE, ui.getUIId());
            return;
        }

        // Set navigation guard to prevent concurrent error navigation
        session.setAttribute(ATTR_ERROR_NAV_GUARD, Boolean.TRUE);
        final String fromRoute = currentRoute;
        final Throwable error = errorEvent.getThrowable();
        try {
            ui.access(() -> {
                try {
                    if (fromRoute != null && !fromRoute.isEmpty()) {
                        LOGGER.debug("Navigating to '{}' from '{}' [uiId={}]", ERROR_ROUTE, fromRoute, ui.getUIId());
                        ui.navigate(
                                ERROR_ROUTE,
                                QueryParameters.simple(Map.of(
                                        "from",
                                        fromRoute,
                                        "error",
                                        error.getClass().getSimpleName(),
                                        "message",
                                        error.getMessage() != null ? error.getMessage() : "Unknown error")));
                    } else {
                        LOGGER.debug("Navigating to '{}' [uiId={}]", ERROR_ROUTE, ui.getUIId());
                        ui.navigate(ERROR_ROUTE);
                    }
                } catch (Exception navEx) {
                    LOGGER.warn("Failed during error navigation [uiId={}]", ui.getUIId(), navEx);
                } finally {
                    // Always clear navigation guard, even if navigation fails
                    session.setAttribute(ATTR_ERROR_NAV_GUARD, Boolean.FALSE);
                }
            });
        } catch (Exception e) {
            LOGGER.warn("Failed to schedule error navigation [uiId={}]", ui.getUIId(), e);
            // Clear navigation guard on scheduling failure
            session.setAttribute(ATTR_ERROR_NAV_GUARD, Boolean.FALSE);
        }
    }
}
