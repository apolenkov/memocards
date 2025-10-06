package org.apolenkov.application.views.shared.utils;

/**
 * Utility class for text formatting operations.
 * Provides methods for formatting text content in UI components.
 */
public final class TextFormattingUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private TextFormattingUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Formats example text, returning "-" if the text is null or empty.
     *
     * @param exampleText the example text to format
     * @return formatted text or "-" if empty
     */
    public static String formatPlaceholder(final String exampleText) {
        return exampleText != null && !exampleText.trim().isEmpty() ? exampleText : "-";
    }
}
