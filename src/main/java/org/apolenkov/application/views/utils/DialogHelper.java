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
 *
 * <p>This utility class provides factory methods for creating consistently
 * styled dialogs throughout the application. It eliminates duplication
 * of dialog creation patterns and ensures uniform appearance and behavior.</p>
 *
 * <p>The class offers:</p>
 * <ul>
 *   <li>Confirmation dialog creation with consistent styling</li>
 *   <li>Standardized dialog layout and button configuration</li>
 *   <li>Consistent dialog behavior and user interaction</li>
 *   <li>Centralized dialog styling for maintainability</li>
 * </ul>
 *
 * <p>All dialogs created through this utility automatically include
 * appropriate styling, button configuration, and user interaction
 * patterns for consistent user experience across the application.</p>
 */
public final class DialogHelper {

    private DialogHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a confirmation dialog with custom button text and actions.
     *
     * <p>Creates a standardized confirmation dialog with consistent styling,
     * layout, and button configuration. The dialog includes a title, message
     * content, and customizable action buttons for user confirmation or cancellation.</p>
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
     *
     * <p>Creates a confirmation dialog using standard "Confirm" and "Cancel"
     * button text. This method provides a simplified interface for common
     * confirmation scenarios while maintaining consistent styling and behavior.</p>
     *
     * @param title the title text to display in the dialog header
     * @param message the message content to display in the dialog body
     * @param onConfirm the action to execute when the confirm button is clicked
     * @param onCancel the action to execute when the cancel button is clicked
     * @return a configured Dialog component ready for display
     */
    public static Dialog createConfirmationDialog(String title, String message, Runnable onConfirm, Runnable onCancel) {
        return createConfirmationDialog(title, message, "Confirm", "Cancel", onConfirm, onCancel);
    }
}
