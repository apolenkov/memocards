package org.apolenkov.application.views.deck.components.grid;

/**
 * Filter options for flashcard visibility.
 */
public enum FilterOption {
    /** Show all cards. */
    ALL,

    /** Show only known cards. */
    KNOWN_ONLY,

    /** Show only unknown cards (hide known). */
    UNKNOWN_ONLY
}
