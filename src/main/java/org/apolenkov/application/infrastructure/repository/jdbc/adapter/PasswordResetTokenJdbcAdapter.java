package org.apolenkov.application.infrastructure.repository.jdbc.adapter;

import java.time.LocalDateTime;
import java.util.Optional;
import org.apolenkov.application.domain.port.PasswordResetTokenRepository;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.PasswordResetTokenSqlQueries;
import org.apolenkov.application.model.PasswordResetToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of PasswordResetTokenRepository.
 * Handles persistence and retrieval of password reset tokens using direct JDBC operations.
 */
@Repository
@Profile({"dev", "prod", "test"})
public class PasswordResetTokenJdbcAdapter implements PasswordResetTokenRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetTokenJdbcAdapter.class);
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<PasswordResetToken> PASSWORD_RESET_TOKEN_ROW_MAPPER = (rs, rowNum) -> {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(rs.getLong("id"));
        token.setToken(rs.getString("token"));
        token.setUserId(rs.getLong("user_id"));
        token.setExpiresAt(rs.getObject("expires_at", LocalDateTime.class));
        token.setUsed(rs.getBoolean("used"));
        return token;
    };

    /**
     * Creates PasswordResetTokenJdbcAdapter with JDBC template.
     *
     * @param jdbcTemplateParam JDBC template for database operations
     */
    public PasswordResetTokenJdbcAdapter(final JdbcTemplate jdbcTemplateParam) {
        this.jdbcTemplate = jdbcTemplateParam;
    }

    /**
     * Saves password reset token to database.
     * This method can be safely overridden by subclasses.
     *
     * @param token the token to save
     */
    @Override
    public void save(final PasswordResetToken token) {
        LOGGER.debug("Saving password reset token: {}", token);
        if (token.getId() == null) {
            jdbcTemplate.update(
                    PasswordResetTokenSqlQueries.INSERT_TOKEN,
                    token.getToken(),
                    token.getUserId(),
                    token.getExpiresAt(),
                    token.isUsed());
        } else {
            jdbcTemplate.update(
                    PasswordResetTokenSqlQueries.UPDATE_TOKEN,
                    token.getToken(),
                    token.getUserId(),
                    token.getExpiresAt(),
                    token.isUsed(),
                    token.getId());
        }
    }

    /**
     * Finds password reset token by token string.
     * This method can be safely overridden by subclasses.
     *
     * @param token the token string to find
     * @return optional password reset token
     */
    @Override
    public Optional<PasswordResetToken> findByToken(final String token) {
        LOGGER.debug("Finding password reset token: {}", token);
        try {
            PasswordResetToken resetToken = jdbcTemplate.queryForObject(
                    PasswordResetTokenSqlQueries.SELECT_BY_TOKEN, PASSWORD_RESET_TOKEN_ROW_MAPPER, token);
            return Optional.ofNullable(resetToken);
        } catch (DataAccessException e) {
            LOGGER.debug("No password reset token found: {}", token);
            return Optional.empty();
        }
    }

    /**
     * Finds unused password reset token for user.
     * This method can be safely overridden by subclasses.
     *
     * @param userId the user ID to find token for
     * @return optional unused password reset token
     */
    @Override
    public Optional<PasswordResetToken> findByUserIdAndNotUsed(final long userId) {
        LOGGER.debug("Finding unused password reset token for user ID: {}", userId);
        try {
            PasswordResetToken resetToken = jdbcTemplate.queryForObject(
                    PasswordResetTokenSqlQueries.SELECT_BY_USER_ID_NOT_USED, PASSWORD_RESET_TOKEN_ROW_MAPPER, userId);
            return Optional.ofNullable(resetToken);
        } catch (DataAccessException e) {
            LOGGER.debug("No unused password reset token found for user ID: {}", userId);
            return Optional.empty();
        }
    }

    /**
     * Marks password reset token as used.
     * This method can be safely overridden by subclasses.
     *
     * @param id the token ID to mark as used
     */
    @Override
    public void markAsUsed(final long id) {
        LOGGER.debug("Marking password reset token as used: {}", id);
        jdbcTemplate.update(PasswordResetTokenSqlQueries.MARK_AS_USED, id);
    }
}
