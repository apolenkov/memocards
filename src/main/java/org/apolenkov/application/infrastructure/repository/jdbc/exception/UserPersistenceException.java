package org.apolenkov.application.infrastructure.repository.jdbc.exception;

/**
 * Exception thrown when user persistence operations fail.
 */
public class UserPersistenceException extends RuntimeException {

    /**
     * Constructs a new user persistence exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public UserPersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
