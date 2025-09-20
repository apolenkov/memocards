package org.apolenkov.application.infrastructure.repository.jdbc.sql;

/**
 * SQL queries for news operations.
 *
 * <p>Contains all SQL queries used by NewsJdbcAdapter.
 * Uses parameterized queries to prevent SQL injection.</p>
 */
public final class NewsSqlQueries {

    private NewsSqlQueries() {
        // Utility class
    }

    /**
     * SQL query to select all news ordered by creation date (newest first).
     */
    public static final String SELECT_ALL_NEWS_ORDER_BY_CREATED_DESC =
            """
            SELECT n.id, n.title, n.content, n.author, n.created_at, n.updated_at
            FROM news n
            ORDER BY n.created_at DESC
            """;

    /**
     * SQL query to select news by ID.
     */
    public static final String SELECT_NEWS_BY_ID =
            """
            SELECT n.id, n.title, n.content, n.author, n.created_at, n.updated_at
            FROM news n
            WHERE n.id = ?
            """;

    /**
     * SQL query to insert new news article.
     */
    public static final String INSERT_NEWS =
            """
            INSERT INTO news (title, content, author, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?)
            """;

    /**
     * SQL query to update existing news article.
     */
    public static final String UPDATE_NEWS =
            """
            UPDATE news
            SET title = ?, content = ?, author = ?, updated_at = ?
            WHERE id = ?
            """;

    /**
     * SQL query to delete news by ID.
     */
    public static final String DELETE_NEWS = """
            DELETE FROM news
            WHERE id = ?
            """;
}
