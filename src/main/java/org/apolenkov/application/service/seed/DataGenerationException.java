package org.apolenkov.application.service.seed;

/**
 * Exception thrown when test data generation fails.
 */
public class DataGenerationException extends RuntimeException {

    /**
     * Constructs exception with message and cause.
     *
     * @param message error description
     * @param cause underlying exception
     */
    public DataGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
