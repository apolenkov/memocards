package org.apolenkov.application.domain.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when a deck is modified (created, updated, or deleted).
 * Used to notify UI caches to invalidate stale data.
 */
public final class DeckModifiedEvent extends ApplicationEvent {

    private final Long userId;
    private final Long deckId;
    private final ModificationType type;

    /**
     * Creates DeckModifiedEvent.
     *
     * @param source the source of the event
     * @param userIdValue the user who owns the deck
     * @param deckIdValue the deck ID
     * @param typeValue the type of modification
     */
    public DeckModifiedEvent(
            final Object source, final Long userIdValue, final Long deckIdValue, final ModificationType typeValue) {
        super(source);
        this.userId = userIdValue;
        this.deckId = deckIdValue;
        this.type = typeValue;
    }

    /**
     * Gets the user ID who owns the deck.
     *
     * @return user ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Gets the deck ID.
     *
     * @return deck ID
     */
    public Long getDeckId() {
        return deckId;
    }

    /**
     * Gets the modification type.
     *
     * @return modification type
     */
    public ModificationType getType() {
        return type;
    }

    /**
     * Type of deck modification.
     */
    public enum ModificationType {
        CREATED,
        UPDATED,
        DELETED
    }
}
