package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
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
     * @param countLabel the translated label for the count select
     * @return configured count select component
     */
    public static Select<Integer> createCountSelect(
            final PracticeSettingsService practiceSettingsService, final String countLabel) {
        Select<Integer> countSelect = new Select<>();
        countSelect.setLabel(countLabel);
        countSelect.setItems(5, 10, 15, 20, 25, 30);
        countSelect.setValue(practiceSettingsService.getDefaultCount());
        return countSelect;
    }

    /**
     * Creates a practice mode selection component.
     *
     * @param practiceSettingsService the settings service for default values
     * @param modeLabel the translated label for the mode group
     * @param randomText the translated text for random mode
     * @param sequentialText the translated text for sequential mode
     * @return configured mode radio button group
     */
    public static RadioButtonGroup<String> createModeGroup(
            final PracticeSettingsService practiceSettingsService,
            final String modeLabel,
            final String randomText,
            final String sequentialText) {
        RadioButtonGroup<String> modeGroup = new RadioButtonGroup<>();
        modeGroup.setLabel(modeLabel);
        modeGroup.setItems(randomText, sequentialText);
        modeGroup.setValue(practiceSettingsService.isDefaultRandomOrder() ? randomText : sequentialText);
        return modeGroup;
    }

    /**
     * Creates a practice direction selection component.
     *
     * @param practiceSettingsService the settings service for default values
     * @param directionLabel the translated label for the direction group
     * @param frontToBackText the translated text for front to back direction
     * @param backToFrontText the translated text for back to front direction
     * @return configured direction radio button group
     */
    public static RadioButtonGroup<String> createDirectionGroup(
            final PracticeSettingsService practiceSettingsService,
            final String directionLabel,
            final String frontToBackText,
            final String backToFrontText) {
        RadioButtonGroup<String> dirGroup = new RadioButtonGroup<>();
        dirGroup.setLabel(directionLabel);
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
     * @param randomText the translated text for random mode (for comparison)
     * @param frontToBackText the translated text for front to back direction (for comparison)
     */
    public static void saveSettings(
            final PracticeSettingsService practiceSettingsService,
            final Select<Integer> countSelect,
            final RadioButtonGroup<String> modeGroup,
            final RadioButtonGroup<String> dirGroup,
            final String randomText,
            final String frontToBackText) {

        practiceSettingsService.setDefaultCount(countSelect.getValue());
        practiceSettingsService.setDefaultRandomOrder(modeGroup.getValue().equals(randomText));
        practiceSettingsService.setDefaultDirection(
                dirGroup.getValue().equals(frontToBackText)
                        ? PracticeDirection.FRONT_TO_BACK
                        : PracticeDirection.BACK_TO_FRONT);
    }

    // Private constructor to prevent instantiation
    private PracticeSettingsComponents() {
        throw new UnsupportedOperationException("Utility class");
    }
}
