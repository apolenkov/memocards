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
 * Collection of flashcards belonging to a user.
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
     * Creates empty deck with current timestamps.
     */
    public Deck() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.flashcards = new ArrayList<>();
    }

    /**
     * Creates new deck with specified fields.
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
     * Creates new deck with validation.
     *
     * @param userId ID of the user who will own this deck
     * @param title title of the deck (required)
     * @param description optional description of the deck
     * @return new Deck instance
     * @throws IllegalArgumentException if userId is null or title is empty
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
     * Returns number of flashcards in deck.
     *
     * @return number of flashcards
     */
    public int getFlashcardCount() {
        return flashcards != null ? flashcards.size() : 0;
    }

    /**
     * Adds flashcard to deck.
     *
     * @param flashcard flashcard to add
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
     * Removes flashcard from deck.
     *
     * @param flashcard flashcard to remove
     */
    public void removeFlashcard(Flashcard flashcard) {
        if (flashcards != null) {
            flashcards.remove(flashcard);
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
     * @param id deck ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns user identifier.
     *
     * @return user ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets user identifier.
     *
     * @param userId user ID to set
     * @throws IllegalArgumentException if userId is null
     */
    public void setUserId(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        this.userId = userId;
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
     * @param title deck title to set
     * @throws IllegalArgumentException if title is null or empty
     */
    public void setTitle(String title) {
        String t = title != null ? title.trim() : null;
        if (t == null || t.isEmpty()) throw new IllegalArgumentException("title is required");
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
     * @param description deck description to set
     */
    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
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
     * @param createdAt creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
     * @param updatedAt modification timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns unmodifiable list of flashcards.
     *
     * @return unmodifiable list of flashcards, or empty list if none exist
     */
    public List<Flashcard> getFlashcards() {
        return flashcards == null ? List.of() : Collections.unmodifiableList(flashcards);
    }

    /**
     * Sets list of flashcards.
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
