package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
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
     * Create a confirmation dialog with custom title and message
     */
    public static Dialog createConfirmDialog(String message) {
        return createConfirmationDialog("Confirm", message, null, null);
    }

    /**
     * Create a confirmation dialog
     */
    public static Dialog createConfirmationDialog(String title, String message, Runnable onConfirm, Runnable onCancel) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3(title));
        layout.add(new Span(message));

        HorizontalLayout buttons = new HorizontalLayout();

        Button confirmButton = new Button("Confirm", VaadinIcon.CHECK.create());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.addClickListener(e -> {
            onConfirm.run();
            dialog.close();
        });

        Button cancelButton = new Button("Cancel");
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
     * Create a delete confirmation dialog
     */
    public static Dialog createDeleteConfirmationDialog(String itemName, Runnable onDelete) {
        return createConfirmationDialog(
                "Confirm Deletion", "Are you sure you want to delete " + itemName + "?", onDelete, null);
    }

    /**
     * Create a simple info dialog
     */
    public static Dialog createInfoDialog(String title, String message) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3(title));
        layout.add(new Span(message));

        Button okButton = new Button("OK", VaadinIcon.CHECK.create());
        okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        okButton.addClickListener(e -> dialog.close());

        layout.add(okButton);
        dialog.add(layout);

        return dialog;
    }

    /**
     * Create a form dialog
     */
    public static Dialog createFormDialog(
            String title, VerticalLayout formContent, Runnable onSave, Runnable onCancel) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3(title));
        layout.add(formContent);

        HorizontalLayout buttons = new HorizontalLayout();

        Button saveButton = new Button("Save", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            onSave.run();
            dialog.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(e -> {
            if (onCancel != null) {
                onCancel.run();
            }
            dialog.close();
        });

        buttons.add(saveButton, cancelButton);
        layout.add(buttons);

        dialog.add(layout);
        return dialog;
    }

    /**
     * Create a loading dialog
     */
    public static Dialog createLoadingDialog(String message) {
        Dialog dialog = new Dialog();
        dialog.setWidth("300px");
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.add(new Span(message));
        layout.add(new Span("Please wait..."));

        dialog.add(layout);
        return dialog;
    }

    /**
     * Show a confirmation dialog and return true if confirmed
     */
    public static boolean showConfirmation(String title, String message) {
        boolean[] confirmed = {false};

        Dialog dialog = createConfirmationDialog(title, message, () -> confirmed[0] = true, () -> confirmed[0] = false);

        dialog.open();

        // Wait for dialog to close
        while (dialog.isOpened()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return confirmed[0];
    }
}
