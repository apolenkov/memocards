package org.apolenkov.application.views.shared.utils;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValidation;
import java.util.function.UnaryOperator;

/**
 * Utility class for centralized form validation operations.
 * Provides reusable methods for field validation, trimming, and error handling
 * to eliminate code duplication across view components.
 */
public final class ValidationHelper {

    private ValidationHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Safely trims a string value, converting null and empty to null.
     * Useful for nullable database fields where empty strings should be stored as null.
     *
     * @param value the string value to trim
     * @return trimmed string, or null if input was null or became empty after trimming
     */
    public static String safeTrim(final String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Safely trims a string value, converting null to empty string.
     * Useful for required fields where empty string is preferred over null.
     *
     * @param value the string value to trim
     * @return trimmed string or empty string if null
     */
    public static String safeTrimToEmpty(final String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Gets trimmed value from a field, handling null values.
     *
     * @param field the field to get value from
     * @return trimmed value or empty string if null
     */
    public static String getTrimmedValue(final AbstractField<?, String> field) {
        return field.getValue() == null ? "" : field.getValue().trim();
    }

    /**
     * Validates that a field has required value (not null and not blank).
     * Sets field as invalid with translated error message if validation fails.
     *
     * @param field the field to validate
     * @param value the trimmed value to check
     * @param errorKey the translation key for error message
     * @param translator function to translate error key
     * @return true if validation failed (field is invalid), false if valid
     */
    public static boolean validateRequired(
            final HasValidation field,
            final String value,
            final String errorKey,
            final UnaryOperator<String> translator) {
        if (value == null || value.isBlank()) {
            field.setErrorMessage(translator.apply(errorKey));
            field.setInvalid(true);
            return true; // validation failed
        }
        field.setInvalid(false);
        return false; // validation passed
    }

    /**
     * Sets error message and invalid state on a field.
     *
     * @param field the field to mark as invalid
     * @param errorKey the translation key for error message
     * @param translator function to translate error key
     */
    public static void setFieldError(
            final HasValidation field, final String errorKey, final UnaryOperator<String> translator) {
        field.setErrorMessage(translator.apply(errorKey));
        field.setInvalid(true);
    }

    /**
     * Clears validation errors from multiple fields.
     *
     * @param fields the fields to clear errors from
     */
    public static void clearValidationErrors(final HasValidation... fields) {
        for (HasValidation field : fields) {
            field.setInvalid(false);
        }
    }

    /**
     * Validates that a string value is not null and not empty after trimming.
     *
     * @param value the value to check
     * @return true if value is valid (not null, not blank)
     */
    public static boolean isNotBlank(final String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates field value and sets error if empty.
     * Simplified version without translation function - for components that have direct getTranslation() access.
     *
     * @param field the field to validate
     * @param value the value to check
     * @param errorMessage the error message to display
     * @return true if validation failed, false if passed
     */
    public static boolean validateRequiredSimple(
            final HasValidation field, final String value, final String errorMessage) {
        if (value == null || value.isBlank()) {
            field.setErrorMessage(errorMessage);
            field.setInvalid(true);
            return true;
        }
        field.setInvalid(false);
        return false;
    }
}
