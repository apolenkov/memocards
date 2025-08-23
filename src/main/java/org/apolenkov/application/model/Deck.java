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
 * Represents a deck of flashcards in the application.
 *
 * <p>A deck is a collection of flashcards that belongs to a specific user.
 * Each deck has a title, optional description, and contains zero or more flashcards.</p>
 *
 */
public class Deck {
    private Long id;

    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 120)
    private String title;

    @Size(max = 500)
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Flashcard> flashcards;

    /**
     * Default constructor.
     * Initializes a new deck with current timestamps and empty flashcards list.
     */
    public Deck() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.flashcards = new ArrayList<>();
    }

    /**
     * Constructs a new Deck with the specified parameters.
     *
     * @param id the unique identifier for the deck
     * @param userId the ID of the user who owns this deck
     * @param title the title of the deck
     * @param description the description of the deck
     */
    public Deck(Long id, Long userId, String title, String description) {
        this();
        this.id = id;
        setUserId(userId);
        setTitle(title);
        setDescription(description);
    }

    /**
     * Gets the total number of flashcards in this deck.
     *
     * <p>This method safely handles null flashcards list and returns 0
     * if no flashcards are present.</p>
     *
     * @return the number of flashcards in this deck
     */
    public int getFlashcardCount() {
        return flashcards != null ? flashcards.size() : 0;
    }

    /**
     * Adds a flashcard to this deck.
     *
     * <p>This method performs the following operations:</p>
     * <ul>
     *   <li>Initializes the flashcards list if it's null</li>
     *   <li>Validates that the flashcard is not null</li>
     *   <li>Sets the deck ID on the flashcard to maintain referential integrity</li>
     *   <li>Updates the deck's modification timestamp</li>
     * </ul>
     *
     * @param flashcard the flashcard to add to this deck
     * @throws IllegalArgumentException if flashcard is null
     */
    public void addFlashcard(Flashcard flashcard) {
        if (flashcards == null) {
            flashcards = new ArrayList<>();
        }
        if (flashcard == null) {
            throw new IllegalArgumentException("flashcard is null");
        }
        flashcards.add(flashcard);
        flashcard.setDeckId(this.id);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Removes a flashcard from this deck.
     *
     * <p>This method safely removes the specified flashcard and updates
     * the deck's modification timestamp. If the flashcards list is null
     * or the flashcard is not found, no action is taken.</p>
     *
     * @param flashcard the flashcard to remove from this deck
     */
    public void removeFlashcard(Flashcard flashcard) {
        if (flashcards != null) {
            flashcards.remove(flashcard);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Creates a new deck with the specified parameters.
     *
     * <p>This static factory method provides a convenient way to create
     * new decks with proper validation and initialization. It ensures that:</p>
     * <ul>
     *   <li>User ID is provided and not null</li>
     *   <li>Title is provided, not null, and not empty after trimming</li>
     *   <li>Description is optional but trimmed if provided</li>
     *   <li>Timestamps are automatically set to current time</li>
     * </ul>
     *
     * @param userId the ID of the user who will own this deck
     * @param title the title of the deck (required, non-empty)
     * @param description the optional description of the deck
     * @return a new Deck instance with the specified parameters
     * @throws IllegalArgumentException if userId is null or title is null/empty
     */
    public static Deck create(Long userId, String title, String description) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        String t = title != null ? title.trim() : null;
        if (t == null || t.isEmpty()) throw new IllegalArgumentException("title is required");
        Deck d = new Deck();
        d.setUserId(userId);
        d.setTitle(t);
        d.setDescription(description != null ? description.trim() : null);
        return d;
    }

    /**
     * Gets the unique identifier of this deck.
     *
     * @return the deck ID, or null if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this deck.
     *
     * <p>This method is typically called by the persistence layer when the entity
     * is saved to the database.</p>
     *
     * @param id the deck ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the ID of the user who owns this deck.
     *
     * @return the user ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user who owns this deck.
     *
     * <p>This method validates that the user ID is not null, as every deck
     * must belong to a specific user.</p>
     *
     * @param userId the user ID to set
     * @throws IllegalArgumentException if userId is null
     */
    public void setUserId(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        this.userId = userId;
    }

    /**
     * Gets the title of this deck.
     *
     * @return the deck title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this deck.
     *
     * <p>This method validates and trims the title, ensuring it's not null
     * or empty. It also updates the modification timestamp.</p>
     *
     * @param title the deck title to set
     * @throws IllegalArgumentException if title is null or empty after trimming
     */
    public void setTitle(String title) {
        String t = title != null ? title.trim() : null;
        if (t == null || t.isEmpty()) throw new IllegalArgumentException("title is required");
        this.title = t;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the description of this deck.
     *
     * @return the deck description, or null if not set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this deck.
     *
     * <p>This method trims the description if provided and updates the
     * modification timestamp. Description is optional and can be null.</p>
     *
     * @param description the deck description to set
     */
    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the timestamp when this deck was created.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp of this deck.
     *
     * <p>This method is typically called by the persistence layer or when
     * creating new decks. It should not be modified after initial creation.</p>
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when this deck was last modified.
     *
     * @return the last modification timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the modification timestamp of this deck.
     *
     * <p>This method is typically called automatically when deck properties
     * are modified. It should not be called manually in most cases.</p>
     *
     * @param updatedAt the modification timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Gets an unmodifiable list of flashcards in this deck.
     *
     * <p>This method returns an immutable view of the flashcards list to prevent
     * external modification. To modify the deck's flashcards, use the dedicated
     * methods {@link #addFlashcard(Flashcard)} and {@link #removeFlashcard(Flashcard)}.</p>
     *
     * @return an unmodifiable list of flashcards, or empty list if no flashcards exist
     */
    public List<Flashcard> getFlashcards() {
        return flashcards == null ? List.of() : Collections.unmodifiableList(flashcards);
    }

    /**
     * Sets the list of flashcards for this deck.
     *
     * <p>This method creates a defensive copy of the provided list to prevent
     * external modification. It also updates the modification timestamp.</p>
     *
     * @param flashcards the list of flashcards to set, or null for empty list
     */
    public void setFlashcards(List<Flashcard> flashcards) {
        this.flashcards = new ArrayList<>(flashcards != null ? flashcards : List.of());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
                + ", flashcardCount="
                + getFlashcardCount()
                + ", createdAt="
                + createdAt
                + ", updatedAt="
                + updatedAt
                + '}';
    }
}
