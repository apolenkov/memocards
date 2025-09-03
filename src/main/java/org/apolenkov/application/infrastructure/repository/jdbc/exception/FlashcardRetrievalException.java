package org.apolenkov.application.infrastructure.repository.jdbc.exception;

/**
 * Exception thrown when flashcard retrieval operations fail.
 */
public class FlashcardRetrievalException extends RuntimeException {

    /**
     * Constructs a new flashcard retrieval exception with the specified detail message.
     *
     * @param message the detail message
     */
    public FlashcardRetrievalException(final String message) {
        super(message);
    }

    /**
     * Constructs a new flashcard retrieval exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public FlashcardRetrievalException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
