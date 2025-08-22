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
 * <p>This class provides internationalization support for the Vaadin application
 * by implementing the I18NProvider interface. It manages message bundles for
 * multiple languages and provides translation functionality with parameter
 * substitution support.</p>
 *
 * <p>The provider supports English, Russian, and Spanish locales, and uses
 * properties files for message storage. It includes fallback mechanisms to
 * handle missing translations gracefully.</p>
 *
 */
@Component
public class AppI18NProvider implements I18NProvider {

    /**
     * Prefix for the message bundle files.
     *
     * <p>This constant defines the base name for the properties files that
     * contain the translated messages. The actual files will be named
     * messages_en.properties, messages_ru.properties, etc.</p>
     */
    public static final String BUNDLE_PREFIX = "i18n.messages";

    /**
     * List of locales supported by this provider.
     *
     * <p>Defines the available languages for the application. Currently
     * supports English, Russian, and Spanish locales.</p>
     */
    private static final List<Locale> PROVIDED_LOCALES =
            Arrays.asList(Locale.ENGLISH, new Locale("ru"), new Locale("es"));

    /**
     * Returns the list of locales supported by this provider.
     *
     * <p>This method is called by Vaadin to determine which languages
     * are available for the application's internationalization features.</p>
     *
     * @return a list of supported locales
     */
    @Override
    public List<Locale> getProvidedLocales() {
        return PROVIDED_LOCALES;
    }

    /**
     * Retrieves a translated message for the specified key and locale.
     *
     * <p>This method looks up the translation for the given key in the appropriate
     * message bundle for the specified locale. If the locale is null, it defaults
     * to English. The method supports parameter substitution using MessageFormat
     * syntax.</p>
     *
     * <p>If the translation key is not found, the method returns the key itself
     * as a fallback. If the key is null, an empty string is returned.</p>
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
