package org.apolenkov.application.infrastructure.repository.jdbc.exception;

/**
 * Exception thrown when news retrieval operations fail.
 */
public class NewsRetrievalException extends RuntimeException {

    /**
     * Constructs a new news retrieval exception with the specified detail message.
     *
     * @param message the detail message
     */
    public NewsRetrievalException(final String message) {
        super(message);
    }

    /**
     * Constructs a new news retrieval exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public NewsRetrievalException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
