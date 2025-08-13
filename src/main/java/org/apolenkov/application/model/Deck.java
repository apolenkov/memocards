package org.apolenkov.application.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Deck model for flashcards application */
public class Deck {
  private Long id;
  @NotNull private Long userId;

  @NotBlank
  @Size(max = 120)
  private String title;

  @Size(max = 500)
  private String description;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<Flashcard> flashcards;

  public Deck() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    this.flashcards = new ArrayList<>();
  }

  public Deck(Long id, Long userId, String title, String description) {
    this();
    this.id = id;
    this.userId = userId;
    this.title = title;
    this.description = description;
  }

  // Convenience methods
  public int getFlashcardCount() {
    return flashcards != null ? flashcards.size() : 0;
  }

  public void addFlashcard(Flashcard flashcard) {
    if (flashcards == null) {
      flashcards = new ArrayList<>();
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

  // Getters and setters
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
    this.userId = userId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
    this.updatedAt = LocalDateTime.now();
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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
    return flashcards;
  }

  public void setFlashcards(List<Flashcard> flashcards) {
    this.flashcards = flashcards;
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
