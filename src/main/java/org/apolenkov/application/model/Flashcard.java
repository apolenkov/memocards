package org.apolenkov.application.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Learning tool with front and back content.
 */
public final class Flashcard {
    private Long id;

    @NotNull
    private Long deckId;

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
     * Creates flashcard with current timestamps.
     */
    public Flashcard() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Creates flashcard with basic fields.
     *
     * @param id unique identifier for the flashcard
     * @param deckId ID of the deck this flashcard belongs to
     * @param frontText text displayed on the front of the card
     * @param backText text displayed on the back of the card
     * @throws IllegalArgumentException if deckId is null, or if frontText/backText are null or empty
     */
    public Flashcard(final Long id, final Long deckId, final String frontText, final String backText) {
        this();
        this.id = id;
        setDeckId(deckId);
        setFrontText(frontText);
        setBackText(backText);
    }

    /**
     * Creates flashcard with basic fields and example.
     *
     * @param id unique identifier for the flashcard
     * @param deckId ID of the deck this flashcard belongs to
     * @param frontText text displayed on the front of the card
     * @param backText text displayed on the back of the card
     * @param example additional example or context for the flashcard
     * @throws IllegalArgumentException if deckId is null, or if frontText/backText are null or empty
     */
    public Flashcard(
            final Long id, final Long deckId, final String frontText, final String backText, final String example) {
        this(id, deckId, frontText, backText);
        this.example = example;
    }

    /**
     * Returns flashcard identifier.
     *
     * @return flashcard ID, or null if not persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets flashcard identifier.
     *
     * @param idValue flashcard ID to set
     */
    public void setId(final Long idValue) {
        this.id = idValue;
    }

    /**
     * Gets ID of the deck this flashcard belongs to.
     *
     * @return deck ID
     */
    public Long getDeckId() {
        return deckId;
    }

    /**
     * Sets deck identifier.
     *
     * @param deckIdValue deck ID to set
     * @throws IllegalArgumentException if deckId is null
     */
    public void setDeckId(final Long deckIdValue) {
        if (deckIdValue == null) {
            throw new IllegalArgumentException("deckId is required");
        }
        this.deckId = deckIdValue;
    }

    /**
     * Returns front text content.
     *
     * @return front text content
     */
    public String getFrontText() {
        return frontText;
    }

    /**
     * Sets front text content.
     *
     * @param frontTextValue text to display on the front
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
     * Returns back text content.
     *
     * @return back text content
     */
    public String getBackText() {
        return backText;
    }

    /**
     * Sets back text content.
     *
     * @param backTextValue text to display on the back
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
     * Compares flashcards for equality based on ID.
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
        Flashcard flashcard = (Flashcard) o;
        return Objects.equals(id, flashcard.id);
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
        return "Flashcard{"
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
