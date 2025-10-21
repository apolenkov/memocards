package org.apolenkov.application.infrastructure.repository.jdbc.dto;

import java.time.LocalDateTime;

/**
 * JDBC DTO for card data transfer operations.
 *
 * <p>Immutable record representing card data for JDBC operations.
 * Used for mapping between database rows and domain models.</p>
 *
 * @param id unique card identifier
 * @param deckId ID of deck this card belongs to
 * @param frontText front side text (question/prompt)
 * @param backText back side text (answer/explanation)
 * @param example optional example text
 * @param imageUrl optional image URL
 * @param timestamps card creation and update timestamps
 */
public record CardDto(
        Long id,
        long deckId,
        String frontText,
        String backText,
        String example,
        String imageUrl,
        CardTimestamps timestamps) {

    /**
     * Creates CardDto with validation.
     *
     * @param id unique card identifier
     * @param deckId ID of deck this card belongs to
     * @param frontText front side text (question/prompt)
     * @param backText back side text (answer/explanation)
     * @param example optional example text
     * @param imageUrl optional image URL
     * @throws IllegalArgumentException if deckId is invalid or required text is null/empty
     */
    public CardDto {
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
     * Creates CardDto for new card (without ID).
     *
     * @param deckId ID of deck this card belongs to
     * @param frontText front side text (question/prompt)
     * @param backText back side text (answer/explanation)
     * @param example optional example text
     * @param imageUrl optional image URL
     * @return CardDto for new card
     */
    public static CardDto forNewCard(
            final long deckId,
            final String frontText,
            final String backText,
            final String example,
            final String imageUrl) {
        LocalDateTime now = LocalDateTime.now();
        return new CardDto(null, deckId, frontText, backText, example, imageUrl, new CardTimestamps(now, now));
    }

    /**
     * Creates CardDto for existing card.
     *
     * @param id unique card identifier
     * @param deckId ID of deck this card belongs to
     * @param frontText front side text (question/prompt)
     * @param backText back side text (answer/explanation)
     * @param example optional example text
     * @param imageUrl optional image URL
     * @param timestamps card creation and update timestamps
     * @return CardDto for existing card
     */
    public static CardDto forExistingCard(
            final Long id,
            final long deckId,
            final String frontText,
            final String backText,
            final String example,
            final String imageUrl,
            final CardTimestamps timestamps) {
        return new CardDto(id, deckId, frontText, backText, example, imageUrl, timestamps);
    }

    /**
     * Timestamps for card creation and updates.
     *
     * @param createdAt card creation timestamp
     * @param updatedAt card last update timestamp
     */
    public record CardTimestamps(LocalDateTime createdAt, LocalDateTime updatedAt) {}
}
