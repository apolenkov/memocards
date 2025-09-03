package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.QueryParameters;
import java.util.Locale;
import java.util.Optional;
import org.apolenkov.application.config.constants.RouteConstants;

/**
 * Utility class for centralized navigation operations.
 * Provides helper methods for common navigation tasks throughout the application.
 */
public final class NavigationHelper {

    private NavigationHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Navigates to a route without parameters.
     * Performs programmatic navigation to the given route path.
     *
     * @param route the route path to navigate to (e.g., "/decks", "/settings")
     */
    public static void navigateTo(final String route) {
        getCurrentUI().ifPresent(ui -> ui.navigate(route));
    }

    /**
     * Navigates to a route with query parameters.
     * Performs programmatic navigation to the given route with additional query parameters.
     *
     * @param route the route path to navigate to
     * @param params the query parameters to include in the navigation
     */
    public static void navigateTo(final String route, final QueryParameters params) {
        getCurrentUI().ifPresent(ui -> ui.navigate(route, params));
    }

    /**
     * Navigates to a route with a single parameter.
     * Performs programmatic navigation to the given route with a single path parameter.
     *
     * @param route the base route path to navigate to
     * @param parameter the single parameter to append to the route
     */
    public static void navigateTo(final String route, final String parameter) {
        getCurrentUI().ifPresent(ui -> ui.navigate(route + "/" + parameter));
    }

    /**
     * Navigates to error page with "from" parameter.
     * Navigates to the error page while preserving information about the route that caused the error.
     *
     * @param fromRoute the route that caused the error or from which the user came
     */
    public static void navigateToError(final String fromRoute) {
        navigateTo(RouteConstants.ERROR_ROUTE, QueryParameters.of("from", fromRoute));
    }

    /**
     * Navigates to deck view.
     * Navigates to the detailed view of a specific deck using the deck's unique identifier.
     *
     * @param deckId the unique identifier of the deck to view
     * @throws IllegalArgumentException if deckId is null
     */
    public static void navigateToDeck(final long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive");
        }
        navigateTo(RouteConstants.DECK_ROUTE, String.valueOf(deckId));
    }

    /**
     * Navigate to practice view
     *
     * <p>Navigates to the practice session view for a specific deck.
     * This method provides quick access to start practicing with
     * the selected deck's flashcards.</p>
     *
     * @param deckId the unique identifier of the deck to practice with
     * @throws IllegalArgumentException if deckId is null
     */
    public static void navigateToPractice(final long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive");
        }
        navigateTo(RouteConstants.PRACTICE_ROUTE, String.valueOf(deckId));
    }

    /**
     * Navigate to decks list
     *
     * <p>Navigates to the main decks listing page where users can
     * view, manage, and organize their flashcard decks. This is
     * typically the primary navigation destination for deck management.</p>
     */
    public static void navigateToDecks() {
        navigateTo(RouteConstants.DECKS_ROUTE);
    }

    /**
     * Set locale for current UI
     *
     * <p>Updates the locale setting for the current user interface.
     * This affects the language and regional formatting of the
     * application for the current session.</p>
     *
     * @param locale the locale to set for the current UI
     */
    public static void setLocale(final Locale locale) {
        getCurrentUI().ifPresent(ui -> ui.setLocale(locale));
    }

    /**
     * Get current UI safely
     *
     * <p>Retrieves the current UI context in a safe manner, handling
     * cases where the UI might not be available. This method prevents
     * null pointer exceptions during navigation operations.</p>
     *
     * @return an Optional containing the current UI if available
     */
    private static Optional<UI> getCurrentUI() {
        return Optional.ofNullable(UI.getCurrent());
    }
}
