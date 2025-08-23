package org.apolenkov.application.config;

/**
 * Constants for locale and theme preferences.
 * Defines constant values for managing user preferences related to locale
 * and theme settings. Used for session storage keys and cookie names.
 */
public final class LocaleConstants {

    /**
     * Private constructor to prevent instantiation.
     */
    private LocaleConstants() {}

    /**
     * Session storage key for storing user's preferred locale.
     */
    public static final String SESSION_LOCALE_KEY = "preferredLocale";

    /**
     * Cookie name for storing user's preferred locale.
     */
    public static final String COOKIE_LOCALE_KEY = "preferredLocale";
}
