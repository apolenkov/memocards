package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Utility class for centralized form creation and management.
 * Eliminates duplication of form setup patterns across the application.
 */
public final class FormHelper {

    private FormHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a required text field
     */
    public static TextField createRequiredTextField(String label, String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setRequired(true);
        field.setClearButtonVisible(true);

        return field;
    }

    /**
     * Create an optional text field
     */
    public static TextField createOptionalTextField(String label, String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setClearButtonVisible(true);

        return field;
    }

    /**
     * Create a text area
     */
    public static TextArea createTextArea(String label, String placeholder) {
        TextArea area = new TextArea(label);
        area.setPlaceholder(placeholder);
        area.setClearButtonVisible(true);
        area.addClassName("text-area--sm");

        return area;
    }
}
