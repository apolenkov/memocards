package org.apolenkov.application.config;

/**
 * Centralized route constants for the application.
 * Prevents duplication of route strings across view classes.
 * Defines all application routes in one place for easy maintenance.
 */
public final class RouteConstants {

    /**
     * Route for decks list view.
     */
    public static final String DECKS_ROUTE = "decks";

    /**
     * Route for error view.
     */
    public static final String ERROR_ROUTE = "error";

    /**
     * Route for individual deck view.
     */
    public static final String DECK_ROUTE = "deck";

    /**
     * Route for practice view.
     */
    public static final String PRACTICE_ROUTE = "practice";

    /**
     * Route for login view.
     */
    public static final String LOGIN_ROUTE = "login";

    /**
     * Route for home view.
     */
    public static final String HOME_ROUTE = "";

    /**
     * Route for forgot password view.
     */
    public static final String FORGOT_PASSWORD_ROUTE = "forgot-password";

    /**
     * Route for reset password view.
     */
    public static final String RESET_PASSWORD_ROUTE = "reset-password";

    /**
     * Private constructor to prevent instantiation.
     */
    private RouteConstants() {
        // Utility class - prevent instantiation
    }
}
