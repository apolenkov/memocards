package org.apolenkov.application.infrastructure.repository.jdbc.sql;

/**
 * SQL queries for user operations.
 *
 * <p>Contains all SQL queries used by UserJdbcAdapter.
 * Uses parameterized queries to prevent SQL injection.</p>
 */
public final class UserSqlQueries {

    private UserSqlQueries() {
        // Utility class
    }

    /**
     * SQL query to select all users.
     */
    public static final String SELECT_ALL_USERS =
            """
            SELECT u.id, u.email, u.password_hash, u.name, u.created_at
            FROM users u
            ORDER BY u.created_at DESC
            """;

    /**
     * SQL query to select user by ID.
     */
    public static final String SELECT_USER_BY_ID =
            """
            SELECT u.id, u.email, u.password_hash, u.name, u.created_at
            FROM users u
            WHERE u.id = ?
            """;

    /**
     * SQL query to select user by email.
     */
    public static final String SELECT_USER_BY_EMAIL =
            """
            SELECT u.id, u.email, u.password_hash, u.name, u.created_at
            FROM users u
            WHERE u.email = ?
            """;

    /**
     * SQL query to select user roles by user ID.
     */
    public static final String SELECT_USER_ROLES =
            """
            SELECT ur.role
            FROM user_roles ur
            WHERE ur.user_id = ?
            ORDER BY ur.role
            """;

    /**
     * SQL query to insert new user.
     */
    public static final String INSERT_USER =
            """
            INSERT INTO users (email, password_hash, name, created_at)
            VALUES (?, ?, ?, ?)
            """;

    /**
     * SQL query to update existing user.
     */
    public static final String UPDATE_USER =
            """
            UPDATE users
            SET email = ?, password_hash = ?, name = ?
            WHERE id = ?
            """;

    /**
     * SQL query to delete user by ID.
     */
    public static final String DELETE_USER = """
            DELETE FROM users
            WHERE id = ?
            """;

    /**
     * SQL query to insert user role.
     */
    public static final String INSERT_USER_ROLE =
            """
            INSERT INTO user_roles (user_id, role)
            VALUES (?, ?)
            """;

    /**
     * SQL query to delete all user roles.
     */
    public static final String DELETE_USER_ROLES =
            """
            DELETE FROM user_roles
            WHERE user_id = ?
            """;

    /**
     * SQL query to check if user exists by email.
     */
    public static final String EXISTS_USER_BY_EMAIL =
            """
            SELECT COUNT(1)
            FROM users
            WHERE email = ?
            """;

    /**
     * SQL query to get user count.
     */
    public static final String COUNT_USERS = """
            SELECT COUNT(1)
            FROM users
            """;
}
