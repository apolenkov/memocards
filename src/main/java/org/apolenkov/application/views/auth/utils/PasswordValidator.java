package org.apolenkov.application.views.auth.utils;

import org.apolenkov.application.domain.constants.BusinessConstants;

/**
 * Utility class for password validation operations.
 * Provides centralized password validation logic following security requirements.
 */
public final class PasswordValidator {

    private PasswordValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Checks if password does not meet security requirements.
     * Password must be at least {@value org.apolenkov.application.domain.constants.BusinessConstants#MIN_PASSWORD_LENGTH}
     * characters and contain both letters and digits.
     *
     * @param password the password to validate
     * @return true if password is invalid, false if valid
     */
    public static boolean isInvalid(final String password) {
        if (password == null || password.length() < BusinessConstants.MIN_PASSWORD_LENGTH) {
            return true;
        }

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (final char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }

            if (hasLetter && hasDigit) {
                break;
            }
        }

        return !(hasLetter && hasDigit);
    }

    /**
     * Validates password and throws exception if invalid.
     * Used for backend validation with security requirements.
     *
     * @param password the password to validate
     * @throws IllegalArgumentException if password does not meet security requirements
     */
    public static void validateOrThrow(final String password) {
        if (isInvalid(password)) {
            throw new IllegalArgumentException("Password must be at least " + BusinessConstants.MIN_PASSWORD_LENGTH
                    + " characters and contain letters and digits");
        }
    }
}
