package org.apolenkov.application.infrastructure.repository.jdbc.exception;

/**
 * Exception thrown when flashcard persistence operations fail.
 */
public class FlashcardPersistenceException extends RuntimeException {

    /**
     * Constructs a new flashcard persistence exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public FlashcardPersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
