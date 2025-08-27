package org.apolenkov.application;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.servlet.http.Cookie;
import java.util.Locale;
import org.apolenkov.application.config.LocaleConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Main entry point for the Flashcards Spring Boot application.
 */
@SpringBootApplication
public class Application implements VaadinServiceInitListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final String ERROR_ROUTE = "error";
    private static final String ATTR_ERROR_NAV_GUARD = "errorNavigationInProgress";

    /**
     * Starts the Spring Boot application.
     */
    public static void main(final String[] args) {
        LOGGER.info("Starting Flashcards application...");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Application arguments: {}", java.util.Arrays.toString(args));
        }

        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        // Register shutdown hook for graceful application termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown hook triggered, cleaning up...");
            if (context.isActive()) {
                context.close();
            }
            LOGGER.info("Shutdown hook completed");
        }));

        LOGGER.info("Flashcards application started successfully");
    }

    /**
     * Initializes Vaadin service with UI configuration.
     *
     * @param event the service initialization event (non-null)
     * @throws IllegalArgumentException if event is null
     */
    @Override
    public void serviceInit(final ServiceInitEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("ServiceInitEvent cannot be null");
        }
        LOGGER.debug("Initializing Vaadin service...");
        event.getSource().addUIInitListener(uiEvent -> configureUi(uiEvent.getUI()));
    }

    /**
     * Configures UI with theme, error handling and locale settings.
     *
     * @param ui the UI instance to configure (non-null)
     * @throws IllegalArgumentException if ui is null
     */
    private void configureUi(final UI ui) {
        if (ui == null) {
            throw new IllegalArgumentException("UI cannot be null");
        }
        LOGGER.debug("UI initialized, applying configuration [uiId={}]", ui.getUIId());
        enableLumoDark(ui);
        installSafeErrorHandler(ui);
        applyPreferredLocale(ui);
        LOGGER.debug("UI setup completed [uiId={}]", ui.getUIId());
    }

    /**
     * Applies Lumo dark theme to the UI.
     *
     * @param ui the UI instance to apply the theme to (non-null)
     * @throws IllegalArgumentException if ui is null
     */
    private void enableLumoDark(final UI ui) {
        if (ui == null) {
            throw new IllegalArgumentException("UI cannot be null");
        }
        ui.getElement().getThemeList().add(Lumo.DARK);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Lumo dark theme enabled [uiId={}]", ui.getUIId());
        }
    }

    /**
     * Sets up error handler for safe navigation to error route.
     *
     * @param ui the UI instance to configure error handling for (non-null)
     * @throws IllegalArgumentException if ui is null
     */
    private void installSafeErrorHandler(final UI ui) {
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
        try {
            ui.access(() -> {
                try {
                    if (fromRoute != null && !fromRoute.isEmpty()) {
                        LOGGER.debug("Navigating to '{}' from '{}' [uiId={}]", ERROR_ROUTE, fromRoute, ui.getUIId());
                        ui.navigate(ERROR_ROUTE, QueryParameters.of("from", fromRoute));
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

    /**
     * Applies user's preferred locale from cookie or session.
     */
    private void applyPreferredLocale(final UI ui) {
        VaadinSession session = ui.getSession();

        // First priority: locale from cookie (user's persistent preference)
        Locale cookieLocale = readLocaleFromCookie();
        if (cookieLocale != null) {
            session.setAttribute(LocaleConstants.SESSION_LOCALE_KEY, cookieLocale);
            ui.setLocale(cookieLocale);
            LOGGER.debug("Locale set from cookie: {} [uiId={}]", cookieLocale, ui.getUIId());
            return;
        }

        // Second priority: locale from session (temporary preference)
        Object preferred = session.getAttribute(LocaleConstants.SESSION_LOCALE_KEY);
        if (preferred instanceof Locale locale) {
            ui.setLocale(locale);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Locale set from session attribute: {} [uiId={}]", locale, ui.getUIId());
            }
        } else {
            // Fallback: default to English if no preference is set
            ui.setLocale(Locale.ENGLISH);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Locale fallback applied: ENGLISH [uiId={}]", ui.getUIId());
            }
        }
    }

    /**
     * Reads locale preference from cookie.
     *
     * @return user's preferred locale or null if not set
     */
    private Locale readLocaleFromCookie() {
        try {
            // Get current HTTP request to access cookies
            VaadinServletRequest req = (VaadinServletRequest) VaadinService.getCurrentRequest();
            if (req != null && req.getCookies() != null) {
                // Search for locale preference cookie
                for (Cookie c : req.getCookies()) {
                    if (LocaleConstants.COOKIE_LOCALE_KEY.equals(c.getName())) {
                        // Parse locale from cookie value and validate
                        Locale locale = Locale.forLanguageTag(c.getValue());
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Locale cookie found: {}", locale);
                        }
                        return locale;
                    }
                }
            }
        } catch (Exception e) {
            // Log but don't fail if cookie reading fails
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Failed to read locale from cookie", e);
            }
        }
        return null;
    }
}
