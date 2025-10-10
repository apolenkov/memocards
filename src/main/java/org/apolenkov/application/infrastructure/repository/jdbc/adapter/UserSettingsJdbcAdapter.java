package org.apolenkov.application.infrastructure.repository.jdbc.adapter;

import org.apolenkov.application.domain.port.UserSettingsRepository;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.UserSettingsSqlQueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
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
     * Saves preferred locale code for the given user.
     * This method can be safely overridden by subclasses.
     *
     * @param userId the user ID to save locale for
     * @param localeCode the locale code to save
     */
    @Override
    public void savePreferredLocaleCode(final long userId, final String localeCode) {
        LOGGER.debug("Saving preferred locale code '{}' for user ID: {}", localeCode, userId);
        jdbcTemplate.update(UserSettingsSqlQueries.UPSERT_PREFERRED_LOCALE, userId, localeCode, localeCode);
    }
}
