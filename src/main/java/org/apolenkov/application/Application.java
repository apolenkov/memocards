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
 *
 * <p>Provides Vaadin service initialization including UI theming, error handling, and locale management.</p>
 */
@SpringBootApplication
public class Application implements VaadinServiceInitListener {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final String ERROR_ROUTE = "error";
    private static final String ATTR_ERROR_NAV_GUARD = "errorNavigationInProgress";

    /**
     * Starts the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        logger.info("Starting Flashcards application...");
        if (logger.isDebugEnabled()) {
            logger.debug("Application arguments: {}", java.util.Arrays.toString(args));
        }

        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        // Register shutdown hook for graceful application termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered, cleaning up...");
            if (context.isActive()) {
                context.close();
            }
            logger.info("Shutdown hook completed");
        }));

        logger.info("Flashcards application started successfully");
    }

    /**
     * Initializes Vaadin service with UI configuration.
     *
     * @param event the service initialization event
     */
    @Override
    public void serviceInit(ServiceInitEvent event) {
        logger.debug("Initializing Vaadin service...");
        event.getSource().addUIInitListener(uiEvent -> configureUi(uiEvent.getUI()));
    }

    /**
     * Configures UI with theme, error handling and locale settings.
     */
    private void configureUi(UI ui) {
        logger.debug("UI initialized, applying configuration [uiId={}]", ui.getUIId());
        enableLumoDark(ui);
        installSafeErrorHandler(ui);
        applyPreferredLocale(ui);
        logger.debug("UI setup completed [uiId={}]", ui.getUIId());
    }

    /**
     * Applies Lumo dark theme to the UI.
     */
    private void enableLumoDark(UI ui) {
        ui.getElement().getThemeList().add(Lumo.DARK);
        if (logger.isTraceEnabled()) {
            logger.trace("Lumo dark theme enabled [uiId={}]", ui.getUIId());
        }
    }

    /**
     * Sets up error handler for safe navigation to error route.
     */
    private void installSafeErrorHandler(UI ui) {
        ui.getSession().setErrorHandler(errorEvent -> handleUiError(ui, errorEvent));
        if (logger.isTraceEnabled()) {
            logger.trace("UI error handler installed [uiId={}]", ui.getUIId());
        }
    }

    /**
     * Processes UI errors with safe navigation and cycle protection.
     */
    private void handleUiError(UI ui, ErrorEvent errorEvent) {
        if (logger.isTraceEnabled()) {
            logger.trace("UI error handled [uiId={}]", ui.getUIId(), errorEvent.getThrowable());
        }

        VaadinSession session = ui.getSession();
        String currentRoute = null;
        try {
            currentRoute = ui.getInternals().getActiveViewLocation().getPath();
        } catch (Exception e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Failed to read current route for error handling [uiId={}]", ui.getUIId(), e);
            }
        }

        if (logger.isErrorEnabled()) {
            logger.error(
                    "Unhandled UI error [uiId={}, route={}]", ui.getUIId(), currentRoute, errorEvent.getThrowable());
        }

        // Check for navigation guards to prevent infinite error loops
        boolean navigationInProgress = Boolean.TRUE.equals(session.getAttribute(ATTR_ERROR_NAV_GUARD));
        boolean onErrorView = ERROR_ROUTE.equals(currentRoute);

        if (navigationInProgress) {
            logger.warn(
                    "Skipping error navigation because previous navigation is still in progress [uiId={}, route={}]",
                    ui.getUIId(),
                    currentRoute);
            return;
        }

        if (onErrorView) {
            logger.warn(
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
                        logger.debug("Navigating to '{}' from '{}' [uiId={}]", ERROR_ROUTE, fromRoute, ui.getUIId());
                        ui.navigate(ERROR_ROUTE, QueryParameters.of("from", fromRoute));
                    } else {
                        logger.debug("Navigating to '{}' [uiId={}]", ERROR_ROUTE, ui.getUIId());
                        ui.navigate(ERROR_ROUTE);
                    }
                } catch (Exception navEx) {
                    logger.warn("Failed during error navigation [uiId={}]", ui.getUIId(), navEx);
                } finally {
                    // Always clear navigation guard, even if navigation fails
                    session.setAttribute(ATTR_ERROR_NAV_GUARD, Boolean.FALSE);
                }
            });
        } catch (Exception e) {
            logger.warn("Failed to schedule error navigation [uiId={}]", ui.getUIId(), e);
            // Clear navigation guard on scheduling failure
            session.setAttribute(ATTR_ERROR_NAV_GUARD, Boolean.FALSE);
        }
    }

    /**
     * Applies user's preferred locale from cookie or session.
     */
    private void applyPreferredLocale(UI ui) {
        VaadinSession session = ui.getSession();

        // First priority: locale from cookie (user's persistent preference)
        Locale cookieLocale = readLocaleFromCookie();
        if (cookieLocale != null) {
            session.setAttribute(LocaleConstants.SESSION_LOCALE_KEY, cookieLocale);
            ui.setLocale(cookieLocale);
            logger.debug("Locale set from cookie: {} [uiId={}]", cookieLocale, ui.getUIId());
            return;
        }

        // Second priority: locale from session (temporary preference)
        Object preferred = session.getAttribute(LocaleConstants.SESSION_LOCALE_KEY);
        if (preferred instanceof Locale locale) {
            ui.setLocale(locale);
            if (logger.isTraceEnabled()) {
                logger.trace("Locale set from session attribute: {} [uiId={}]", locale, ui.getUIId());
            }
        } else {
            // Fallback: default to English if no preference is set
            ui.setLocale(Locale.ENGLISH);
            if (logger.isTraceEnabled()) {
                logger.trace("Locale fallback applied: ENGLISH [uiId={}]", ui.getUIId());
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
                        if (logger.isDebugEnabled()) {
                            logger.debug("Locale cookie found: {}", locale);
                        }
                        return locale;
                    }
                }
            }
        } catch (Exception e) {
            // Log but don't fail if cookie reading fails
            if (logger.isTraceEnabled()) {
                logger.trace("Failed to read locale from cookie", e);
            }
        }
        return null;
    }
}
