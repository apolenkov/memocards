package org.apolenkov.application.config.locale;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.Cookie;
import java.util.Locale;
import org.apolenkov.application.config.LocaleConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Manages locale configuration for Vaadin UI instances.
 *
 * <p>This component is responsible for applying user's preferred locale
 * from cookies or session attributes.
 */
@Component
public class LocaleConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocaleConfiguration.class);

    /**
     * Applies user's preferred locale from cookie or session.
     *
     * @param ui the UI instance to apply locale settings to
     */
    public void applyPreferredLocale(final UI ui) {
        VaadinSession session = ui.getSession();

        // First priority: locale from cookie (user's persistent preference)
        Locale cookieLocale = readLocaleFromCookie();
        if (cookieLocale != null) {
            session.setAttribute(LocaleConstants.SESSION_LOCALE_KEY, cookieLocale);
            ui.setLocale(cookieLocale);
            LOGGER.debug("Locale set from cookie: {} [uiId={}]", cookieLocale, ui.getUIId());
            return;
        }

        // Second priority: locale from session (temporary preference)
        Object preferred = session.getAttribute(LocaleConstants.SESSION_LOCALE_KEY);
        if (preferred instanceof Locale locale) {
            ui.setLocale(locale);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Locale set from session attribute: {} [uiId={}]", locale, ui.getUIId());
            }
        } else {
            // Fallback: default to English if no preference is set
            ui.setLocale(Locale.ENGLISH);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Locale fallback applied: ENGLISH [uiId={}]", ui.getUIId());
            }
        }
    }

    /**
     * Reads locale preference from cookie.
     *
     * @return user's preferred locale or null if not set
     */
    private Locale readLocaleFromCookie() {
        try {
            // Get current HTTP request to access cookies
            VaadinServletRequest req = (VaadinServletRequest) VaadinService.getCurrentRequest();
            if (req != null && req.getCookies() != null) {
                // Search for locale preference cookie
                for (Cookie c : req.getCookies()) {
                    if (LocaleConstants.COOKIE_LOCALE_KEY.equals(c.getName())) {
                        // Parse locale from cookie value and validate
                        Locale locale = Locale.forLanguageTag(c.getValue());
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Locale cookie found: {}", locale);
                        }
                        return locale;
                    }
                }
            }
        } catch (Exception e) {
            // Log but don't fail if cookie reading fails
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Failed to read locale from cookie", e);
            }
        }
        return null;
    }
}
