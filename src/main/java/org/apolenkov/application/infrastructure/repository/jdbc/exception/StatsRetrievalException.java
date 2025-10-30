package org.apolenkov.application.infrastructure.repository.jdbc.exception;

/**
 * Exception thrown when statistics retrieval operations fail.
 */
public class StatsRetrievalException extends RuntimeException {

    /**
     * Constructs a new statistics retrieval exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public StatsRetrievalException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
