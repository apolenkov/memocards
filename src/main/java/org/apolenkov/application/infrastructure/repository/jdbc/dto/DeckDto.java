package org.apolenkov.application.infrastructure.repository.jdbc.dto;

import java.time.LocalDateTime;

/**
 * JDBC DTO for deck data transfer operations.
 *
 * <p>Immutable record representing deck data for JDBC operations.
 * Used for mapping between database rows and domain models.</p>
 *
 * @param id unique deck identifier
 * @param userId ID of user who owns this deck
 * @param title deck title
 * @param description deck description
 * @param createdAt deck creation timestamp
 * @param updatedAt deck last update timestamp
 */
public record DeckDto(
        Long id, long userId, String title, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {

    /**
     * Creates DeckDto with validation.
     *
     * @param id unique deck identifier
     * @param userId ID of user who owns this deck
     * @param title deck title
     * @param description deck description
     * @param createdAt deck creation timestamp
     * @param updatedAt deck last update timestamp
     * @throws IllegalArgumentException if userId is invalid or title is null/empty
     */
    public DeckDto {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
    }

    /**
     * Creates DeckDto for new deck (without ID).
     *
     * @param userId ID of user who owns this deck
     * @param title deck title
     * @param description deck description
     * @return DeckDto for new deck
     */
    public static DeckDto forNewDeck(final long userId, final String title, final String description) {
        LocalDateTime now = LocalDateTime.now();
        return new DeckDto(null, userId, title, description, now, now);
    }

    /**
     * Creates DeckDto for existing deck.
     *
     * @param id unique deck identifier
     * @param userId ID of user who owns this deck
     * @param title deck title
     * @param description deck description
     * @param createdAt deck creation timestamp
     * @param updatedAt deck last update timestamp
     * @return DeckDto for existing deck
     */
    public static DeckDto forExistingDeck(
            final Long id,
            final long userId,
            final String title,
            final String description,
            final LocalDateTime createdAt,
            final LocalDateTime updatedAt) {
        return new DeckDto(id, userId, title, description, createdAt, updatedAt);
    }
}
