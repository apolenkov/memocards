package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity representing known cards that users have learned.
 *
 * <p>Tracks which cards in a deck the user has already mastered.
 * Used for progress tracking and practice adaptation.</p>
 *
 * <p>Table: "known_cards" with composite relationship (deck_id, card_id).</p>
 */
@Entity
@Table(name = "known_cards")
public class KnownCardEntity {

    /**
     * Primary key, auto-generated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Deck identifier. References an existing deck.
     */
    @Column(nullable = false)
    private Long deckId;

    /**
     * Card identifier. References the learned flashcard.
     */
    @Column(nullable = false)
    private Long cardId;

    /**
     * Gets the primary key identifier.
     *
     * @return the unique identifier for this known card record
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the primary key identifier.
     *
     * @param idValue the unique identifier for this known card record
     */
    public void setId(final Long idValue) {
        this.id = idValue;
    }

    /**
     * Gets the deck identifier.
     *
     * @return the deck ID this card belongs to
     */
    public Long getDeckId() {
        return deckId;
    }

    /**
     * Sets the deck identifier.
     *
     * <p>Establishes the relationship between this known card record
     * and a specific deck. The deck must exist in the system before
     * this relationship can be established.</p>
     *
     * @param deckIdValue deck identifier, must not be null
     * @throws IllegalArgumentException if deckId is null
     */
    public void setDeckId(final Long deckIdValue) {
        if (deckIdValue == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        this.deckId = deckIdValue;
    }

    /**
     * Gets the card identifier.
     *
     * @return the flashcard ID that has been learned
     */
    @SuppressWarnings("unused") // IDE Community problem
    public Long getCardId() {
        return cardId;
    }

    /**
     * Sets the card identifier.
     *
     * <p>Establishes the relationship between this known card record
     * and a specific flashcard. The card must exist in the system
     * before this relationship can be established.</p>
     *
     * @param cardIdValue card identifier, must not be null
     * @throws IllegalArgumentException if cardId is null
     */
    public void setCardId(final Long cardIdValue) {
        if (cardIdValue == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }
        this.cardId = cardIdValue;
    }
}
