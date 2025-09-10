package org.apolenkov.application.views.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.LayoutHelper;

/**
 * Dialog component for configuring practice session settings.
 * Allows users to customize their practice experience by setting
 * default values for card count, practice mode, and practice direction.
 */
public class PracticeSettingsDialog extends Dialog {

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

        addClassName("dialog-md");

        // Create main layout with proper spacing and padding
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.add(new H3(getTranslation("settings.title")));

        // Configure card count selection with predefined options
        Select<Integer> countSelect = new Select<>();
        countSelect.setLabel(getTranslation("settings.count"));
        countSelect.setItems(5, 10, 15, 20, 25, 30);
        countSelect.setValue(practiceSettingsService.getDefaultCount());

        // Configure practice mode selection (random vs. sequential)
        RadioButtonGroup<String> modeGroup = new RadioButtonGroup<>();
        modeGroup.setLabel(getTranslation("settings.mode"));
        String random = getTranslation("settings.mode.random");
        String seq = getTranslation("settings.mode.sequential");
        modeGroup.setItems(random, seq);
        modeGroup.setValue(practiceSettingsService.isDefaultRandomOrder() ? random : seq);

        // Configure practice direction selection (front-to-back vs. back-to-front)
        RadioButtonGroup<String> dirGroup = new RadioButtonGroup<>();
        dirGroup.setLabel(getTranslation("settings.direction"));
        String f2b = getTranslation("settings.direction.f2b");
        String b2f = getTranslation("settings.direction.b2f");
        dirGroup.setItems(f2b, b2f);
        dirGroup.setValue(
                practiceSettingsService.getDefaultDirection()
                                == org.apolenkov.application.model.PracticeDirection.FRONT_TO_BACK
                        ? f2b
                        : b2f);

        // Create button layout with save and cancel actions
        HorizontalLayout buttons = LayoutHelper.createButtonLayout();

        Button save = ButtonHelper.createButton(
                getTranslation("settings.save"),
                e -> {
                    // Save all selected settings to the service
                    practiceSettingsService.setDefaultCount(countSelect.getValue());
                    practiceSettingsService.setDefaultRandomOrder(
                            modeGroup.getValue().equals(random));
                    practiceSettingsService.setDefaultDirection(
                            dirGroup.getValue().equals(f2b)
                                    ? org.apolenkov.application.model.PracticeDirection.FRONT_TO_BACK
                                    : org.apolenkov.application.model.PracticeDirection.BACK_TO_FRONT);
                    // Show success notification and close dialog
                    Notification n = Notification.show(
                            getTranslation("settings" + ".saved"), 3000, Notification.Position.BOTTOM_START);
                    n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    close();
                },
                ButtonVariant.LUMO_PRIMARY);
        Button cancel =
                ButtonHelper.createButton(getTranslation("common.cancel"), e -> close(), ButtonVariant.LUMO_TERTIARY);
        buttons.add(save, cancel);

        // Assemble final layout and add to dialog
        layout.add(countSelect, modeGroup, dirGroup, buttons);
        add(layout);
    }
}
