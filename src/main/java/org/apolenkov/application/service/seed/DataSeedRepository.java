package org.apolenkov.application.service.seed;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apolenkov.application.infrastructure.repository.jdbc.batch.FlashcardBatchOperations;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.DeckSqlQueries;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.NewsSqlQueries;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.UserSqlQueries;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.News;
import org.apolenkov.application.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Repository for batch data operations used exclusively for seed/test data generation.
 * Separated from production repositories to avoid polluting domain layer with seed-specific methods.
 *
 * <p>This repository is only active in dev and test profiles and provides
 * optimized batch insert operations for initial data loading.
 */
@Repository
@Profile({"dev"})
public class DataSeedRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSeedRepository.class);

    private final JdbcTemplate jdbcTemplate;
    private final FlashcardBatchOperations flashcardBatchOperations;

    /**
     * Creates repository with required dependencies.
     *
     * @param jdbcTemplateValue JDBC template for database operations
     * @param flashcardBatchOperationsValue helper for flashcard batch operations
     */
    public DataSeedRepository(
            final JdbcTemplate jdbcTemplateValue, final FlashcardBatchOperations flashcardBatchOperationsValue) {
        this.jdbcTemplate = jdbcTemplateValue;
        this.flashcardBatchOperations = flashcardBatchOperationsValue;
    }

    /**
     * Batch inserts users with ON CONFLICT handling for idempotent seed operations.
     *
     * @param users collection of users to insert
     * @return list of saved users with generated IDs
     */
    public List<User> batchInsertUsers(final Collection<User> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }

        LOGGER.debug("Batch inserting {} users", users.size());
        LocalDateTime now = LocalDateTime.now();
        List<User> result = new ArrayList<>(users.size());
        List<Long> generatedIds = new ArrayList<>(users.size());

        // Insert users and collect generated IDs
        for (User user : users) {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(
                                UserSqlQueries.INSERT_USER_ON_CONFLICT_RETURNING_ID, Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1, user.getEmail());
                        ps.setString(2, user.getPasswordHash());
                        ps.setString(3, user.getName());
                        ps.setTimestamp(4, Timestamp.valueOf(now));
                        return ps;
                    },
                    keyHolder);

            Number key = keyHolder.getKey();
            if (key != null) {
                generatedIds.add(key.longValue());
            }
        }

        // Batch insert roles for all users
        List<Object[]> roleBatchArgs = new ArrayList<>();
        List<User> userList = new ArrayList<>(users);

        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            Long userId = generatedIds.get(i);

            for (String role : user.getRoles()) {
                roleBatchArgs.add(new Object[] {userId, role});
            }
        }

        if (!roleBatchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(UserSqlQueries.INSERT_USER_ROLE_ON_CONFLICT, roleBatchArgs);
        }

        // Build result users with generated IDs
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            Long generatedId = generatedIds.get(i);

            User savedUser = new User();
            savedUser.setId(generatedId);
            savedUser.setEmail(user.getEmail());
            savedUser.setName(user.getName());
            savedUser.setPasswordHash(user.getPasswordHash());
            savedUser.setRoles(new HashSet<>(user.getRoles()));

            result.add(savedUser);
        }

        LOGGER.debug("Batch inserted {} users successfully", result.size());
        return result;
    }

    /**
     * Batch inserts decks for seed operations.
     *
     * @param decks collection of decks to insert
     * @return list of saved decks with generated IDs
     */
    public List<Deck> batchInsertDecks(final Collection<Deck> decks) {
        if (decks == null || decks.isEmpty()) {
            return List.of();
        }

        LOGGER.debug("Batch inserting {} decks", decks.size());
        LocalDateTime now = LocalDateTime.now();
        List<Deck> result = new ArrayList<>(decks.size());

        for (Deck deck : decks) {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(
                                DeckSqlQueries.INSERT_DECK_RETURNING_ID, Statement.RETURN_GENERATED_KEYS);
                        ps.setLong(1, deck.getUserId());
                        ps.setString(2, deck.getTitle());
                        ps.setString(3, deck.getDescription());
                        ps.setTimestamp(4, Timestamp.valueOf(now));
                        ps.setTimestamp(5, Timestamp.valueOf(now));
                        return ps;
                    },
                    keyHolder);

            Number key = keyHolder.getKey();
            if (key != null) {
                Long generatedId = key.longValue();
                Deck savedDeck = new Deck(generatedId, deck.getUserId(), deck.getTitle(), deck.getDescription());
                result.add(savedDeck);
            }
        }

        LOGGER.debug("Batch inserted {} decks successfully", result.size());
        return result;
    }

    /**
     * Batch inserts flashcards for seed operations.
     *
     * @param flashcards collection of flashcards to insert
     */
    public void batchInsertFlashcards(final Collection<Flashcard> flashcards) {
        if (flashcards == null || flashcards.isEmpty()) {
            return;
        }

        LOGGER.debug("Batch inserting {} flashcards", flashcards.size());
        flashcardBatchOperations.saveAll(jdbcTemplate, flashcards);
        LOGGER.debug("Batch inserted flashcards successfully");
    }

    /**
     * Batch inserts news articles for seed operations.
     *
     * @param items collection of news items to insert
     */
    public void batchInsertNews(final Collection<News> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        LOGGER.debug("Batch inserting {} news items", items.size());
        LocalDateTime now = LocalDateTime.now();

        List<Object[]> batchArgs = new ArrayList<>(items.size());
        for (News news : items) {
            batchArgs.add(new Object[] {news.getTitle(), news.getContent(), news.getAuthor(), Timestamp.valueOf(now)});
        }

        jdbcTemplate.batchUpdate(NewsSqlQueries.INSERT_NEWS, batchArgs);
        LOGGER.debug("Batch inserted {} news items successfully", items.size());
    }
}
