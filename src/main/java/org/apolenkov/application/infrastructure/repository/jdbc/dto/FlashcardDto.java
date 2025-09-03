package org.apolenkov.application.infrastructure.repository.jdbc.dto;

import java.time.LocalDateTime;

/**
 * JDBC DTO for flashcard data transfer operations.
 *
 * <p>Immutable record representing flashcard data for JDBC operations.
 * Used for mapping between database rows and domain models.</p>
 *
 * @param id unique flashcard identifier
 * @param deckId ID of deck this flashcard belongs to
 * @param frontText front side text (question/prompt)
 * @param backText back side text (answer/explanation)
 * @param example optional example text
 * @param imageUrl optional image URL
 * @param timestamps flashcard creation and update timestamps
 */
public record FlashcardDto(
        Long id,
        long deckId,
        String frontText,
        String backText,
        String example,
        String imageUrl,
        FlashcardTimestamps timestamps) {

    /**
     * Creates FlashcardDto with validation.
     *
     * @param id unique flashcard identifier
     * @param deckId ID of deck this flashcard belongs to
     * @param frontText front side text (question/prompt)
     * @param backText back side text (answer/explanation)
     * @param example optional example text
     * @param imageUrl optional image URL
     * @throws IllegalArgumentException if deckId is invalid or required text is null/empty
     */
    public FlashcardDto {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive");
        }
        if (frontText == null || frontText.trim().isEmpty()) {
            throw new IllegalArgumentException("Front text cannot be null or empty");
        }
        if (backText == null || backText.trim().isEmpty()) {
            throw new IllegalArgumentException("Back text cannot be null or empty");
        }
    }

    /**
     * Creates FlashcardDto for new flashcard (without ID).
     *
     * @param deckId ID of deck this flashcard belongs to
     * @param frontText front side text (question/prompt)
     * @param backText back side text (answer/explanation)
     * @param example optional example text
     * @param imageUrl optional image URL
     * @return FlashcardDto for new flashcard
     */
    public static FlashcardDto forNewFlashcard(
            final long deckId,
            final String frontText,
            final String backText,
            final String example,
            final String imageUrl) {
        LocalDateTime now = LocalDateTime.now();
        return new FlashcardDto(
                null, deckId, frontText, backText, example, imageUrl, new FlashcardTimestamps(now, now));
    }

    /**
     * Creates FlashcardDto for existing flashcard.
     *
     * @param id unique flashcard identifier
     * @param deckId ID of deck this flashcard belongs to
     * @param frontText front side text (question/prompt)
     * @param backText back side text (answer/explanation)
     * @param example optional example text
     * @param imageUrl optional image URL
     * @param timestamps flashcard creation and update timestamps
     * @return FlashcardDto for existing flashcard
     */
    public static FlashcardDto forExistingFlashcard(
            final Long id,
            final long deckId,
            final String frontText,
            final String backText,
            final String example,
            final String imageUrl,
            final FlashcardTimestamps timestamps) {
        return new FlashcardDto(id, deckId, frontText, backText, example, imageUrl, timestamps);
    }

    /**
     * Timestamps for flashcard creation and updates.
     *
     * @param createdAt flashcard creation timestamp
     * @param updatedAt flashcard last update timestamp
     */
    public record FlashcardTimestamps(LocalDateTime createdAt, LocalDateTime updatedAt) {}
}
