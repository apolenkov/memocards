package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.Locale;
import org.apolenkov.application.domain.usecase.UserUseCase;
import org.apolenkov.application.service.settings.UserSettingsService;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Desktop language selection component for the application.
 * Provides a user interface for selecting the preferred language
 * for the application with support for English, Russian, and Spanish.
 */
@Component
@UIScope
public class LanguageSwitcher extends BaseLanguageSwitcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageSwitcher.class);

    // Event Registrations
    private Registration localeChangeListenerRegistration;

    // Initialization flag
    private boolean hasBeenInitialized = false;

    /**
     * Creates a new LanguageSwitcher with required dependencies.
     *
     * @param useCase service for user operations and current user information
     * @param settingsService service for persisting user preferences
     */
    public LanguageSwitcher(final UserUseCase useCase, final UserSettingsService settingsService) {
        super(useCase, settingsService);
    }

    /**
     * Initializes the language selection interface when the component is attached to the UI.
     * At this point, getTranslation() is safe to use.
     *
     * @param attachEvent the attaching event
     */
    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Check if already initialized
        if (hasBeenInitialized) {
            return;
        }

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
            LOGGER.info("Language changed to: {}", value);
            if (value != null) {
                Locale locale = mapSelectedValueToLocale(value);
                applyLocaleToApplication(locale);
            }
        });

        add(label, combo);

        hasBeenInitialized = true;
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
}
