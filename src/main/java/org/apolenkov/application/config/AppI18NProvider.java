package org.apolenkov.application.config;

import com.vaadin.flow.i18n.I18NProvider;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.springframework.stereotype.Component;

/**
 * Vaadin internationalization (i18n) provider.
 * Supports English, Russian, and Spanish locales with parameter substitution.
 */
@Component
public class AppI18NProvider implements I18NProvider {

    /**
     * Message bundle files prefix.
     */
    public static final String BUNDLE_PREFIX = "i18n.messages";

    /**
     * Supported locales: English, Russian, and Spanish.
     */
    private static final List<Locale> PROVIDED_LOCALES =
            Arrays.asList(Locale.ENGLISH, Locale.forLanguageTag("ru"), Locale.forLanguageTag("es"));

    /**
     * Returns supported locales.
     *
     * @return list of supported locales
     */
    @Override
    public List<Locale> getProvidedLocales() {
        return PROVIDED_LOCALES;
    }

    /**
     * Gets translated message for the specified key and locale.
     *
     * @param key message key to translate
     * @param locale locale for translation (null defaults to English)
     * @param params optional parameters for message formatting
     * @return translated message or key if translation not found
     */
    @Override
    public String getTranslation(final String key, final Locale locale, final Object... params) {
        if (key == null) {
            return "";
        }
        final Locale used = locale != null ? locale : Locale.ENGLISH;
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(
                    BUNDLE_PREFIX,
                    used,
                    ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
            String value = bundle.containsKey(key) ? bundle.getString(key) : key;
            return params != null && params.length > 0 ? MessageFormat.format(value, params) : value;
        } catch (MissingResourceException e) {
            return key;
        }
    }
}
