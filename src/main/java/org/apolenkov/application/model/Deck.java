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

    public int getFlashcardCount() {
        return flashcards != null ? flashcards.size() : 0;
    }

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

    public void removeFlashcard(Flashcard flashcard) {
        if (flashcards != null) {
            flashcards.remove(flashcard);
            this.updatedAt = LocalDateTime.now();
        }
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        String t = title != null ? title.trim() : null;
        if (t == null || t.isEmpty()) throw new IllegalArgumentException("title is required");
        this.title = t;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Flashcard> getFlashcards() {
        return flashcards == null ? List.of() : Collections.unmodifiableList(flashcards);
    }

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
