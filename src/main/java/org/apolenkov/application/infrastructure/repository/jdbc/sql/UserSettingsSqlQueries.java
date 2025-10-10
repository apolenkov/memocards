package org.apolenkov.application.infrastructure.repository.jdbc.sql;

/**
 * SQL queries for user settings operations.
 *
 * <p>Contains all SQL queries used by UserSettingsJdbcAdapter.
 * Uses parameterized queries to prevent SQL injection.</p>
 */
public final class UserSettingsSqlQueries {

    private UserSettingsSqlQueries() {
        // Utility class
    }

    /**
     * SQL query to upsert preferred locale code for user.
     */
    public static final String UPSERT_PREFERRED_LOCALE =
            """
            INSERT INTO user_settings (user_id, preferred_locale_code)
            VALUES (?, ?)
            ON CONFLICT (user_id)
            DO UPDATE SET preferred_locale_code = ?
            """;
}
