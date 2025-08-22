package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Utility class for centralized form creation and management.
 *
 * <p>This utility class provides factory methods for creating consistently
 * styled form fields throughout the application. It eliminates duplication
 * of form setup patterns and ensures uniform appearance and behavior.</p>
 *
 * <p>The class offers:</p>
 * <ul>
 *   <li>Required and optional text field creation</li>
 *   <li>Text area creation with consistent styling</li>
 *   <li>Placeholder text and validation indicators</li>
 *   <li>Standardized form field configuration</li>
 * </ul>
 *
 * <p>All form fields created through this utility automatically include
 * appropriate styling, placeholders, and validation settings for consistent
 * user experience across the application.</p>
 */
public final class FormHelper {

    private FormHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a required text field with the specified label and placeholder.
     *
     * <p>Creates a text field that is marked as required with a visual
     * indicator and includes placeholder text for user guidance. The field
     * is configured with clear button functionality for easy input clearing.</p>
     *
     * @param label the label text to display above the field
     * @param placeholder the placeholder text to show when the field is empty
     * @return a configured TextField marked as required with placeholder
     */
    public static TextField createRequiredTextField(String label, String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setRequired(true);
        field.setClearButtonVisible(true);

        return field;
    }

    /**
     * Creates an optional text field with the specified label and placeholder.
     *
     * <p>Creates a text field that is not required, allowing users to
     * leave it empty. The field includes placeholder text for guidance
     * and clear button functionality for user convenience.</p>
     *
     * @param label the label text to display above the field
     * @param placeholder the placeholder text to show when the field is empty
     * @return a configured TextField marked as optional with placeholder
     */
    public static TextField createOptionalTextField(String label, String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setClearButtonVisible(true);

        return field;
    }

    /**
     * Creates a text area with the specified label and placeholder.
     *
     * <p>Creates a multi-line text area suitable for longer text input.
     * The area includes placeholder text for guidance and is styled
     * with consistent sizing and appearance.</p>
     *
     * @param label the label text to display above the text area
     * @param placeholder the placeholder text to show when the area is empty
     * @return a configured TextArea with placeholder and consistent styling
     */
    public static TextArea createTextArea(String label, String placeholder) {
        TextArea area = new TextArea(label);
        area.setPlaceholder(placeholder);
        area.setClearButtonVisible(true);
        area.addClassName("text-area--sm");

        return area;
    }
}
