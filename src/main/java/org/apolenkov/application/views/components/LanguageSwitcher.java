package org.apolenkov.application.views.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import org.apolenkov.application.service.UserSettingsService;
import org.apolenkov.application.usecase.UserUseCase;
import org.springframework.stereotype.Component;

/**
 * Language selection component for the application.
 * Provides a user interface for selecting the preferred language
 * for the application with support for English, Russian, and Spanish.
 */
@Component
@UIScope
public class LanguageSwitcher extends HorizontalLayout {

    public static final String SESSION_LOCALE_KEY =
            org.apolenkov.application.config.constants.LocaleConstants.SESSION_LOCALE_KEY;
    private static final String COOKIE_LOCALE_KEY =
            org.apolenkov.application.config.constants.LocaleConstants.COOKIE_LOCALE_KEY;

    private final transient UserUseCase userUseCase;
    private final transient UserSettingsService userSettingsService;

    /**
     * Creates a new LanguageSwitcher with required dependencies.
     *
     * @param useCase service for user operations and current user information
     * @param settingsService service for persisting user preferences
     */
    public LanguageSwitcher(final UserUseCase useCase, final UserSettingsService settingsService) {
        this.userUseCase = useCase;
        this.userSettingsService = settingsService;
    }

    /**
     * Initializes the language selection interface after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    private void init() {
        setSpacing(true);
        setPadding(false);
        setAlignItems(Alignment.CENTER);

        Span label = new Span(getTranslation("language.label"));
        label.addClassName("language-switcher__label");

        ComboBox<String> combo = new ComboBox<>();

        String en = getTranslation("language.en");
        String ru = getTranslation("language.ru");
        String es = getTranslation("language.es");
        combo.setItems(en, ru, es);

        Locale current = getCurrentLocale();
        String selectedValue = getSelectedValueForLocale(current, en, ru, es);
        combo.setValue(selectedValue);

        combo.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        combo.addClassName("language-switcher__combo");
        combo.getElement().setAttribute("aria-label", getTranslation("language.label"));

        combo.addValueChangeListener(e -> {
            if (e.getValue() == null) {
                return;
            }
            Locale newLocale;
            if (e.getValue().equalsIgnoreCase(ru)) {
                newLocale = Locale.forLanguageTag("ru");
            } else if (e.getValue().equalsIgnoreCase(es)) {
                newLocale = Locale.forLanguageTag("es");
            } else {
                newLocale = Locale.ENGLISH;
            }
            VaadinSession.getCurrent().setAttribute(SESSION_LOCALE_KEY, newLocale.toLanguageTag());
            persistPreferredLocaleCookie(newLocale);
            persistIfLoggedIn(newLocale);
            getUI().ifPresent(ui -> {
                ui.setLocale(newLocale);
                ui.getPage().reload();
            });
        });

        add(label, combo);
    }

    /**
     * Maps the current locale to the appropriate display value for the combo box.
     * Converts the current locale to the corresponding display text that matches the combo box items.
     *
     * @param current the current locale to map
     * @param en the English display text
     * @param ru the Russian display text
     * @param es the Spanish display text
     * @return the appropriate display text for the current locale
     */
    private String getSelectedValueForLocale(final Locale current, final String en, final String ru, final String es) {
        String language = current.getLanguage().toLowerCase();
        if ("ru".equals(language)) {
            return ru;
        } else if ("es".equals(language)) {
            return es;
        } else {
            return en;
        }
    }

    /**
     * Retrieves the current locale from session, cookie, or UI context.
     * Priority order: Session attribute (highest), Cookie value, UI locale, English (fallback).
     *
     * @return the current locale, never null
     */
    private Locale getCurrentLocale() {
        Object sessionAttribute = VaadinSession.getCurrent().getAttribute(SESSION_LOCALE_KEY);
        if (sessionAttribute instanceof String localeTag) {
            return Locale.forLanguageTag(localeTag);
        }
        // Fallback to cookie if present
        Locale fromCookie = readPreferredLocaleCookie();
        if (fromCookie != null) {
            // Store into session for this UI and apply
            VaadinSession.getCurrent().setAttribute(SESSION_LOCALE_KEY, fromCookie.toLanguageTag());
            getUI().ifPresent(ui -> ui.setLocale(fromCookie));
            return fromCookie;
        }
        return getUI().map(UI::getLocale).orElse(Locale.ENGLISH);
    }

    /**
     * Persists the preferred locale to user settings if the user is logged in.
     *
     * <p>Attempts to save the language preference to the user's profile
     * for future sessions. This method gracefully handles cases where
     * the user is not authenticated or the service is unavailable.</p>
     *
     * @param locale the locale preference to persist
     */
    private void persistIfLoggedIn(final Locale locale) {
        if (userUseCase == null || userSettingsService == null) {
            return;
        }
        try {
            long userId = userUseCase.getCurrentUser().getId();
            userSettingsService.setPreferredLocale(userId, locale);
        } catch (Exception ex) {
            // Intentionally ignoring exceptions for anonymous users or when user service is unavailable
            // This allows the language switcher to work without authentication
        }
    }

    /**
     * Persists the preferred locale to a secure HTTP-only cookie.
     *
     * <p>Creates a secure cookie with the user's language preference that
     * will persist across browser sessions. The cookie is configured with
     * appropriate security settings including HTTP-only and secure flags.</p>
     *
     * @param locale the locale preference to store in the cookie
     */
    private void persistPreferredLocaleCookie(final Locale locale) {
        VaadinServletResponse vaadinResponse = (VaadinServletResponse) VaadinService.getCurrentResponse();
        if (vaadinResponse == null) {
            return;
        }
        HttpServletResponse response = vaadinResponse.getHttpServletResponse();
        Cookie cookie = new Cookie(COOKIE_LOCALE_KEY, locale.toLanguageTag());
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 365); // 1 year
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }

    /**
     * Reads the preferred locale from the HTTP cookie.
     *
     * <p>Retrieves the language preference from the cookie if present and
     * valid. Handles cases where cookies are not available or contain
     * invalid locale values gracefully.</p>
     *
     * @return the locale from the cookie, or null if not available or invalid
     */
    private Locale readPreferredLocaleCookie() {
        VaadinServletRequest vaadinRequest = (VaadinServletRequest) VaadinService.getCurrentRequest();
        if (vaadinRequest == null) {
            return null;
        }
        HttpServletRequest request = vaadinRequest.getHttpServletRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_LOCALE_KEY.equals(cookie.getName())) {
                try {
                    return Locale.forLanguageTag(cookie.getValue());
                } catch (Exception ignored) {
                    // Invalid locale format in cookie, continue searching
                }
            }
        }
        return null;
    }
}
