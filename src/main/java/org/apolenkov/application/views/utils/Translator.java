package org.apolenkov.application.views.utils;

/**
 * Abstraction for translating message keys into localized strings.
 */
public interface Translator {

    /**
     * Translates the given key using current user locale.
     *
     * @param key message key
     * @param params optional parameters for formatting
     * @return localized string
     */
    String tr(String key, Object... params);
}
