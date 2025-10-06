package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;

/**
 * Dialog component for configuring practice session settings.
 * Allows users to customize their practice experience by setting
 * default values for card count, practice mode, and practice direction.
 */
public class PracticeSettingsDialog extends Dialog {

    // Dependencies
    private final transient PracticeSettingsService practiceSettingsService;

    /**
     * Creates a new PracticeSettingsDialog.
     *
     * @param service service for managing practice settings
     */
    public PracticeSettingsDialog(final PracticeSettingsService service) {
        this.practiceSettingsService = service;
    }

    /**
     * Initializes the dialog components when the component is attached to the UI.
     * This method is called by Vaadin when the component is added to the component tree.
     *
     * @param attachEvent the attaching event
     */
    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        setupDialog();
    }

    /**
     * Sets up the dialog with all components and layout.
     */
    private void setupDialog() {
        addClassName(PracticeConstants.DIALOG_MD_CLASS);

        VerticalLayout layout = createMainLayout();
        // Get translated texts
        String countLabel = getTranslation(PracticeConstants.SETTINGS_COUNT_KEY);
        String modeLabel = getTranslation(PracticeConstants.SETTINGS_MODE_KEY);
        String randomText = getTranslation(PracticeConstants.SETTINGS_MODE_RANDOM_KEY);
        String sequentialText = getTranslation(PracticeConstants.SETTINGS_MODE_SEQUENTIAL_KEY);
        String directionLabel = getTranslation(PracticeConstants.SETTINGS_DIRECTION_KEY);
        String frontToBackText = getTranslation(PracticeConstants.SETTINGS_DIRECTION_F2B_KEY);
        String backToFrontText = getTranslation(PracticeConstants.SETTINGS_DIRECTION_B2F_KEY);

        Select<Integer> countSelect = PracticeSettingsComponents.createCountSelect(practiceSettingsService, countLabel);
        RadioButtonGroup<String> modeGroup = PracticeSettingsComponents.createModeGroup(
                practiceSettingsService, modeLabel, randomText, sequentialText);
        RadioButtonGroup<String> dirGroup = PracticeSettingsComponents.createDirectionGroup(
                practiceSettingsService, directionLabel, frontToBackText, backToFrontText);
        HorizontalLayout buttons = createButtonLayout(countSelect, modeGroup, dirGroup, randomText, frontToBackText);

        layout.add(countSelect, modeGroup, dirGroup, buttons);
        add(layout);
    }

    /**
     * Creates the main layout with proper spacing and padding.
     *
     * @return configured vertical layout
     */
    private VerticalLayout createMainLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.add(new H3(getTranslation(PracticeConstants.SETTINGS_TITLE_KEY)));
        return layout;
    }

    /**
     * Creates the button layout with save and cancel actions.
     *
     * @param countSelect the count selection component
     * @param modeGroup the mode selection component
     * @param dirGroup the direction selection component
     * @param randomText the translated text for random mode
     * @param frontToBackText the translated text for front to back direction
     * @return configured button layout
     */
    private HorizontalLayout createButtonLayout(
            final Select<Integer> countSelect,
            final RadioButtonGroup<String> modeGroup,
            final RadioButtonGroup<String> dirGroup,
            final String randomText,
            final String frontToBackText) {

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        Button save = createSaveButton(countSelect, modeGroup, dirGroup, randomText, frontToBackText);
        Button cancel = createCancelButton();

        buttons.add(save, cancel);
        return buttons;
    }

    /**
     * Creates the save button with settings persistence logic.
     *
     * @param countSelect the count selection component
     * @param modeGroup the mode selection component
     * @param dirGroup the direction selection component
     * @param randomText the translated text for random mode
     * @param frontToBackText the translated text for front to back direction
     * @return configured save button
     */
    private Button createSaveButton(
            final Select<Integer> countSelect,
            final RadioButtonGroup<String> modeGroup,
            final RadioButtonGroup<String> dirGroup,
            final String randomText,
            final String frontToBackText) {

        return ButtonHelper.createButton(
                getTranslation(PracticeConstants.SETTINGS_SAVE_KEY),
                e -> saveSettings(countSelect, modeGroup, dirGroup, randomText, frontToBackText),
                ButtonVariant.LUMO_PRIMARY);
    }

    /**
     * Creates the cancel button.
     *
     * @return configured cancel button
     */
    private Button createCancelButton() {
        return ButtonHelper.createButton(
                getTranslation(PracticeConstants.COMMON_CANCEL_KEY), e -> close(), ButtonVariant.LUMO_TERTIARY);
    }

    /**
     * Saves all selected settings to the service and shows success notification.
     *
     * @param countSelect the count selection component
     * @param modeGroup the mode selection component
     * @param dirGroup the direction selection component
     * @param randomText the translated text for random mode
     * @param frontToBackText the translated text for front to back direction
     */
    private void saveSettings(
            final Select<Integer> countSelect,
            final RadioButtonGroup<String> modeGroup,
            final RadioButtonGroup<String> dirGroup,
            final String randomText,
            final String frontToBackText) {

        PracticeSettingsComponents.saveSettings(
                practiceSettingsService, countSelect, modeGroup, dirGroup, randomText, frontToBackText);
        NotificationHelper.showSuccessBottom(getTranslation(PracticeConstants.SETTINGS_SAVED_KEY));
        close();
    }
}
