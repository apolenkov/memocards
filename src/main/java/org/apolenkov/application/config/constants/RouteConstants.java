package org.apolenkov.application.config.constants;

/**
 * Centralized route constants for the application.
 * Prevents duplication of route strings across view classes.
 * Defines all application routes in one place for easy maintenance.
 */
public final class RouteConstants {

    /**
     * Root path constant for URL construction.
     */
    public static final String ROOT_PATH = "/";

    /**
     * Route for decks list view.
     */
    public static final String DECKS_ROUTE = "decks";

    /**
     * Route for error view.
     */
    public static final String ERROR_ROUTE = "error-page";

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
     * Route for register view.
     */
    public static final String REGISTER_ROUTE = "register";

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
     * Route for settings view.
     */
    public static final String SETTINGS_ROUTE = "settings";

    /**
     * Route for logout action.
     */
    public static final String LOGOUT_ROUTE = "logout";

    /**
     * Route for stats view.
     */
    public static final String STATS_ROUTE = "stats";

    /**
     * Route for admin content view.
     */
    public static final String ADMIN_CONTENT_ROUTE = "admin/content";

    /**
     * Route for admin news view.
     */
    public static final String ADMIN_NEWS_ROUTE = "admin/news";

    /**
     * Route for 404 error view.
     */
    public static final String ERROR_404_ROUTE = "error/404";

    /**
     * Actuator endpoints - public access (no authentication required).
     */
    public static final String ACTUATOR_HEALTH = "/actuator/health";

    public static final String ACTUATOR_INFO = "/actuator/info";

    /**
     * Actuator base path for authenticated endpoints.
     */
    public static final String ACTUATOR_BASE_PATH = "/actuator/**";

    /**
     * Private constructor to prevent instantiation.
     */
    private RouteConstants() {
        // Utility class - prevent instantiation
    }
}
