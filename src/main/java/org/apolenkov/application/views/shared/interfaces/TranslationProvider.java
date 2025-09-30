package org.apolenkov.application.views.shared.interfaces;

/**
 * Interface for providing translations.
 * Used to decouple translation functionality from Vaadin components.
 */
public interface TranslationProvider {
    /**
     * Gets a translated string for the given key with optional parameters.
     *
     * @param key the translation key
     * @param params optional parameters for string formatting
     * @return the translated string
     */
    String getTranslation(String key, Object... params);
}
