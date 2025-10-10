package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.spring.annotation.UIScope;
import org.apolenkov.application.model.PracticeDirection;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.views.practice.constants.PracticeConstants;
import org.springframework.stereotype.Component;

/**
 * Reusable components for practice settings configuration.
 * Provides common UI components for practice settings dialogs and views.
 * Uses @UIScope to access Vaadin I18N provider for translations.
 */
@Component
@UIScope
public final class PracticeSettingsComponents implements LocaleChangeObserver {

    /**
     * Creates a card count selection component.
     *
     * @param practiceSettingsService the settings service for default values
     * @return configured count select component
     */
    public Select<Integer> createCountSelect(final PracticeSettingsService practiceSettingsService) {
        Select<Integer> countSelect = new Select<>();
        countSelect.setLabel(getTranslation(PracticeConstants.SETTINGS_COUNT_KEY));
        countSelect.setItems(5, 10, 15, 20, 25, 30);
        countSelect.setValue(practiceSettingsService.getDefaultCount());
        return countSelect;
    }

    /**
     * Creates a practice mode selection component.
     *
     * @param practiceSettingsService the settings service for default values
     * @return configured mode radio button group
     */
    public RadioButtonGroup<String> createModeGroup(final PracticeSettingsService practiceSettingsService) {
        String randomText = getTranslation(PracticeConstants.SETTINGS_MODE_RANDOM_KEY);
        String sequentialText = getTranslation(PracticeConstants.SETTINGS_MODE_SEQUENTIAL_KEY);

        RadioButtonGroup<String> modeGroup = new RadioButtonGroup<>();
        modeGroup.setLabel(getTranslation(PracticeConstants.SETTINGS_MODE_KEY));
        modeGroup.setItems(randomText, sequentialText);
        modeGroup.setValue(practiceSettingsService.isDefaultRandomOrder() ? randomText : sequentialText);
        return modeGroup;
    }

    /**
     * Creates a practice direction selection component.
     *
     * @param practiceSettingsService the settings service for default values
     * @return configured direction radio button group
     */
    public RadioButtonGroup<String> createDirectionGroup(final PracticeSettingsService practiceSettingsService) {
        String frontToBackText = getTranslation(PracticeConstants.SETTINGS_DIRECTION_F2B_KEY);
        String backToFrontText = getTranslation(PracticeConstants.SETTINGS_DIRECTION_B2F_KEY);

        RadioButtonGroup<String> dirGroup = new RadioButtonGroup<>();
        dirGroup.setLabel(getTranslation(PracticeConstants.SETTINGS_DIRECTION_KEY));
        dirGroup.setItems(frontToBackText, backToFrontText);
        dirGroup.setValue(
                practiceSettingsService.getDefaultDirection() == PracticeDirection.FRONT_TO_BACK
                        ? frontToBackText
                        : backToFrontText);
        return dirGroup;
    }

    /**
     * Saves practice settings from the components to the service.
     *
     * @param practiceSettingsService the settings service to save to
     * @param countSelect the count selection component
     * @param modeGroup the mode selection component
     * @param dirGroup the direction selection component
     */
    public void saveSettings(
            final PracticeSettingsService practiceSettingsService,
            final Select<Integer> countSelect,
            final RadioButtonGroup<String> modeGroup,
            final RadioButtonGroup<String> dirGroup) {

        String randomText = getTranslation(PracticeConstants.SETTINGS_MODE_RANDOM_KEY);
        String frontToBackText = getTranslation(PracticeConstants.SETTINGS_DIRECTION_F2B_KEY);

        practiceSettingsService.setDefaultCount(countSelect.getValue());
        practiceSettingsService.setDefaultRandomOrder(modeGroup.getValue().equals(randomText));
        practiceSettingsService.setDefaultDirection(
                dirGroup.getValue().equals(frontToBackText)
                        ? PracticeDirection.FRONT_TO_BACK
                        : PracticeDirection.BACK_TO_FRONT);
    }

    /**
     * Gets translation for the specified key using current UI locale.
     *
     * @param key the translation key
     * @param params optional parameters
     * @return translated text
     */
    private String getTranslation(final String key, final Object... params) {
        return UI.getCurrent().getTranslation(key, params);
    }

    @Override
    public void localeChange(final LocaleChangeEvent event) {
        // Handle locale changes if needed in the future
    }
}
