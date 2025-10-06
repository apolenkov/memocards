package org.apolenkov.application.views.core.navigation;

import java.util.function.UnaryOperator;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.views.core.constants.CoreConstants;

/**
 * Factory for creating menu button configurations.
 * Centralizes menu button creation logic and reduces duplication.
 */
public final class MenuButtonFactory {

    // Navigation test IDs
    private static final String NAV_DECKS_TEST_ID = "nav-decks";
    private static final String NAV_STATS_TEST_ID = "nav-stats";
    private static final String NAV_SETTINGS_TEST_ID = "nav-settings";
    private static final String NAV_ADMIN_CONTENT_TEST_ID = "nav-admin-content";
    private static final String LOGOUT_TEST_ID = "nav-logout";

    // Logout button constants
    private static final String LOGOUT_ROUTE = RouteConstants.ROOT_PATH + RouteConstants.LOGOUT_ROUTE;

    /**
     * Creates a menu button for the decks navigation.
     *
     * @param translationProvider function to get translations for button text
     * @return configured menu button for decks
     */
    public static MenuButton createDecksButton(final UnaryOperator<String> translationProvider) {
        return new MenuButton(
                translationProvider.apply(CoreConstants.MAIN_DECKS_KEY),
                RouteConstants.ROOT_PATH + RouteConstants.DECKS_ROUTE,
                NAV_DECKS_TEST_ID,
                false,
                SecurityConstants.ROLE_USER);
    }

    /**
     * Creates a menu button for the stats navigation.
     *
     * @param translationProvider function to get translations for button text
     * @return configured menu button for stats
     */
    public static MenuButton createStatsButton(final UnaryOperator<String> translationProvider) {
        return new MenuButton(
                translationProvider.apply(CoreConstants.MAIN_STATS_KEY),
                RouteConstants.ROOT_PATH + RouteConstants.STATS_ROUTE,
                NAV_STATS_TEST_ID,
                false,
                SecurityConstants.ROLE_USER);
    }

    /**
     * Creates a menu button for the settings navigation.
     *
     * @param translationProvider function to get translations for button text
     * @return configured menu button for settings
     */
    public static MenuButton createSettingsButton(final UnaryOperator<String> translationProvider) {
        return new MenuButton(
                translationProvider.apply(CoreConstants.MAIN_SETTINGS_KEY),
                RouteConstants.ROOT_PATH + RouteConstants.SETTINGS_ROUTE,
                NAV_SETTINGS_TEST_ID,
                false,
                SecurityConstants.ROLE_USER);
    }

    /**
     * Creates a menu button for the admin content navigation.
     *
     * @param translationProvider function to get translations for button text
     * @return configured menu button for admin content
     */
    public static MenuButton createAdminContentButton(final UnaryOperator<String> translationProvider) {
        return new MenuButton(
                translationProvider.apply(CoreConstants.ADMIN_CONTENT_TITLE_KEY),
                RouteConstants.ROOT_PATH + RouteConstants.ADMIN_CONTENT_ROUTE,
                NAV_ADMIN_CONTENT_TEST_ID,
                false,
                SecurityConstants.ROLE_ADMIN);
    }

    /**
     * Creates a menu button for the logout action.
     *
     * @param translationProvider function to get translations for button text
     * @return configured menu button for logout
     */
    public static MenuButton createLogoutButton(final UnaryOperator<String> translationProvider) {
        return new MenuButton(
                translationProvider.apply(CoreConstants.MAIN_LOGOUT_KEY), LOGOUT_ROUTE, LOGOUT_TEST_ID, false);
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private MenuButtonFactory() {
        throw new UnsupportedOperationException("Utility class");
    }
}
