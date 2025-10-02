package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import java.util.function.UnaryOperator;
import org.apolenkov.application.model.PracticeDirection;
import org.apolenkov.application.service.PracticeSettingsService;

/**
 * Reusable components for practice settings configuration.
 * Provides common UI components for practice settings dialogs and views.
 */
public final class PracticeSettingsComponents {

    /**
     * Creates a card count selection component.
     *
     * @param practiceSettingsService the settings service for default values
     * @param translationProvider function to provide translations
     * @return configured count select component
     */
    public static Select<Integer> createCountSelect(
            final PracticeSettingsService practiceSettingsService, final UnaryOperator<String> translationProvider) {
        Select<Integer> countSelect = new Select<>();
        countSelect.setLabel(translationProvider.apply(PracticeConstants.SETTINGS_COUNT_KEY));
        countSelect.setItems(5, 10, 15, 20, 25, 30);
        countSelect.setValue(practiceSettingsService.getDefaultCount());
        return countSelect;
    }

    /**
     * Creates a practice mode selection component.
     *
     * @param practiceSettingsService the settings service for default values
     * @param translationProvider function to provide translations
     * @return configured mode radio button group
     */
    public static RadioButtonGroup<String> createModeGroup(
            final PracticeSettingsService practiceSettingsService, final UnaryOperator<String> translationProvider) {
        RadioButtonGroup<String> modeGroup = new RadioButtonGroup<>();
        modeGroup.setLabel(translationProvider.apply(PracticeConstants.SETTINGS_MODE_KEY));
        String random = translationProvider.apply(PracticeConstants.SETTINGS_MODE_RANDOM_KEY);
        String seq = translationProvider.apply(PracticeConstants.SETTINGS_MODE_SEQUENTIAL_KEY);
        modeGroup.setItems(random, seq);
        modeGroup.setValue(practiceSettingsService.isDefaultRandomOrder() ? random : seq);
        return modeGroup;
    }

    /**
     * Creates a practice direction selection component.
     *
     * @param practiceSettingsService the settings service for default values
     * @param translationProvider function to provide translations
     * @return configured direction radio button group
     */
    public static RadioButtonGroup<String> createDirectionGroup(
            final PracticeSettingsService practiceSettingsService, final UnaryOperator<String> translationProvider) {
        RadioButtonGroup<String> dirGroup = new RadioButtonGroup<>();
        dirGroup.setLabel(translationProvider.apply(PracticeConstants.SETTINGS_DIRECTION_KEY));
        String f2b = translationProvider.apply(PracticeConstants.SETTINGS_DIRECTION_F2B_KEY);
        String b2f = translationProvider.apply(PracticeConstants.SETTINGS_DIRECTION_B2F_KEY);
        dirGroup.setItems(f2b, b2f);
        dirGroup.setValue(practiceSettingsService.getDefaultDirection() == PracticeDirection.FRONT_TO_BACK ? f2b : b2f);
        return dirGroup;
    }

    /**
     * Saves practice settings from the components to the service.
     *
     * @param practiceSettingsService the settings service to save to
     * @param countSelect the count selection component
     * @param modeGroup the mode selection component
     * @param dirGroup the direction selection component
     * @param translationProvider function to provide translations
     */
    public static void saveSettings(
            final PracticeSettingsService practiceSettingsService,
            final Select<Integer> countSelect,
            final RadioButtonGroup<String> modeGroup,
            final RadioButtonGroup<String> dirGroup,
            final UnaryOperator<String> translationProvider) {

        practiceSettingsService.setDefaultCount(countSelect.getValue());
        practiceSettingsService.setDefaultRandomOrder(
                modeGroup.getValue().equals(translationProvider.apply(PracticeConstants.SETTINGS_MODE_RANDOM_KEY)));
        practiceSettingsService.setDefaultDirection(
                dirGroup.getValue().equals(translationProvider.apply(PracticeConstants.SETTINGS_DIRECTION_F2B_KEY))
                        ? PracticeDirection.FRONT_TO_BACK
                        : PracticeDirection.BACK_TO_FRONT);
    }

    // Private constructor to prevent instantiation
    private PracticeSettingsComponents() {
        throw new UnsupportedOperationException("Utility class");
    }
}
