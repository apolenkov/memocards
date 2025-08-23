package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import java.util.Locale;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Utility for retrieving localized messages using Vaadin's i18n provider.
 */
public final class I18nHelper {

    private I18nHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Translates a message key using the current UI/session locale.
     * Falls back to English or the key itself if translation is not available.
     *
     * @param key the translation key
     * @param params optional parameters for message formatting
     * @return translated message or the key as a fallback
     */
    public static String tr(String key, Object... params) {
        if (key == null || key.isBlank()) {
            return "";
        }

        try {
            // Try UI translation first (most efficient when available)
            String uiTranslation = tryUiTranslation(key, params);
            if (uiTranslation != null) {
                return uiTranslation;
            }

            // Fall back to manual provider lookup
            return tryProviderTranslation(key, params);

        } catch (Exception ignored) {
            // Fall through to returning the key
        }

        return key;
    }

    /**
     * Attempts to get translation directly from current UI.
     *
     * @param key the translation key
     * @param params optional parameters
     * @return translated string or null if UI is not available
     */
    private static String tryUiTranslation(String key, Object... params) {
        UI ui = UI.getCurrent();
        return ui != null ? ui.getTranslation(key, params) : null;
    }

    /**
     * Attempts to get translation using I18N provider directly.
     *
     * @param key the translation key
     * @param params optional parameters
     * @return translated string or the original key
     */
    private static String tryProviderTranslation(String key, Object... params) {
        I18NProvider provider = getI18NProvider();
        if (provider == null) {
            return key;
        }

        Locale locale = resolveLocale();
        return provider.getTranslation(key, locale, params);
    }

    /**
     * Resolves the current locale with fallback chain:
     * VaadinSession -> LocaleContextHolder -> English
     *
     * @return resolved locale, never null
     */
    private static Locale resolveLocale() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null && session.getLocale() != null) {
            return session.getLocale();
        }

        return LocaleContextHolder.getLocale();
    }

    /**
     * Gets the I18N provider from current Vaadin service.
     *
     * @return I18N provider or null if not available
     */
    private static I18NProvider getI18NProvider() {
        VaadinService service = VaadinService.getCurrent();
        return service != null ? service.getInstantiator().getI18NProvider() : null;
    }
}
