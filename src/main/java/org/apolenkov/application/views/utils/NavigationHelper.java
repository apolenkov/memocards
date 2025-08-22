package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.QueryParameters;
import java.util.Optional;
import org.apolenkov.application.config.RouteConstants;

/**
 * Utility class for centralized navigation operations.
 *
 * <p>This utility class provides helper methods for common navigation tasks
 * throughout the application. It simplifies navigation operations and ensures
 * consistent behavior across different components and views.</p>
 *
 * <p>The class offers:</p>
 * <ul>
 *   <li>Programmatic navigation to specific routes</li>
 *   <li>Router link creation with consistent styling</li>
 *   <li>Navigation state management and updates</li>
 *   <li>Centralized navigation logic for maintainability</li>
 * </ul>
 *
 * <p>All navigation operations use Vaadin's routing system to ensure
 * proper page transitions and state management.</p>
 */
public final class NavigationHelper {

    private NavigationHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Navigate to a route without parameters
     *
     * <p>Performs programmatic navigation to the given route path
     * using the current UI context. This method is useful for
     * navigation triggered by user actions or application logic.</p>
     *
     * @param route the route path to navigate to (e.g., "/decks", "/settings")
     */
    public static void navigateTo(String route) {
        getCurrentUI().ifPresent(ui -> ui.navigate(route));
    }

    /**
     * Navigate to a route with query parameters
     *
     * <p>Performs programmatic navigation to the given route with
     * additional query parameters. This method is useful for passing
     * data or state information during navigation.</p>
     *
     * @param route the route path to navigate to
     * @param params the query parameters to include in the navigation
     */
    public static void navigateTo(String route, QueryParameters params) {
        getCurrentUI().ifPresent(ui -> ui.navigate(route, params));
    }

    /**
     * Navigate to a route with a single parameter
     *
     * <p>Performs programmatic navigation to the given route with
     * a single path parameter. The parameter is appended to the route
     * as a path segment.</p>
     *
     * @param route the base route path to navigate to
     * @param parameter the single parameter to append to the route
     */
    public static void navigateTo(String route, String parameter) {
        getCurrentUI().ifPresent(ui -> ui.navigate(route + "/" + parameter));
    }

    /**
     * Navigate to error page with "from" parameter
     *
     * <p>Navigates to the error page while preserving information about
     * the route that caused the error. This helps with error handling
     * and user navigation recovery.</p>
     *
     * @param fromRoute the route that caused the error or from which the user came
     */
    public static void navigateToError(String fromRoute) {
        navigateTo(RouteConstants.ERROR_ROUTE, QueryParameters.of("from", fromRoute));
    }

    /**
     * Navigate to deck view
     *
     * <p>Navigates to the detailed view of a specific deck using
     * the deck's unique identifier. This method provides a convenient
     * way to access deck details from various parts of the application.</p>
     *
     * @param deckId the unique identifier of the deck to view
     */
    public static void navigateToDeck(Long deckId) {
        navigateTo(RouteConstants.DECK_ROUTE, deckId.toString());
    }

    /**
     * Navigate to practice view
     *
     * <p>Navigates to the practice session view for a specific deck.
     * This method provides quick access to start practicing with
     * the selected deck's flashcards.</p>
     *
     * @param deckId the unique identifier of the deck to practice with
     */
    public static void navigateToPractice(Long deckId) {
        navigateTo(RouteConstants.PRACTICE_ROUTE, deckId.toString());
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
    public static void setLocale(java.util.Locale locale) {
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
