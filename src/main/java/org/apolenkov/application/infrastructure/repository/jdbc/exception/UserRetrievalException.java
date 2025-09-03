package org.apolenkov.application.infrastructure.repository.jdbc.exception;

/**
 * Exception thrown when user retrieval operations fail.
 */
public class UserRetrievalException extends RuntimeException {

    /**
     * Constructs a new user retrieval exception with the specified detail message.
     *
     * @param message the detail message
     */
    public UserRetrievalException(final String message) {
        super(message);
    }

    /**
     * Constructs a new user retrieval exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public UserRetrievalException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
