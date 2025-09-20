package org.apolenkov.application.infrastructure.repository.jdbc.adapter;

import java.util.Optional;
import org.apolenkov.application.domain.port.UserSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of UserSettingsRepository.
 * Handles persistence and retrieval of user settings using direct JDBC operations.
 */
@Repository
@Profile({"dev", "prod", "test"})
public class UserSettingsJdbcAdapter implements UserSettingsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSettingsJdbcAdapter.class);
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates UserSettingsJdbcAdapter with JDBC template.
     *
     * @param jdbcTemplateParam JDBC template for database operations
     */
    public UserSettingsJdbcAdapter(final JdbcTemplate jdbcTemplateParam) {
        this.jdbcTemplate = jdbcTemplateParam;
    }

    /**
     * Finds preferred locale code for the given user.
     * This method can be safely overridden by subclasses.
     *
     * @param userId the user ID to find locale for
     * @return optional locale code
     */
    @Override
    public Optional<String> findPreferredLocaleCode(final long userId) {
        LOGGER.debug("Finding preferred locale code for user ID: {}", userId);
        String sql = "SELECT preferred_locale_code FROM user_settings WHERE user_id = ?";
        try {
            String locale = jdbcTemplate.queryForObject(sql, String.class, userId);
            return Optional.ofNullable(locale);
        } catch (DataAccessException e) {
            LOGGER.debug("No user settings found for user ID: {}", userId);
            return Optional.empty();
        }
    }

    /**
     * Saves preferred locale code for the given user.
     * This method can be safely overridden by subclasses.
     *
     * @param userId the user ID to save locale for
     * @param localeCode the locale code to save
     */
    @Override
    public void savePreferredLocaleCode(final long userId, final String localeCode) {
        LOGGER.debug("Saving preferred locale code '{}' for user ID: {}", localeCode, userId);
        String sql =
                """
            INSERT INTO user_settings (user_id, preferred_locale_code)
            VALUES (?, ?)
            ON CONFLICT (user_id)
            DO UPDATE SET preferred_locale_code = ?
            """;
        jdbcTemplate.update(sql, userId, localeCode, localeCode);
    }
}
