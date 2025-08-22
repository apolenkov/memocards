package org.apolenkov.application.config;

/**
 * Constants for locale and theme preferences.
 *
 * <p>This utility class defines constant values used throughout the application
 * for managing user preferences related to locale (language) and theme settings.
 * The constants are used for session storage keys and cookie names to maintain
 * user preferences across browser sessions.</p>
 *
 * <p>The class is designed as a utility class and cannot be instantiated.</p>
 *
 */
public final class LocaleConstants {

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>This class is designed as a utility class containing only constants
     * and should not be instantiated.</p>
     */
    private LocaleConstants() {}

    /**
     * Session storage key for storing the user's preferred locale.
     *
     * <p>This constant is used as a key in the HTTP session to store
     * the user's language preference for the current session.</p>
     */
    public static final String SESSION_LOCALE_KEY = "preferredLocale";

    /**
     * Cookie name for storing the user's preferred locale.
     *
     * <p>This constant is used as the name for a persistent cookie that
     * stores the user's language preference across browser sessions.</p>
     */
    public static final String COOKIE_LOCALE_KEY = "preferredLocale";

    /**
     * Session storage key for storing the user's preferred theme.
     *
     * <p>This constant is used as a key in the HTTP session to store
     * the user's theme preference (e.g., light/dark mode) for the current session.</p>
     */
    public static final String SESSION_THEME_KEY = "preferredTheme";

    /**
     * Cookie name for storing the user's preferred theme.
     *
     * <p>This constant is used as the name for a persistent cookie that
     * stores the user's theme preference across browser sessions.</p>
     */
    public static final String COOKIE_THEME_KEY = "preferredTheme";
}
