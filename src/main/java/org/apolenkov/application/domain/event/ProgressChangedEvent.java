package org.apolenkov.application.domain.event;

import org.springframework.context.ApplicationEvent;

/**
 * Domain event published when flashcard progress changes.
 * Signals that a card's known/unknown status has been modified.
 *
 * <p>This event enables event-driven cache invalidation following Clean Architecture principles.
 * Infrastructure components (caches) listen to domain events instead of being directly called by services.
 *
 * <p>Event is published by:
 * <ul>
 *   <li>StatsService when setCardKnown() is called</li>
 *   <li>StatsService when resetDeckProgress() is called</li>
 * </ul>
 *
 * <p>Event is consumed by:
 * <ul>
 *   <li>KnownCardsCache - invalidates cached known card IDs</li>
 *   <li>Future subscribers - analytics, notifications, etc.</li>
 * </ul>
 */
public final class ProgressChangedEvent extends ApplicationEvent {

    private final long deckId;
    private final ChangeType changeType;

    /**
     * Type of progress change.
     */
    public enum ChangeType {
        /** Single card status changed (known/unknown toggle). */
        CARD_STATUS_CHANGED,

        /** All progress reset for entire deck. */
        DECK_RESET
    }

    /**
     * Creates event for single card status change.
     *
     * @param source the object on which the event initially occurred
     * @param deckIdValue deck identifier
     */
    public ProgressChangedEvent(final Object source, final long deckIdValue) {
        super(source);
        this.deckId = deckIdValue;
        this.changeType = ChangeType.CARD_STATUS_CHANGED;
    }

    /**
     * Creates event for deck reset (all progress cleared).
     *
     * @param source the object on which the event initially occurred
     * @param deckIdValue deck identifier
     * @param changeTypeValue type of progress change
     */
    public ProgressChangedEvent(final Object source, final long deckIdValue, final ChangeType changeTypeValue) {
        super(source);
        this.deckId = deckIdValue;
        this.changeType = changeTypeValue;
    }

    /**
     * Gets deck identifier.
     *
     * @return deck ID
     */
    public long getDeckId() {
        return deckId;
    }

    /**
     * Gets change type.
     *
     * @return type of progress change
     */
    public ChangeType getChangeType() {
        return changeType;
    }

    @Override
    public String toString() {
        return "ProgressChangedEvent{deckId=" + deckId + ", type=" + changeType + "}";
    }
}
