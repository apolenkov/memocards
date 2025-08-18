package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import java.util.function.Function;

/**
 * Utility class for centralized form creation and management.
 * Eliminates duplication of form setup patterns across the application.
 */
public final class FormHelper {

    private FormHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a basic form layout
     */
    public static FormLayout createFormLayout() {
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 3));
        form.addClassName("enhanced-form");
        return form;
    }

    /**
     * Create a required text field
     */
    public static TextField createRequiredTextField(String label, String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setRequired(true);
        field.setClearButtonVisible(true);
        field.addClassName("form-field");
        return field;
    }

    /**
     * Create an optional text field
     */
    public static TextField createOptionalTextField(String label, String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setClearButtonVisible(true);
        field.addClassName("form-field");
        return field;
    }

    /**
     * Create a text area
     */
    public static TextArea createTextArea(String label, String placeholder) {
        TextArea area = new TextArea(label);
        area.setPlaceholder(placeholder);
        area.setClearButtonVisible(true);
        area.setMaxHeight("100px");
        area.addClassName("form-field");
        return area;
    }

    /**
     * Create a select field
     */
    public static <T> Select<T> createSelect(String label, T... items) {
        Select<T> select = new Select<>();
        select.setLabel(label);
        select.setItems(items);
        select.addClassName("form-field");
        return select;
    }

    /**
     * Create a checkbox
     */
    public static Checkbox createCheckbox(String label) {
        Checkbox checkbox = new Checkbox(label);
        checkbox.addClassName("form-field");
        return checkbox;
    }

    /**
     * Create a date picker
     */
    public static DatePicker createDatePicker(String label) {
        DatePicker picker = new DatePicker(label);
        picker.addClassName("form-field");
        return picker;
    }

    /**
     * Create a time picker
     */
    public static TimePicker createTimePicker(String label) {
        TimePicker picker = new TimePicker(label);
        picker.addClassName("form-field");
        return picker;
    }

    /**
     * Create a form field with custom configuration
     */
    public static TextField createCustomTextField(
            String label, String placeholder, boolean required, boolean clearButton) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setRequired(required);
        field.setClearButtonVisible(clearButton);
        field.addClassName("form-field");
        return field;
    }

    /**
     * Create a form field with validation
     */
    public static TextField createValidatedTextField(
            String label, String placeholder, Function<String, Boolean> validator) {
        TextField field = createRequiredTextField(label, placeholder);
        field.addValueChangeListener(e -> {
            String value = e.getValue();
            if (value != null && !validator.apply(value)) {
                field.setInvalid(true);
                field.setErrorMessage("Invalid value");
            } else {
                field.setInvalid(false);
            }
        });
        return field;
    }

    /**
     * Create a form field with length validation
     */
    public static TextField createLengthValidatedTextField(String label, String placeholder, int maxLength) {
        return createValidatedTextField(label, placeholder, value -> value == null || value.length() <= maxLength);
    }

    /**
     * Create a form field with email validation
     */
    public static TextField createEmailField(String label, String placeholder) {
        return createValidatedTextField(
                label, placeholder, value -> value == null || value.matches("^[A-Za-z0-9+_.-]+@(.+)$"));
    }

    /**
     * Create a form field with password validation
     */
    public static TextField createPasswordField(String label, String placeholder) {
        TextField field = createRequiredTextField(label, placeholder);
        // Password type not available in this version
        field.addValueChangeListener(e -> {
            String value = e.getValue();
            if (value != null && value.length() < 8) {
                field.setInvalid(true);
                field.setErrorMessage("Password must be at least 8 characters");
            } else {
                field.setInvalid(false);
            }
        });
        return field;
    }

    /**
     * Create a form field with number validation
     */
    public static TextField createNumberField(String label, String placeholder) {
        TextField field = createRequiredTextField(label, placeholder);
        // Number type not available in this version
        return field;
    }

    /**
     * Create a form field with URL validation
     */
    public static TextField createUrlField(String label, String placeholder) {
        return createValidatedTextField(label, placeholder, value -> value == null || value.matches("^https?://.*"));
    }

    /**
     * Create a form field with phone validation
     */
    public static TextField createPhoneField(String label, String placeholder) {
        return createValidatedTextField(
                label, placeholder, value -> value == null || value.matches("^[+]?[0-9\\s\\-()]+$"));
    }

    /**
     * Create a form field with postal code validation
     */
    public static TextField createPostalCodeField(String label, String placeholder) {
        return createValidatedTextField(
                label, placeholder, value -> value == null || value.matches("^[0-9A-Za-z\\s-]+$"));
    }

    /**
     * Create a form field with credit card validation
     */
    public static TextField createCreditCardField(String label, String placeholder) {
        return createValidatedTextField(
                label,
                placeholder,
                value -> value == null || value.replaceAll("\\s", "").matches("^[0-9]{13,19}$"));
    }

    /**
     * Create a form field with IP address validation
     */
    public static TextField createIpAddressField(String label, String placeholder) {
        return createValidatedTextField(
                label,
                placeholder,
                value -> value == null
                        || value.matches(
                                "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"));
    }

    /**
     * Create a form field with MAC address validation
     */
    public static TextField createMacAddressField(String label, String placeholder) {
        return createValidatedTextField(
                label,
                placeholder,
                value -> value == null || value.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"));
    }

    /**
     * Create a form field with hex color validation
     */
    public static TextField createHexColorField(String label, String placeholder) {
        return createValidatedTextField(
                label, placeholder, value -> value == null || value.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"));
    }

    /**
     * Create a form field with time validation
     */
    public static TextField createTimeField(String label, String placeholder) {
        return createValidatedTextField(
                label, placeholder, value -> value == null || value.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"));
    }

    /**
     * Create a form field with date validation
     */
    public static TextField createDateField(String label, String placeholder) {
        return createValidatedTextField(
                label, placeholder, value -> value == null || value.matches("^\\d{4}-\\d{2}-\\d{2}$"));
    }

    /**
     * Create a form field with currency validation
     */
    public static TextField createCurrencyField(String label, String placeholder) {
        return createValidatedTextField(
                label, placeholder, value -> value == null || value.matches("^\\$?\\d+(\\.\\d{2})?$"));
    }

    /**
     * Create a form field with percentage validation
     */
    public static TextField createPercentageField(String label, String placeholder) {
        return createValidatedTextField(
                label, placeholder, value -> value == null || value.matches("^\\d+(\\.\\d+)?%?$"));
    }

    /**
     * Create a form field with integer validation
     */
    public static TextField createIntegerField(String label, String placeholder) {
        return createValidatedTextField(label, placeholder, value -> value == null || value.matches("^-?\\d+$"));
    }

    /**
     * Create a form field with positive integer validation
     */
    public static TextField createPositiveIntegerField(String label, String placeholder) {
        return createValidatedTextField(label, placeholder, value -> value == null || value.matches("^\\d+$"));
    }

    /**
     * Create a form field with decimal validation
     */
    public static TextField createDecimalField(String label, String placeholder) {
        return createValidatedTextField(
                label, placeholder, value -> value == null || value.matches("^-?\\d+(\\.\\d+)?$"));
    }

    /**
     * Create a form field with positive decimal validation
     */
    public static TextField createPositiveDecimalField(String label, String placeholder) {
        return createValidatedTextField(
                label, placeholder, value -> value == null || value.matches("^\\d+(\\.\\d+)?$"));
    }
}
