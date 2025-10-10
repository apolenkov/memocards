package org.apolenkov.application.infrastructure.repository.jdbc.sql;

/**
 * SQL queries for password reset token operations.
 *
 * <p>Contains all SQL queries used by PasswordResetTokenJdbcAdapter.
 * Uses parameterized queries to prevent SQL injection.</p>
 */
public final class PasswordResetTokenSqlQueries {

    private PasswordResetTokenSqlQueries() {
        // Utility class
    }

    /**
     * SQL query to insert new password reset token.
     */
    public static final String INSERT_TOKEN =
            """
            INSERT INTO password_reset_tokens (token, user_id, expires_at, used)
            VALUES (?, ?, ?, ?)
            """;

    /**
     * SQL query to update existing password reset token.
     */
    public static final String UPDATE_TOKEN =
            """
            UPDATE password_reset_tokens
            SET token = ?, user_id = ?, expires_at = ?, used = ?
            WHERE id = ?
            """;

    /**
     * SQL query to select password reset token by token string.
     */
    public static final String SELECT_BY_TOKEN =
            """
            SELECT id, token, user_id, expires_at, used
            FROM password_reset_tokens
            WHERE token = ?
            """;

    /**
     * SQL query to select unused password reset token for user.
     */
    public static final String SELECT_BY_USER_ID_NOT_USED =
            """
            SELECT id, token, user_id, expires_at, used
            FROM password_reset_tokens
            WHERE user_id = ? AND used = false
            ORDER BY expires_at DESC
            LIMIT 1
            """;

    /**
     * SQL query to mark password reset token as used.
     */
    public static final String MARK_AS_USED =
            """
            UPDATE password_reset_tokens
            SET used = true
            WHERE id = ?
            """;
}
