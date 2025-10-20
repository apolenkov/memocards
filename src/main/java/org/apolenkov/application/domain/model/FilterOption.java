package org.apolenkov.application.domain.model;

/**
 * Filter option for flashcard filtering by known/unknown status.
 * Used across domain, service, and UI layers for consistent filtering logic.
 */
public enum FilterOption {
    /**
     * Show all flashcards regardless of known status.
     */
    ALL,

    /**
     * Show only flashcards marked as known by user.
     */
    KNOWN_ONLY,

    /**
     * Show only flashcards not yet marked as known by user.
     */
    UNKNOWN_ONLY
}
