package org.apolenkov.application.infrastructure.repository.jdbc.exception;

/**
 * Exception thrown when deck persistence operations fail.
 */
public class DeckPersistenceException extends RuntimeException {

    /**
     * Constructs a new deck persistence exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public DeckPersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
