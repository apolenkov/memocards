package org.apolenkov.application.views.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.Locale;
import org.apolenkov.application.service.UserSettingsService;
import org.apolenkov.application.usecase.UserUseCase;
import org.springframework.stereotype.Component;

@Component
@UIScope
public class LanguageSwitcher extends HorizontalLayout {

    public static final String SESSION_LOCALE_KEY = "preferredLocale";

    private final transient UserUseCase userUseCase;
    private final transient UserSettingsService userSettingsService;

    public LanguageSwitcher(UserUseCase userUseCase, UserSettingsService userSettingsService) {
        this.userUseCase = userUseCase;
        this.userSettingsService = userSettingsService;
        setSpacing(true);
        setPadding(false);
        addClassName("language-switcher");

        Span label = new Span(getTranslation("language.label"));
        label.addClassName("language-switcher__label");
        ComboBox<String> combo = new ComboBox<>();
        combo.addClassName("language-switcher__combo");
        String en = getTranslation("language.en");
        String ru = getTranslation("language.ru");
        String es = getTranslation("language.es");
        combo.setItems(en, ru, es);

        Locale current = getCurrentLocale();
        combo.setValue(
                current.getLanguage().equalsIgnoreCase("ru")
                        ? ru
                        : current.getLanguage().equalsIgnoreCase("es") ? es : en);

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
            persistIfLoggedIn(newLocale);
            getUI().ifPresent(ui -> {
                ui.setLocale(newLocale);
                ui.getPage().reload();
            });
        });

        add(label, combo);
    }

    private Locale getCurrentLocale() {
        Object attr = VaadinSession.getCurrent().getAttribute(SESSION_LOCALE_KEY);
        if (attr instanceof Locale) {
            return (Locale) attr;
        }
        return getUI().map(UI::getLocale).orElse(Locale.ENGLISH);
    }

    private void persistIfLoggedIn(Locale locale) {
        if (userUseCase == null || userSettingsService == null) return;
        try {
            long userId = userUseCase.getCurrentUser().getId();
            userSettingsService.setPreferredLocale(userId, locale);
        } catch (Exception ex) {
            // ignore for anonymous
        }
    }
}
