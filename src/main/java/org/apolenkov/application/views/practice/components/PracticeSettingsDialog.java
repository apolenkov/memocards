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
import org.apolenkov.application.views.practice.constants.PracticeConstants;
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
    private final transient PracticeSettingsComponents settingsComponents;

    /**
     * Creates a new PracticeSettingsDialog.
     *
     * @param service service for managing practice settings
     * @param components components factory for creating settings UI elements
     */
    public PracticeSettingsDialog(final PracticeSettingsService service, final PracticeSettingsComponents components) {
        this.practiceSettingsService = service;
        this.settingsComponents = components;
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

        Select<Integer> countSelect = settingsComponents.createCountSelect(practiceSettingsService);
        RadioButtonGroup<String> modeGroup = settingsComponents.createModeGroup(practiceSettingsService);
        RadioButtonGroup<String> dirGroup = settingsComponents.createDirectionGroup(practiceSettingsService);
        HorizontalLayout buttons = createButtonLayout(countSelect, modeGroup, dirGroup);

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
     * @return configured button layout
     */
    private HorizontalLayout createButtonLayout(
            final Select<Integer> countSelect,
            final RadioButtonGroup<String> modeGroup,
            final RadioButtonGroup<String> dirGroup) {

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        Button save = createSaveButton(countSelect, modeGroup, dirGroup);
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
     * @return configured save button
     */
    private Button createSaveButton(
            final Select<Integer> countSelect,
            final RadioButtonGroup<String> modeGroup,
            final RadioButtonGroup<String> dirGroup) {

        return ButtonHelper.createButton(
                getTranslation(PracticeConstants.SETTINGS_SAVE_KEY),
                e -> saveSettings(countSelect, modeGroup, dirGroup),
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
     */
    private void saveSettings(
            final Select<Integer> countSelect,
            final RadioButtonGroup<String> modeGroup,
            final RadioButtonGroup<String> dirGroup) {

        settingsComponents.saveSettings(practiceSettingsService, countSelect, modeGroup, dirGroup);
        NotificationHelper.showSuccessBottom(getTranslation(PracticeConstants.SETTINGS_SAVED_KEY));
        close();
    }
}
