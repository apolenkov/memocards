package org.apolenkov.application.config.constants;

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

    /**
     * Locale code for Russian language (uppercase for ComboBox values).
     */
    public static final String LOCALE_CODE_RU = "RU";

    /**
     * Locale code for Spanish language (uppercase for ComboBox values).
     */
    public static final String LOCALE_CODE_ES = "ES";
}
