package org.apolenkov.application.infrastructure.repository.jdbc.exception;

/**
 * Exception thrown when deck retrieval operations fail.
 */
public class DeckRetrievalException extends RuntimeException {

    /**
     * Constructs a new deck retrieval exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public DeckRetrievalException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
