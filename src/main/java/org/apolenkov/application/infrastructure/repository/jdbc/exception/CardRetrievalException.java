package org.apolenkov.application.infrastructure.repository.jdbc.exception;

/**
 * Exception thrown when card retrieval operations fail.
 */
public class CardRetrievalException extends RuntimeException {

    /**
     * Constructs a new card retrieval exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public CardRetrievalException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
