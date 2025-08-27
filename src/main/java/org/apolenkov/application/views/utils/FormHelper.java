package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Utility class for centralized form creation and management.
 * Provides factory methods for creating consistently styled form fields.
 */
public final class FormHelper {

    private FormHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a required text field with the specified label and placeholder.
     *
     * @param label the label text to display above the field
     * @param placeholder the placeholder text to show when the field is empty
     * @return a configured TextField marked as required with placeholder
     */
    public static TextField createRequiredTextField(final String label, final String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setRequired(true);
        field.setClearButtonVisible(true);

        return field;
    }

    /**
     * Creates an optional text field with the specified label and placeholder.
     *
     * @param label the label text to display above the field
     * @param placeholder the placeholder text to show when the field is empty
     * @return a configured TextField marked as optional with placeholder
     */
    public static TextField createOptionalTextField(final String label, final String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setClearButtonVisible(true);

        return field;
    }

    /**
     * Creates a text area with the specified label and placeholder.
     *
     * @param label the label text to display above the text area
     * @param placeholder the placeholder text to show when the area is empty
     * @return a configured TextArea with placeholder and consistent styling
     */
    public static TextArea createTextArea(final String label, final String placeholder) {
        TextArea area = new TextArea(label);
        area.setPlaceholder(placeholder);
        area.setClearButtonVisible(true);
        area.addClassName("text-area--sm");

        return area;
    }
}
