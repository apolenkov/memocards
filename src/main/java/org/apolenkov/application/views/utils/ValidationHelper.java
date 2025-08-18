package org.apolenkov.application.views.utils;

import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import java.util.function.Function;

/**
 * Utility class for centralized validation logic.
 * Eliminates duplication of validation patterns across forms and dialogs.
 */
public final class ValidationHelper {

    private ValidationHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a required field validator
     */
    public static <T> Validator<T> createRequiredValidator(String fieldName) {
        return (value, context) -> {
            if (value == null
                    || (value instanceof String && ((String) value).trim().isEmpty())) {
                return ValidationResult.error(fieldName + " is required");
            }
            return ValidationResult.ok();
        };
    }

    /**
     * Create an email validator
     */
    public static Validator<String> createEmailValidator() {
        return (email, context) -> {
            if (email == null || email.trim().isEmpty()) {
                return ValidationResult.error("Email is required");
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ValidationResult.error("Invalid email format");
            }
            return ValidationResult.ok();
        };
    }

    /**
     * Create a password strength validator
     */
    public static Validator<String> createPasswordValidator() {
        return (password, context) -> {
            if (password == null || password.length() < 8) {
                return ValidationResult.error("Password must be at least 8 characters long");
            }
            if (!password.matches(".*[a-zA-Z].*")) {
                return ValidationResult.error("Password must contain letters");
            }
            if (!password.matches(".*\\d.*")) {
                return ValidationResult.error("Password must contain numbers");
            }
            return ValidationResult.ok();
        };
    }

    /**
     * Create a length validator
     */
    public static Validator<String> createLengthValidator(int maxLength, String fieldName) {
        return (value, context) -> {
            if (value != null && value.length() > maxLength) {
                return ValidationResult.error(fieldName + " must be no more than " + maxLength + " characters");
            }
            return ValidationResult.ok();
        };
    }

    /**
     * Create a range validator for numbers
     */
    public static <T extends Number & Comparable<T>> Validator<T> createRangeValidator(T min, T max, String fieldName) {
        return (value, context) -> {
            if (value == null) {
                return ValidationResult.error(fieldName + " is required");
            }
            if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
                return ValidationResult.error(fieldName + " must be between " + min + " and " + max);
            }
            return ValidationResult.ok();
        };
    }

    /**
     * Validate and write bean with error handling
     */
    public static <T> boolean validateAndWriteBean(BeanValidationBinder<T> binder, T bean) {
        try {
            binder.writeBean(bean);
            return true;
        } catch (ValidationException e) {
            NotificationHelper.showValidationError();
            return false;
        }
    }

    /**
     * Validate and write bean with custom error handling
     */
    public static <T> boolean validateAndWriteBean(
            BeanValidationBinder<T> binder, T bean, Function<ValidationException, String> errorMessageProvider) {
        try {
            binder.writeBean(bean);
            return true;
        } catch (ValidationException e) {
            String errorMessage = errorMessageProvider.apply(e);
            NotificationHelper.showError(errorMessage);
            return false;
        }
    }
}
