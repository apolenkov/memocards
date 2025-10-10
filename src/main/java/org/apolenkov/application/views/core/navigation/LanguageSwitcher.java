package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import org.apolenkov.application.config.constants.LocaleConstants;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.domain.usecase.UserUseCase;
import org.apolenkov.application.service.settings.UserSettingsService;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.springframework.stereotype.Component;

/**
 * Language selection component for the application.
 * Provides a user interface for selecting the preferred language
 * for the application with support for English, Russian, and Spanish.
 */
@Component
@UIScope
public class LanguageSwitcher extends HorizontalLayout {

    public static final String SESSION_LOCALE_KEY = LocaleConstants.SESSION_LOCALE_KEY;
    private static final String COOKIE_LOCALE_KEY = LocaleConstants.COOKIE_LOCALE_KEY;

    // Locale code constants for ComboBox values (matching translation keys)
    private static final String LOCALE_CODE_RU = "RU";
    private static final String LOCALE_CODE_ES = "ES";

    private final transient UserUseCase userUseCase;
    private final transient UserSettingsService userSettingsService;

    // Event Registrations
    private Registration localeChangeListenerRegistration;

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

        Span label = new Span(getTranslation(CoreConstants.LANGUAGE_LABEL_KEY));
        label.addClassName(CoreConstants.LANGUAGE_SWITCHER_LABEL_CLASS);

        ComboBox<String> combo = new ComboBox<>();

        String en = getTranslation(CoreConstants.LANGUAGE_EN_KEY);
        String ru = getTranslation(CoreConstants.LANGUAGE_RU_KEY);
        String es = getTranslation(CoreConstants.LANGUAGE_ES_KEY);
        combo.setItems(en, ru, es);

        Locale current = getCurrentLocale();
        String selectedValue = getSelectedValueForLocale(current, en, ru, es);
        combo.setValue(selectedValue);

        combo.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        combo.addClassName(CoreConstants.LANGUAGE_SWITCHER_COMBO_CLASS);
        combo.getElement()
                .setAttribute(CoreConstants.ARIA_LABEL_ATTRIBUTE, getTranslation(CoreConstants.LANGUAGE_LABEL_KEY));

        localeChangeListenerRegistration = combo.addValueChangeListener(event -> {
            String value = event.getValue();
            if (value != null) {
                Locale locale = mapSelectedValueToLocale(value);
                applyLocaleToApplication(locale);
            }
        });

        add(label, combo);
    }

    /**
     * Cleans up event listeners when the component is detached.
     * Prevents memory leaks by removing event listener registrations.
     *
     * @param detachEvent the detach event
     */
    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        if (localeChangeListenerRegistration != null) {
            localeChangeListenerRegistration.remove();
            localeChangeListenerRegistration = null;
        }
        super.onDetach(detachEvent);
    }

    /**
     * Maps the selected combo box value to a Locale object.
     * ComboBox values are locale codes: "EN", "RU", "ES" (from translations).
     *
     * @param selectedValue the selected value from the combo box
     * @return the corresponding locale
     */
    private Locale mapSelectedValueToLocale(final String selectedValue) {
        if (selectedValue == null) {
            return Locale.forLanguageTag(CoreConstants.EN_LOCALE);
        }

        String upperValue = selectedValue.toUpperCase();
        return switch (upperValue) {
            case LOCALE_CODE_RU -> Locale.forLanguageTag(CoreConstants.RU_LOCALE);
            case LOCALE_CODE_ES -> Locale.forLanguageTag(CoreConstants.ES_LOCALE);
            default -> Locale.forLanguageTag(CoreConstants.EN_LOCALE);
        };
    }

    /**
     * Applies the new locale to the application and persists the preference.
     *
     * @param newLocale the locale to apply
     */
    private void applyLocaleToApplication(final Locale newLocale) {
        VaadinSession.getCurrent().setAttribute(SESSION_LOCALE_KEY, newLocale.toLanguageTag());
        persistPreferredLocaleCookie(newLocale);
        persistIfLoggedIn(newLocale);

        getUI().ifPresent(ui -> {
            ui.setLocale(newLocale);
            ui.getPage().reload();
        });
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
        return switch (current.getLanguage().toLowerCase()) {
            case CoreConstants.RU_LOCALE -> ru;
            case CoreConstants.ES_LOCALE -> es;
            default -> en;
        };
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
        return getUI().map(UI::getLocale).orElse(Locale.forLanguageTag(CoreConstants.EN_LOCALE));
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
        cookie.setPath(RouteConstants.ROOT_PATH);
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
