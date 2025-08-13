package org.apolenkov.application.views.components;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.VaadinSession;

import java.util.Locale;
import org.apolenkov.application.application.usecase.UserUseCase;
import org.apolenkov.application.service.UserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;

public class LanguageSwitcher extends HorizontalLayout {

    public static final String SESSION_LOCALE_KEY = "preferredLocale";

    @Autowired
    private transient UserUseCase userUseCase;

    @Autowired
    private transient UserSettingsService userSettingsService;

    public LanguageSwitcher() {
        setSpacing(true);
        setPadding(false);

        Span label = new Span(getTranslation("language.label"));
        ComboBox<String> combo = new ComboBox<>();
        String en = getTranslation("language.en");
        String ru = getTranslation("language.ru");
        String es = getTranslation("language.es");
        combo.setItems(en, ru, es);

        Locale current = getCurrentLocale();
        combo.setValue(current.getLanguage().equalsIgnoreCase("ru") ? ru : current.getLanguage().equalsIgnoreCase("es") ? es : en);

        combo.addValueChangeListener(e -> {
            if (e.getValue() == null) return;
            Locale newLocale;
            if (e.getValue().equalsIgnoreCase(ru)) {
                newLocale = new Locale("ru");
            } else if (e.getValue().equalsIgnoreCase(es)) {
                newLocale = new Locale("es");
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
        return getUI().map(ui -> ui.getLocale()).orElse(Locale.ENGLISH);
    }

    private void persistIfLoggedIn(Locale locale) {
        try {
            if (userUseCase != null && userSettingsService != null) {
                long userId = userUseCase.getCurrentUser().getId();
                userSettingsService.setPreferredLocale(userId, locale);
            }
        } catch (Exception ignore) {
            // anonymous or not available
        }
    }
}


