package org.apolenkov.application.config;

/**
 * Centralized route constants for the application.
 * Prevents duplication of route strings across view classes.
 */
public final class RouteConstants {

    // Main routes
    public static final String HOME_ROUTE = "";
    public static final String DECKS_ROUTE = "decks";
    public static final String ERROR_ROUTE = "error";
    public static final String LOGIN_ROUTE = "login";
    public static final String REGISTER_ROUTE = "register";
    public static final String ACCESS_DENIED_ROUTE = "access-denied";

    // Deck routes
    public static final String DECK_ROUTE = "deck";
    public static final String DECK_CREATE_ROUTE = "deck/create";
    public static final String PRACTICE_ROUTE = "practice";

    // Admin routes
    public static final String ADMIN_USERS_ROUTE = "admin/users";
    public static final String ADMIN_NEWS_ROUTE = "admin/news";
    public static final String ADMIN_ROLE_AUDIT_ROUTE = "admin/role-audit";

    // Other routes
    public static final String STATS_ROUTE = "stats";
    public static final String SETTINGS_ROUTE = "settings";

    private RouteConstants() {
        // Utility class - prevent instantiation
    }
}
