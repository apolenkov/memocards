package org.apolenkov.application.infrastructure.repository.jdbc.exception;

/**
 * Exception thrown when news persistence operations fail.
 */
public class NewsPersistenceException extends RuntimeException {

    /**
     * Constructs a new news persistence exception with the specified detail message.
     *
     * @param message the detail message
     */
    public NewsPersistenceException(final String message) {
        super(message);
    }

    /**
     * Constructs a new news persistence exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public NewsPersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
