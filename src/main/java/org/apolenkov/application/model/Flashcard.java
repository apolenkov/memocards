package org.apolenkov.application.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a flashcard in the application.
 *
 * <p>A flashcard is a learning tool that contains information on both sides:
 * the front (question/prompt) and back (answer/explanation). Flashcards are
 * organized into decks and can include additional metadata like examples and images.</p>
 */
public class Flashcard {
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
     * Creates flashcard with basic required fields.
     *
     * <p>Validates input parameters and sets appropriate timestamps.
     * DeckId, frontText, and backText are required.</p>
     *
     * @param id unique identifier for the flashcard
     * @param deckId ID of the deck this flashcard belongs to
     * @param frontText text displayed on the front of the card
     * @param backText text displayed on the back of the card
     * @throws IllegalArgumentException if deckId is null, or if frontText/backText are null or empty
     */
    public Flashcard(Long id, Long deckId, String frontText, String backText) {
        this();
        this.id = id;
        setDeckId(deckId);
        setFrontText(frontText);
        setBackText(backText);
    }

    /**
     * Creates flashcard with basic fields and example.
     *
     * <p>Extends basic constructor by adding optional example field
     * that provides additional context.</p>
     *
     * @param id unique identifier for the flashcard
     * @param deckId ID of the deck this flashcard belongs to
     * @param frontText text displayed on the front of the card
     * @param backText text displayed on the back of the card
     * @param example additional example or context for the flashcard
     * @throws IllegalArgumentException if deckId is null, or if frontText/backText are null or empty
     */
    public Flashcard(Long id, Long deckId, String frontText, String backText, String example) {
        this(id, deckId, frontText, backText);
        this.example = example;
    }

    /**
     * Gets unique identifier of the flashcard.
     *
     * @return flashcard ID, or null if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets unique identifier of the flashcard.
     *
     * @param id flashcard ID to set
     */
    public void setId(Long id) {
        this.id = id;
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
     * Sets deck ID for this flashcard.
     *
     * <p>Validates that deckId is not null before setting it.</p>
     *
     * @param deckId deck ID to set
     * @throws IllegalArgumentException if deckId is null
     */
    public void setDeckId(Long deckId) {
        if (deckId == null) throw new IllegalArgumentException("deckId is required");
        this.deckId = deckId;
    }

    /**
     * Gets text displayed on the front of the flashcard.
     *
     * @return front text content
     */
    public String getFrontText() {
        return frontText;
    }

    /**
     * Sets front text of the flashcard.
     *
     * <p>Validates that text is not null or empty after trimming.
     * Automatically updates modification timestamp.</p>
     *
     * @param frontText text to display on the front
     * @throws IllegalArgumentException if frontText is null or empty after trimming
     */
    public void setFrontText(String frontText) {
        String t = frontText != null ? frontText.trim() : null;
        if (t == null || t.isEmpty()) throw new IllegalArgumentException("frontText is required");
        this.frontText = t;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets text displayed on the back of the flashcard.
     *
     * @return back text content
     */
    public String getBackText() {
        return backText;
    }

    /**
     * Sets back text of the flashcard.
     *
     * <p>Validates that text is not null or empty after trimming.
     * Automatically updates modification timestamp.</p>
     *
     * @param backText text to display on the back
     * @throws IllegalArgumentException if backText is null or empty after trimming
     */
    public void setBackText(String backText) {
        String t = backText != null ? backText.trim() : null;
        if (t == null || t.isEmpty()) throw new IllegalArgumentException("backText is required");
        this.backText = t;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets optional example or context for the flashcard.
     *
     * @return example text, or null if not set
     */
    public String getExample() {
        return example;
    }

    /**
     * Sets example text for the flashcard.
     *
     * <p>Trims input text and automatically updates modification timestamp.
     * Example is optional and can be null.</p>
     *
     * @param example example text to set
     */
    public void setExample(String example) {
        this.example = example != null ? example.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets optional image URL associated with the flashcard.
     *
     * @return image URL, or null if not set
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets image URL for the flashcard.
     *
     * <p>Trims input URL and automatically updates modification timestamp.
     * Image URL is optional and can be null.</p>
     *
     * @param imageUrl image URL to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl != null ? imageUrl.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets timestamp when the flashcard was created.
     *
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets creation timestamp of the flashcard.
     *
     * @param createdAt creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets timestamp when the flashcard was last modified.
     *
     * @return last modification timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets modification timestamp of the flashcard.
     *
     * @param updatedAt modification timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Compares this flashcard with another object for equality.
     *
     * <p>Two flashcards are considered equal if they have the same ID.</p>
     *
     * @param o object to compare with
     * @return true if objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flashcard flashcard = (Flashcard) o;
        return Objects.equals(id, flashcard.id);
    }

    /**
     * Returns hash code value for this flashcard.
     *
     * <p>Hash code is based on ID field to maintain consistency with equals method.</p>
     *
     * @return hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns string representation of the flashcard.
     *
     * <p>Includes all field values for debugging purposes.</p>
     *
     * @return string representation of this flashcard
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
