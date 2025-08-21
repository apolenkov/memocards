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
 * Eliminates duplication of dialog creation patterns across the application.
 */
public final class DialogHelper {

    private DialogHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a confirmation dialog
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
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);

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
     * Create a confirmation dialog with default button texts
     */
    public static Dialog createConfirmationDialog(String title, String message, Runnable onConfirm, Runnable onCancel) {
        return createConfirmationDialog(title, message, "Confirm", "Cancel", onConfirm, onCancel);
    }
}
