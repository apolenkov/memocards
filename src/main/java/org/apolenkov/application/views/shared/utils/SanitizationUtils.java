package org.apolenkov.application.views.shared.utils;

import org.apolenkov.application.views.core.constants.CoreConstants;

/**
 * Utility class for sanitizing user input and error details.
 * Provides methods to prevent XSS attacks and information leakage.
 */
public final class SanitizationUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private SanitizationUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Sanitizes error details for safe display.
     *
     * @param errorDetail the error detail to sanitize
     * @param unknownText the text to return for null/empty inputs
     * @return sanitized error detail suitable for display
     */
    public static String sanitizeErrorDetail(final String errorDetail, final String unknownText) {
        if (errorDetail == null || errorDetail.isEmpty()) {
            return unknownText;
        }

        // Remove potentially dangerous characters
        String sanitized =
                errorDetail.replaceAll("[<>\"'&]", "").replaceAll("\\s+", " ").trim();

        // Limit length to prevent information leakage
        if (sanitized.length() > CoreConstants.MAX_ERROR_DETAIL_LENGTH) {
            sanitized = sanitized.substring(0, CoreConstants.MAX_ERROR_DETAIL_LENGTH) + CoreConstants.TRUNCATION_SUFFIX;
        }

        return sanitized.isEmpty() ? unknownText : sanitized;
    }
}
