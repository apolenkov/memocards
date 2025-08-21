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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import org.apolenkov.application.service.UserSettingsService;
import org.apolenkov.application.usecase.UserUseCase;
import org.springframework.stereotype.Component;

@Component
@UIScope
public class LanguageSwitcher extends HorizontalLayout {

    public static final String SESSION_LOCALE_KEY = org.apolenkov.application.config.LocaleConstants.SESSION_LOCALE_KEY;
    private static final String COOKIE_LOCALE_KEY = org.apolenkov.application.config.LocaleConstants.COOKIE_LOCALE_KEY;

    private final transient UserUseCase userUseCase;
    private final transient UserSettingsService userSettingsService;

    public LanguageSwitcher(UserUseCase userUseCase, UserSettingsService userSettingsService) {
        this.userUseCase = userUseCase;
        this.userSettingsService = userSettingsService;
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
        combo.setWidth("92px");
        combo.getElement().setAttribute("aria-label", getTranslation("language.label"));

        combo.addValueChangeListener(e -> {
            if (e.getValue() == null) return;
            Locale newLocale;
            if (e.getValue().equalsIgnoreCase(ru)) {
                newLocale = Locale.forLanguageTag("ru");
            } else if (e.getValue().equalsIgnoreCase(es)) {
                newLocale = Locale.forLanguageTag("es");
            } else {
                newLocale = Locale.ENGLISH;
            }
            VaadinSession.getCurrent().setAttribute(SESSION_LOCALE_KEY, newLocale);
            persistPreferredLocaleCookie(newLocale);
            persistIfLoggedIn(newLocale);
            getUI().ifPresent(ui -> {
                ui.setLocale(newLocale);
                ui.getPage().reload();
            });
        });

        add(label, combo);
    }

    private String getSelectedValueForLocale(Locale current, String en, String ru, String es) {
        String language = current.getLanguage().toLowerCase();
        if ("ru".equals(language)) {
            return ru;
        } else if ("es".equals(language)) {
            return es;
        } else {
            return en;
        }
    }

    private Locale getCurrentLocale() {
        Object sessionAttribute = VaadinSession.getCurrent().getAttribute(SESSION_LOCALE_KEY);
        if (sessionAttribute instanceof Locale locale) {
            return locale;
        }
        // Fallback to cookie if present
        Locale fromCookie = readPreferredLocaleCookie();
        if (fromCookie != null) {
            // Store into session for this UI and apply
            VaadinSession.getCurrent().setAttribute(SESSION_LOCALE_KEY, fromCookie);
            getUI().ifPresent(ui -> ui.setLocale(fromCookie));
            return fromCookie;
        }
        return getUI().map(UI::getLocale).orElse(Locale.ENGLISH);
    }

    private void persistIfLoggedIn(Locale locale) {
        if (userUseCase == null || userSettingsService == null) return;
        try {
            long userId = userUseCase.getCurrentUser().getId();
            userSettingsService.setPreferredLocale(userId, locale);
        } catch (Exception ex) {
            // Intentionally ignoring exceptions for anonymous users or when user service is unavailable
            // This allows the language switcher to work without authentication
        }
    }

    private void persistPreferredLocaleCookie(Locale locale) {
        VaadinServletResponse vaadinResponse = (VaadinServletResponse) VaadinService.getCurrentResponse();
        if (vaadinResponse == null) return;
        HttpServletResponse response = vaadinResponse.getHttpServletResponse();
        Cookie cookie = new Cookie(COOKIE_LOCALE_KEY, locale.toLanguageTag());
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 365); // 1 year
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }

    private Locale readPreferredLocaleCookie() {
        VaadinServletRequest vaadinRequest = (VaadinServletRequest) VaadinService.getCurrentRequest();
        if (vaadinRequest == null) return null;
        HttpServletRequest request = vaadinRequest.getHttpServletRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
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
