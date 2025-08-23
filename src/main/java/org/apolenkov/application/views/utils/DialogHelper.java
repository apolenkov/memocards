package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Utility class for centralized dialog management.
 * Provides factory methods for creating consistently styled dialogs.
 */
public final class DialogHelper {

    private DialogHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a confirmation dialog with custom button text and actions.
     * Creates a standardized confirmation dialog with consistent styling and layout.
     *
     * @param title the title text to display in the dialog header
     * @param message the message content to display in the dialog body
     * @param confirmText the text to display on the confirm button
     * @param cancelText the text to display on the cancel button
     * @param onConfirm the action to execute when the confirm button is clicked
     * @param onCancel the action to execute when the cancel button is clicked
     * @return a configured Dialog component ready for display
     */
    public static Dialog createConfirmationDialog(
            String title,
            String message,
            String confirmText,
            String cancelText,
            Runnable onConfirm,
            Runnable onCancel) {
        Dialog dialog = new Dialog();
        dialog.addClassName("dialog-sm");

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3(title));
        layout.add(new Span(message));

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        Button confirmButton = new Button(confirmText, VaadinIcon.CHECK.create());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        confirmButton.addClickListener(e -> {
            onConfirm.run();
            dialog.close();
        });

        Button cancelButton = new Button(cancelText);
        cancelButton.addClickListener(e -> {
            if (onCancel != null) {
                onCancel.run();
            }
            dialog.close();
        });

        buttons.add(confirmButton, cancelButton);
        layout.add(buttons);

        dialog.add(layout);
        return dialog;
    }

    /**
     * Creates a confirmation dialog with default button text and actions.
     * Creates a confirmation dialog using standard "Confirm" and "Cancel" button text.
     *
     * @param title the title text to display in the dialog header
     * @param message the message content to display in the dialog body
     * @param onConfirm the action to execute when the confirm button is clicked
     * @param onCancel the action to execute when the cancel button is clicked
     * @return a configured Dialog component ready for display
     */
    public static Dialog createConfirmationDialog(String title, String message, Runnable onConfirm, Runnable onCancel) {
        return createConfirmationDialog(
                title, message, I18nHelper.tr("dialog.confirm"), I18nHelper.tr("dialog.cancel"), onConfirm, onCancel);
    }
}
