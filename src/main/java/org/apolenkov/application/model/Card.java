package org.apolenkov.application.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Learning tool with front and back content.
 */
public final class Card {
    private Long id;

    @NotNull
    private long deckId;

    @NotBlank
    @Size(max = 300)
    private String frontText;

    @NotBlank
    @Size(max = 300)
    private String backText;

    @Size(max = 500)
    private String example;

    @Size(max = 2048)
    private String imageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Creates card with current timestamps.
     */
    public Card() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Creates card with basic fields.
     *
     * @param idValue unique identifier for the card
     * @param deckIdValue ID of the deck this card belongs to
     * @param frontTextValue text displayed on the front of the card
     * @param backTextValue text displayed on the back of the card
     * @throws IllegalArgumentException if deckId is null, or if frontText/backText are null or empty
     */
    public Card(final Long idValue, final long deckIdValue, final String frontTextValue, final String backTextValue) {
        this();
        this.id = idValue;
        setDeckId(deckIdValue);
        setFrontText(frontTextValue);
        setBackText(backTextValue);
    }

    /**
     * Creates card with basic fields and example.
     *
     * @param idValue unique identifier for the card
     * @param deckIdValue ID of the deck this card belongs to
     * @param frontTextValue text displayed on the front of the card
     * @param backTextValue text displayed on the back of the card
     * @param exampleValue additional example or context for the card
     * @throws IllegalArgumentException if deckId is null, or if frontText/backText are null or empty
     */
    public Card(
            final Long idValue,
            final long deckIdValue,
            final String frontTextValue,
            final String backTextValue,
            final String exampleValue) {
        this(idValue, deckIdValue, frontTextValue, backTextValue);
        this.example = exampleValue;
    }

    /**
     * Returns unique card identifier.
     *
     * @return card ID if persisted, null for new instances
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets card identifier.
     *
     * @param idValue card ID to set (null for new instances)
     */
    public void setId(final Long idValue) {
        this.id = idValue;
    }

    /**
     * Returns identifier of the deck this card belongs to.
     *
     * @return deck ID
     */
    public long getDeckId() {
        return deckId;
    }

    /**
     * Sets identifier of the parent deck.
     *
     * @param deckIdValue deck ID to set
     * @throws IllegalArgumentException if deckId is not positive
     */
    public void setDeckId(final long deckIdValue) {
        if (deckIdValue <= 0) {
            throw new IllegalArgumentException("deckId must be positive");
        }
        this.deckId = deckIdValue;
    }

    /**
     * Returns front-side text content of the card.
     *
     * @return front text content
     */
    public String getFrontText() {
        return frontText;
    }

    /**
     * Sets front-side text with validation and trimming.
     *
     * @param frontTextValue text to display on the front (will be trimmed)
     * @throws IllegalArgumentException if frontText is null or empty
     */
    public void setFrontText(final String frontTextValue) {
        String t = frontTextValue != null ? frontTextValue.trim() : null;
        if (t == null || t.isEmpty()) {
            throw new IllegalArgumentException("frontText is required");
        }
        this.frontText = t;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Returns back-side text content of the card.
     *
     * @return back text content
     */
    public String getBackText() {
        return backText;
    }

    /**
     * Sets back-side text with validation and trimming.
     *
     * @param backTextValue text to display on the back (will be trimmed)
     * @throws IllegalArgumentException if backText is null or empty
     */
    public void setBackText(final String backTextValue) {
        String t = backTextValue != null ? backTextValue.trim() : null;
        if (t == null || t.isEmpty()) {
            throw new IllegalArgumentException("backText is required");
        }
        this.backText = t;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Returns optional example text.
     *
     * @return example text, or null if not set
     */
    public String getExample() {
        return example;
    }

    /**
     * Sets example text.
     *
     * @param exampleValue example text to set
     */
    public void setExample(final String exampleValue) {
        this.example = exampleValue != null ? exampleValue.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Returns optional image URL.
     *
     * @return image URL, or null if not set
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets image URL.
     *
     * @param imageUrlValue image URL to set
     */
    public void setImageUrl(final String imageUrlValue) {
        this.imageUrl = imageUrlValue != null ? imageUrlValue.trim() : null;
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
     * Compares cards for equality based on ID.
     *
     * @param o object to compare with
     * @return true if objects are equal
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Card card = (Card) o;
        return Objects.equals(id, card.id);
    }

    /**
     * Returns hash code based on ID field.
     *
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns string representation for debugging.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "Card{"
                + "id="
                + id
                + ", deckId="
                + deckId
                + ", frontText='"
                + frontText
                + '\''
                + ", backText='"
                + backText
                + '\''
                + ", example='"
                + example
                + '\''
                + ", imageUrl='"
                + imageUrl
                + '\''
                + ", createdAt="
                + createdAt
                + ", updatedAt="
                + updatedAt
                + '}';
    }
}
