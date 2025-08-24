package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import java.util.Locale;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Spring bean implementation of Translator backed by Vaadin I18NProvider.
 */
@Component
public class VaadinTranslator implements Translator {

    /**
     * Translates the given key using current user locale.
     *
     * @param key message key
     * @param params optional parameters for formatting
     * @return localized string
     */
    @Override
    public String tr(String key, Object... params) {
        if (key == null || key.isBlank()) {
            return "";
        }

        try {
            UI ui = UI.getCurrent();
            if (ui != null) {
                return ui.getTranslation(key, params);
            }

            VaadinService service = VaadinService.getCurrent();
            I18NProvider provider = service != null ? service.getInstantiator().getI18NProvider() : null;

            Locale locale;
            VaadinSession session = VaadinSession.getCurrent();
            if (session != null && session.getLocale() != null) {
                locale = session.getLocale();
            } else {
                locale = LocaleContextHolder.getLocale();
            }

            if (provider != null) {
                return provider.getTranslation(key, locale != null ? locale : Locale.ENGLISH, params);
            }
        } catch (Exception ignored) {
            // ignore and fall through to returning the key
        }

        return key;
    }
}
