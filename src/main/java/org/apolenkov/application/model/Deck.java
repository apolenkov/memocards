package org.apolenkov.application.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Collection of cards belonging to a user.
 */
public final class Deck {
    private Long id;

    @NotNull
    private long userId;

    @NotBlank
    @Size(max = 120)
    private String title;

    @Size(max = 500)
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Card> cards;

    /**
     * Creates empty deck with current timestamps.
     */
    public Deck() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.cards = new ArrayList<>();
    }

    /**
     * Creates new deck with specified fields.
     *
     * @param idValue unique identifier for the deck
     * @param userIdValue ID of the user who owns this deck
     * @param titleValue title of the deck
     * @param descriptionValue description of the deck
     */
    public Deck(final Long idValue, final long userIdValue, final String titleValue, final String descriptionValue) {
        this();
        this.id = idValue;
        setUserId(userIdValue);
        setTitle(titleValue);
        setDescription(descriptionValue);
    }

    /**
     * Creates new deck with validation.
     *
     * @param userId ID of the user who will own this deck
     * @param title title of the deck (required)
     * @param description optional description of the deck
     * @return new Deck instance
     * @throws IllegalArgumentException if userId is null or title is empty
     */
    public static Deck create(final long userId, final String title, final String description) {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
        String t = title != null ? title.trim() : null;
        if (t == null || t.isEmpty()) {
            throw new IllegalArgumentException("title is required");
        }
        Deck d = new Deck();
        d.setUserId(userId);
        d.setTitle(t);
        d.setDescription(description != null ? description.trim() : null);
        return d;
    }

    /**
     * Returns number of cards in deck.
     *
     * @return number of cards
     */
    public int getCardCount() {
        return cards != null ? cards.size() : 0;
    }

    /**
     * Adds card to deck.
     *
     * @param card card to add
     * @throws IllegalArgumentException if card is null
     */
    public void addCard(final Card card) {
        if (cards == null) {
            cards = new ArrayList<>();
        }
        if (card == null) {
            throw new IllegalArgumentException("card is null");
        }
        cards.add(card);
        card.setDeckId(this.id);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Removes card from deck.
     *
     * @param card card to remove
     */
    public void removeCard(final Card card) {
        if (cards != null) {
            cards.remove(card);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Returns deck identifier.
     *
     * @return deck ID, or null if not persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets deck identifier.
     *
     * @param idValue deck ID to set
     */
    public void setId(final Long idValue) {
        this.id = idValue;
    }

    /**
     * Returns user identifier.
     *
     * @return user ID
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Sets user identifier.
     *
     * @param userIdValue user ID to set
     * @throws IllegalArgumentException if userId is null
     */
    public void setUserId(final long userIdValue) {
        if (userIdValue <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
        this.userId = userIdValue;
    }

    /**
     * Returns deck title.
     *
     * @return deck title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets deck title.
     *
     * @param titleValue deck title to set
     * @throws IllegalArgumentException if title is null or empty
     */
    public void setTitle(final String titleValue) {
        String t = titleValue != null ? titleValue.trim() : null;
        if (t == null || t.isEmpty()) {
            throw new IllegalArgumentException("title is required");
        }
        this.title = t;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Returns deck description.
     *
     * @return deck description, or null if not set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description of this deck.
     *
     * <p>Trims description if provided and updates modification timestamp.
     * Description is optional and can be null.</p>
     *
     * @param descriptionValue deck description to set
     */
    public void setDescription(final String descriptionValue) {
        this.description = descriptionValue != null ? descriptionValue.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Returns creation timestamp.
     *
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets creation timestamp.
     *
     * @param createdAtValue creation timestamp to set
     */
    public void setCreatedAt(final LocalDateTime createdAtValue) {
        this.createdAt = createdAtValue;
    }

    /**
     * Returns last modification timestamp.
     *
     * @return last modification timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets modification timestamp.
     *
     * @param updatedAtValue modification timestamp to set
     */
    public void setUpdatedAt(final LocalDateTime updatedAtValue) {
        this.updatedAt = updatedAtValue;
    }

    /**
     * Returns unmodifiable list of cards.
     *
     * @return unmodifiable list of cards, or empty list if none exist
     */
    public List<Card> getCards() {
        return cards == null ? List.of() : Collections.unmodifiableList(cards);
    }

    /**
     * Sets list of cards.
     *
     * @param cardsList list of cards to set, or null for empty list
     */
    public void setCards(final List<Card> cardsList) {
        this.cards = new ArrayList<>(cardsList != null ? cardsList : List.of());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Deck deck = (Deck) o;
        return Objects.equals(id, deck.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Deck{"
                + "id="
                + id
                + ", userId="
                + userId
                + ", title='"
                + title
                + '\''
                + ", description='"
                + description
                + '\''
                + ", cardCount="
                + getCardCount()
                + ", createdAt="
                + createdAt
                + ", updatedAt="
                + updatedAt
                + '}';
    }
}
