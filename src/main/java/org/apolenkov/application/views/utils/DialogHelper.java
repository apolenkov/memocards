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

    private static final String I18N_DIALOG_CONFIRM = "dialog.confirm";
    private static final String I18N_DIALOG_CANCEL = "dialog.cancel";

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
     * Creates a confirmation dialog using provided Translator for default button texts.
     *
     * @param title dialog title
     * @param message dialog message
     * @param translator translator bean to resolve i18n keys
     * @param onConfirm confirm action
     * @param onCancel cancel action (nullable)
     * @return configured dialog
     */
    public static Dialog createConfirmationDialog(
            String title, String message, Translator translator, Runnable onConfirm, Runnable onCancel) {
        String confirm = translator != null ? translator.tr(I18N_DIALOG_CONFIRM) : I18nHelper.tr(I18N_DIALOG_CONFIRM);
        String cancel = translator != null ? translator.tr(I18N_DIALOG_CANCEL) : I18nHelper.tr(I18N_DIALOG_CANCEL);
        return createConfirmationDialog(title, message, confirm, cancel, onConfirm, onCancel);
    }
}
