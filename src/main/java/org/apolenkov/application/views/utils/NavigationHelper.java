package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.QueryParameters;
import java.util.Optional;
import org.apolenkov.application.config.RouteConstants;

/**
 * Utility class for centralized navigation logic.
 * Eliminates duplication of getUI().ifPresent(ui -> ui.navigate(...)) patterns.
 */
public final class NavigationHelper {

    private NavigationHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Navigate to a route without parameters
     */
    public static void navigateTo(String route) {
        getCurrentUI().ifPresent(ui -> ui.navigate(route));
    }

    /**
     * Navigate to a route with query parameters
     */
    public static void navigateTo(String route, QueryParameters params) {
        getCurrentUI().ifPresent(ui -> ui.navigate(route, params));
    }

    /**
     * Navigate to a route with a single parameter
     */
    public static void navigateTo(String route, String parameter) {
        getCurrentUI().ifPresent(ui -> ui.navigate(route + "/" + parameter));
    }

    /**
     * Navigate to error page with "from" parameter
     */
    public static void navigateToError(String fromRoute) {
        navigateTo(RouteConstants.ERROR_ROUTE, QueryParameters.of("from", fromRoute));
    }

    /**
     * Navigate to deck view
     */
    public static void navigateToDeck(Long deckId) {
        navigateTo(RouteConstants.DECK_ROUTE, deckId.toString());
    }

    /**
     * Navigate to practice view
     */
    public static void navigateToPractice(Long deckId) {
        navigateTo(RouteConstants.PRACTICE_ROUTE, deckId.toString());
    }

    /**
     * Navigate to decks list
     */
    public static void navigateToDecks() {
        navigateTo(RouteConstants.DECKS_ROUTE);
    }

    /**
     * Set locale for current UI
     */
    public static void setLocale(java.util.Locale locale) {
        getCurrentUI().ifPresent(ui -> ui.setLocale(locale));
    }

    /**
     * Get current UI safely
     */
    private static Optional<UI> getCurrentUI() {
        return Optional.ofNullable(UI.getCurrent());
    }
}
