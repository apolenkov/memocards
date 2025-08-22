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
 *
 * <p>This class provides validation constraints and automatic timestamp management
 * for creation and modification tracking.</p>
 *
 *
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
     * Default constructor that initializes timestamps.
     *
     * <p>Sets both creation and update timestamps to the current time.</p>
     */
    public Flashcard() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructs a flashcard with basic required fields.
     *
     * <p>This constructor validates the input parameters and sets appropriate
     * timestamps. The deckId, frontText, and backText are validated for
     * non-null and non-empty values.</p>
     *
     * @param id the unique identifier for the flashcard
     * @param deckId the ID of the deck this flashcard belongs to
     * @param frontText the text displayed on the front of the card
     * @param backText the text displayed on the back of the card
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
     * Constructs a flashcard with basic fields and an example.
     *
     * <p>This constructor extends the basic constructor by adding an optional
     * example field that provides additional context for the flashcard.</p>
     *
     * @param id the unique identifier for the flashcard
     * @param deckId the ID of the deck this flashcard belongs to
     * @param frontText the text displayed on the front of the card
     * @param backText the text displayed on the back of the card
     * @param example additional example or context for the flashcard
     * @throws IllegalArgumentException if deckId is null, or if frontText/backText are null or empty
     */
    public Flashcard(Long id, Long deckId, String frontText, String backText, String example) {
        this(id, deckId, frontText, backText);
        this.example = example;
    }

    /**
     * Gets the unique identifier of the flashcard.
     *
     * @return the flashcard ID, or null if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the flashcard.
     *
     * @param id the flashcard ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the ID of the deck this flashcard belongs to.
     *
     * @return the deck ID
     */
    public Long getDeckId() {
        return deckId;
    }

    /**
     * Sets the deck ID for this flashcard.
     *
     * <p>Validates that the deckId is not null before setting it.</p>
     *
     * @param deckId the deck ID to set
     * @throws IllegalArgumentException if deckId is null
     */
    public void setDeckId(Long deckId) {
        if (deckId == null) throw new IllegalArgumentException("deckId is required");
        this.deckId = deckId;
    }

    /**
     * Gets the text displayed on the front of the flashcard.
     *
     * @return the front text content
     */
    public String getFrontText() {
        return frontText;
    }

    /**
     * Sets the front text of the flashcard.
     *
     * <p>Validates that the text is not null or empty after trimming.
     * Automatically updates the modification timestamp.</p>
     *
     * @param frontText the text to display on the front
     * @throws IllegalArgumentException if frontText is null or empty after trimming
     */
    public void setFrontText(String frontText) {
        String t = frontText != null ? frontText.trim() : null;
        if (t == null || t.isEmpty()) throw new IllegalArgumentException("frontText is required");
        this.frontText = t;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the text displayed on the back of the flashcard.
     *
     * @return the back text content
     */
    public String getBackText() {
        return backText;
    }

    /**
     * Sets the back text of the flashcard.
     *
     * <p>Validates that the text is not null or empty after trimming.
     * Automatically updates the modification timestamp.</p>
     *
     * @param backText the text to display on the back
     * @throws IllegalArgumentException if backText is null or empty after trimming
     */
    public void setBackText(String backText) {
        String t = backText != null ? backText.trim() : null;
        if (t == null || t.isEmpty()) throw new IllegalArgumentException("backText is required");
        this.backText = t;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the optional example or context for the flashcard.
     *
     * @return the example text, or null if not set
     */
    public String getExample() {
        return example;
    }

    /**
     * Sets the example text for the flashcard.
     *
     * <p>Trims the input text and automatically updates the modification timestamp.
     * The example is optional and can be null.</p>
     *
     * @param example the example text to set
     */
    public void setExample(String example) {
        this.example = example != null ? example.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the optional image URL associated with the flashcard.
     *
     * @return the image URL, or null if not set
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the image URL for the flashcard.
     *
     * <p>Trims the input URL and automatically updates the modification timestamp.
     * The image URL is optional and can be null.</p>
     *
     * @param imageUrl the image URL to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl != null ? imageUrl.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the timestamp when the flashcard was created.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp of the flashcard.
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when the flashcard was last modified.
     *
     * @return the last modification timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the modification timestamp of the flashcard.
     *
     * @param updatedAt the modification timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Compares this flashcard with another object for equality.
     *
     * <p>Two flashcards are considered equal if they have the same ID.
     * This implementation only considers the ID field for equality comparison.</p>
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flashcard flashcard = (Flashcard) o;
        return Objects.equals(id, flashcard.id);
    }

    /**
     * Returns a hash code value for this flashcard.
     *
     * <p>The hash code is based on the ID field to maintain consistency
     * with the equals method.</p>
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of the flashcard.
     *
     * <p>The string includes all field values for debugging purposes.</p>
     *
     * @return a string representation of this flashcard
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
