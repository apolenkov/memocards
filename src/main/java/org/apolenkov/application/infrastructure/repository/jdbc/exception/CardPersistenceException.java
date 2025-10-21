package org.apolenkov.application.infrastructure.repository.jdbc.exception;

/**
 * Exception thrown when card persistence operations fail.
 */
public class CardPersistenceException extends RuntimeException {

    /**
     * Constructs a new card persistence exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public CardPersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
