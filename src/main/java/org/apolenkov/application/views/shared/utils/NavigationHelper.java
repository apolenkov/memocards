package org.apolenkov.application.views.shared.utils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.QueryParameters;
import java.util.Optional;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.views.landing.pages.LandingView;

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
     * Navigates to a route with a single parameter.
     * Performs programmatic navigation to the given route with a single path parameter.
     *
     * @param route the base route path to navigate to
     * @param parameter the single parameter to append to the route
     */
    public static void navigateTo(final String route, final String parameter) {
        getCurrentUI().ifPresent(ui -> ui.navigate(route + RouteConstants.ROOT_PATH + parameter));
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
     * Navigate to login page
     *
     * <p>Navigates to the user authentication page where users can
     * sign in to their accounts. This is typically used for
     * redirecting unauthenticated users or after logout.</p>
     */
    public static void navigateToLogin() {
        navigateTo(RouteConstants.LOGIN_ROUTE);
    }

    /**
     * Navigate to register page
     *
     * <p>Navigates to the user registration page where new users can
     * create accounts. This is typically used for redirecting users
     * who want to sign up for the service.</p>
     */
    public static void navigateToRegister() {
        navigateTo(RouteConstants.REGISTER_ROUTE);
    }

    /**
     * Navigate to forgot password page
     *
     * <p>Navigates to the password recovery page where users can
     * request a password reset. This is typically used when users
     * have forgotten their login credentials.</p>
     */
    public static void navigateToForgotPassword() {
        navigateTo(RouteConstants.FORGOT_PASSWORD_ROUTE);
    }

    /**
     * Navigate to reset password page with token
     *
     * <p>Navigates to the password reset page with a specific token.
     * This is typically used when users click on password reset links
     * from their email to complete the password reset process.</p>
     *
     * @param token the password reset token for validation
     * @throws IllegalArgumentException if token is null or empty
     */
    public static void navigateToResetPassword(final String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Reset password token cannot be null or empty");
        }
        navigateTo(RouteConstants.RESET_PASSWORD_ROUTE, token);
    }

    /**
     * Navigate to home page
     *
     * <p>Navigates to the main home page of the application.
     * This is typically the landing page for authenticated users.</p>
     */
    public static void navigateToHome() {
        navigateTo(RouteConstants.HOME_ROUTE);
    }

    /**
     * Forward to home page using BeforeEnterEvent
     *
     * <p>Forwards to the main home page using BeforeEnterEvent.
     * This method should be used in BeforeEnterObserver implementations
     * to properly redirect without showing the current view content.</p>
     *
     * @param event the BeforeEnterEvent to use for forwarding
     */
    public static void forwardToHome(final BeforeEnterEvent event) {
        event.forwardTo(LandingView.class);
    }

    /**
     * Navigate to error page
     *
     * <p>Navigates to the error page with a reference to the page
     * that caused the error. This is useful for error handling scenarios
     * where we need to redirect users to a safe error page.</p>
     *
     * @param fromRoute the route that caused the error
     */
    public static void navigateToError(final String fromRoute) {
        getCurrentUI().ifPresent(ui -> ui.navigate(RouteConstants.ERROR_ROUTE, QueryParameters.of("from", fromRoute)));
    }

    /**
     * Navigate to error page with custom parameters
     *
     * <p>Navigates to the error page with custom query parameters.
     * This is useful for system error handling where we need to pass
     * additional error information.</p>
     *
     * @param route the route to navigate to
     * @param params the query parameters to include
     */
    public static void navigateToError(final String route, final QueryParameters params) {
        getCurrentUI().ifPresent(ui -> ui.navigate(route, params));
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
