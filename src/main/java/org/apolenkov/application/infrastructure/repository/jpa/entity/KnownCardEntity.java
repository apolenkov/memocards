package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.*;

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

    public void setId(Long id) {
        this.id = id;
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
     * @param deckId deck identifier, must not be null
     */
    public void setDeckId(Long deckId) {
        if (deckId == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        this.deckId = deckId;
    }

    /**
     * Gets the card identifier.
     *
     * @return the flashcard ID that has been learned
     */
    @SuppressWarnings("unused")
    public Long getCardId() {
        return cardId;
    }

    /**
     * @param cardId card identifier, must not be null
     */
    public void setCardId(Long cardId) {
        if (cardId == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }
        this.cardId = cardId;
    }
}
