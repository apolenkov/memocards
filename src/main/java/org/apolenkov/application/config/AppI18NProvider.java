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
 * Vaadin internationalization (i18n) provider implementation.
 *
 * <p>Supports English, Russian, and Spanish locales with parameter substitution.</p>
 */
@Component
public class AppI18NProvider implements I18NProvider {

    /**
     * Prefix for the message bundle files.
     *
     * <p>Base name for properties files (messages_en.properties, etc.).</p>
     */
    public static final String BUNDLE_PREFIX = "i18n.messages";

    /**
     * List of locales supported by this provider.
     *
     * <p>Supports English, Russian, and Spanish locales.</p>
     */
    private static final List<Locale> PROVIDED_LOCALES =
            Arrays.asList(Locale.ENGLISH, Locale.forLanguageTag("ru"), Locale.forLanguageTag("es"));

    /**
     * Returns the list of locales supported by this provider.
     *
     * @return list of supported locales
     */
    @Override
    public List<Locale> getProvidedLocales() {
        return PROVIDED_LOCALES;
    }

    /**
     * Retrieves a translated message for the specified key and locale.
     *
     * @param key the message key to translate
     * @param locale the locale for the translation (null defaults to English)
     * @param params optional parameters for message formatting
     * @return the translated message, or the key if translation is not found
     */
    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
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
