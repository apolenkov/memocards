package org.apolenkov.application.infrastructure.repository.jdbc.dto;

import java.time.LocalDateTime;

/**
 * JDBC DTO for news data transfer operations.
 *
 * <p>Immutable record representing news data for JDBC operations.
 * Used for mapping between database rows and domain models.</p>
 *
 * @param id unique news identifier
 * @param title news article title
 * @param content news article content
 * @param author news article author
 * @param createdAt news creation timestamp
 * @param updatedAt news last update timestamp
 */
public record NewsDto(
        Long id, String title, String content, String author, LocalDateTime createdAt, LocalDateTime updatedAt) {

    /**
     * Creates NewsDto with validation.
     *
     * @param id unique news identifier
     * @param title news article title
     * @param content news article content
     * @param author news article author
     * @param createdAt news creation timestamp
     * @param updatedAt news last update timestamp
     * @throws IllegalArgumentException if required fields are null/empty
     */
    public NewsDto {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be null or empty");
        }
    }

    /**
     * Creates NewsDto for new news article (without ID).
     *
     * @param title news article title
     * @param content news article content
     * @param author news article author
     * @return NewsDto for new news article
     */
    public static NewsDto forNewNews(final String title, final String content, final String author) {
        LocalDateTime now = LocalDateTime.now();
        return new NewsDto(null, title, content, author, now, now);
    }

    /**
     * Creates NewsDto for existing news article.
     *
     * @param id unique news identifier
     * @param title news article title
     * @param content news article content
     * @param author news article author
     * @param createdAt news creation timestamp
     * @param updatedAt news last update timestamp
     * @return NewsDto for existing news article
     */
    public static NewsDto forExistingNews(
            final Long id,
            final String title,
            final String content,
            final String author,
            final LocalDateTime createdAt,
            final LocalDateTime updatedAt) {
        return new NewsDto(id, title, content, author, createdAt, updatedAt);
    }
}
