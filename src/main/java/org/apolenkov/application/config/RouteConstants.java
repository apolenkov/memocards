package org.apolenkov.application.config;

/**
 * Centralized route constants for the application.
 *
 * <p>Prevents duplication of route strings across view classes.
 * Defines all application routes in one place for easy maintenance.</p>
 */
public final class RouteConstants {

    public static final String DECKS_ROUTE = "decks";
    public static final String ERROR_ROUTE = "error";

    public static final String DECK_ROUTE = "deck";
    public static final String PRACTICE_ROUTE = "practice";

    private RouteConstants() {
        // Utility class - prevent instantiation
    }
}
