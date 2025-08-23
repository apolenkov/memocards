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
     * Creates new deck with current timestamps and empty flashcards list.
     */
    public Deck() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.flashcards = new ArrayList<>();
    }

    /**
     * Creates deck with specified parameters.
     *
     * @param id unique identifier for the deck
     * @param userId ID of the user who owns this deck
     * @param title title of the deck
     * @param description description of the deck
     */
    public Deck(Long id, Long userId, String title, String description) {
        this();
        this.id = id;
        setUserId(userId);
        setTitle(title);
        setDescription(description);
    }

    /**
     * Gets total number of flashcards in this deck.
     *
     * @return number of flashcards in deck
     */
    public int getFlashcardCount() {
        return flashcards != null ? flashcards.size() : 0;
    }

    /**
     * Adds flashcard to this deck.
     *
     * <p>Initializes flashcards list if null, validates flashcard,
     * sets deck ID on flashcard, and updates modification timestamp.</p>
     *
     * @param flashcard flashcard to add to deck
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
     * Removes flashcard from this deck.
     *
     * <p>Safely removes specified flashcard and updates modification timestamp.
     * No action taken if flashcards list is null or flashcard not found.</p>
     *
     * @param flashcard flashcard to remove from deck
     */
    public void removeFlashcard(Flashcard flashcard) {
        if (flashcards != null) {
            flashcards.remove(flashcard);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Creates new deck with specified parameters.
     *
     * <p>Static factory method that ensures proper validation and initialization.
     * User ID and title are required, description is optional.</p>
     *
     * @param userId ID of the user who will own this deck
     * @param title title of the deck (required, non-empty)
     * @param description optional description of the deck
     * @return new Deck instance with specified parameters
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
     * Gets unique identifier of this deck.
     *
     * @return deck ID, or null if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets unique identifier of this deck.
     *
     * @param id deck ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets ID of the user who owns this deck.
     *
     * @return user ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets ID of the user who owns this deck.
     *
     * <p>Validates that user ID is not null, as every deck must belong to a specific user.</p>
     *
     * @param userId user ID to set
     * @throws IllegalArgumentException if userId is null
     */
    public void setUserId(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        this.userId = userId;
    }

    /**
     * Gets title of this deck.
     *
     * @return deck title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title of this deck.
     *
     * <p>Validates and trims title, ensuring it's not null or empty.
     * Updates modification timestamp.</p>
     *
     * @param title deck title to set
     * @throws IllegalArgumentException if title is null or empty after trimming
     */
    public void setTitle(String title) {
        String t = title != null ? title.trim() : null;
        if (t == null || t.isEmpty()) throw new IllegalArgumentException("title is required");
        this.title = t;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets description of this deck.
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
     * @param description deck description to set
     */
    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets timestamp when this deck was created.
     *
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets creation timestamp of this deck.
     *
     * @param createdAt creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets timestamp when this deck was last modified.
     *
     * @return last modification timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets modification timestamp of this deck.
     *
     * @param updatedAt modification timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Gets unmodifiable list of flashcards in this deck.
     *
     * <p>Returns immutable view to prevent external modification.
     * Use dedicated methods to modify deck's flashcards.</p>
     *
     * @return unmodifiable list of flashcards, or empty list if none exist
     */
    public List<Flashcard> getFlashcards() {
        return flashcards == null ? List.of() : Collections.unmodifiableList(flashcards);
    }

    /**
     * Sets list of flashcards for this deck.
     *
     * <p>Creates defensive copy to prevent external modification.
     * Updates modification timestamp.</p>
     *
     * @param flashcards list of flashcards to set, or null for empty list
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
