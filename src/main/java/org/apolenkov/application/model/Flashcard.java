package org.apolenkov.application.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/** Flashcard model for flashcards application */
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

    public Flashcard() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Flashcard(Long id, Long deckId, String frontText, String backText) {
        this();
        this.id = id;
        setDeckId(deckId);
        setFrontText(frontText);
        setBackText(backText);
    }

    public Flashcard(Long id, Long deckId, String frontText, String backText, String example) {
        this(id, deckId, frontText, backText);
        this.example = example;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeckId() {
        return deckId;
    }

    public void setDeckId(Long deckId) {
        if (deckId == null) throw new IllegalArgumentException("deckId is required");
        this.deckId = deckId;
    }

    public String getFrontText() {
        return frontText;
    }

    public void setFrontText(String frontText) {
        String t = frontText != null ? frontText.trim() : null;
        if (t == null || t.isEmpty()) throw new IllegalArgumentException("frontText is required");
        this.frontText = t;
        this.updatedAt = LocalDateTime.now();
    }

    public String getBackText() {
        return backText;
    }

    public void setBackText(String backText) {
        String t = backText != null ? backText.trim() : null;
        if (t == null || t.isEmpty()) throw new IllegalArgumentException("backText is required");
        this.backText = t;
        this.updatedAt = LocalDateTime.now();
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example != null ? example.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl != null ? imageUrl.trim() : null;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flashcard flashcard = (Flashcard) o;
        return Objects.equals(id, flashcard.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

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
