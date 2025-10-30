package org.apolenkov.application.infrastructure.repository.jdbc.batch;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.CardSqlQueries;
import org.apolenkov.application.model.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Helper class for batch operations on cards.
 * Separates batch logic from adapter for better maintainability.
 */
@Component
public class CardBatchOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardBatchOperations.class);
    private static final int DEFAULT_BATCH_SIZE = 1000;

    /**
     * Performs batch save operations for cards.
     *
     * @param jdbcTemplate JDBC template for database operations
     * @param cards collection of cards to save
     */
    public void saveAll(final JdbcTemplate jdbcTemplate, final Collection<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return;
        }

        LOGGER.debug("Batch saving {} cards", cards.size());

        // Separate new cards from updates
        List<Card> newCards = cards.stream().filter(f -> f.getId() == null).toList();

        List<Card> existingCards = cards.stream().filter(f -> f.getId() != null).toList();

        // Batch insert new cards
        if (!newCards.isEmpty()) {
            batchInsert(jdbcTemplate, newCards);
        }

        // Batch update existing cards
        if (!existingCards.isEmpty()) {
            batchUpdate(jdbcTemplate, existingCards);
        }

        LOGGER.debug("Batch saved {} cards successfully", cards.size());
    }

    /**
     * Batch inserts new cards in chunks for optimal performance.
     * NOTE: Generated IDs are NOT set on card objects due to Spring JDBC limitations.
     * For seed operations this is acceptable as IDs are not needed after insert.
     * For production use cases that require IDs, use single save() in loop.
     *
     * @param jdbcTemplate JDBC template for database operations
     * @param newCards list of new cards
     */
    private void batchInsert(final JdbcTemplate jdbcTemplate, final List<Card> newCards) {
        LocalDateTime now = LocalDateTime.now();
        long startTime = System.currentTimeMillis();
        int chunkCount = 0;

        // Process in chunks to avoid memory issues
        for (int i = 0; i < newCards.size(); i += DEFAULT_BATCH_SIZE) {
            int end = Math.min(i + DEFAULT_BATCH_SIZE, newCards.size());
            List<Card> chunk = newCards.subList(i, end);

            List<Object[]> batchArgs = prepareBatchInsertArgs(chunk, now);

            // NOTE: batchUpdate with RETURNING ID doesn't populate IDs back to objects
            // This is a Spring JDBC limitation - acceptable for seed operations
            jdbcTemplate.batchUpdate(CardSqlQueries.INSERT_CARD_RETURNING_ID, batchArgs);
            chunkCount++;
        }

        // Log summary after completion (no logging in loop)
        long duration = System.currentTimeMillis() - startTime;
        LOGGER.debug("Batch insert completed: {} cards in {} chunks, took {}ms", newCards.size(), chunkCount, duration);
    }

    /**
     * Prepares batch arguments for insert operation.
     *
     * @param cards list of cards
     * @param timestamp timestamp for created_at and updated_at
     * @return list of batch arguments
     */
    private List<Object[]> prepareBatchInsertArgs(final List<Card> cards, final LocalDateTime timestamp) {
        List<Object[]> batchArgs = new ArrayList<>(cards.size());

        for (Card card : cards) {
            batchArgs.add(new Object[] {
                card.getDeckId(),
                card.getFrontText(),
                card.getBackText(),
                card.getExample(),
                card.getImageUrl(),
                Timestamp.valueOf(timestamp),
                Timestamp.valueOf(timestamp)
            });
        }

        return batchArgs;
    }

    /**
     * Batch updates existing cards.
     *
     * @param jdbcTemplate JDBC template for database operations
     * @param existingCards list of cards to update
     */
    private void batchUpdate(final JdbcTemplate jdbcTemplate, final List<Card> existingCards) {
        long startTime = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();
        List<Object[]> batchArgs = prepareBatchUpdateArgs(existingCards, now);
        jdbcTemplate.batchUpdate(CardSqlQueries.UPDATE_CARD, batchArgs);

        // Log summary after completion (no logging in loop)
        long duration = System.currentTimeMillis() - startTime;
        LOGGER.debug("Batch update completed: {} cards, took {}ms", existingCards.size(), duration);
    }

    /**
     * Prepares batch arguments for update operation.
     *
     * @param cards list of cards
     * @param timestamp timestamp for updated_at
     * @return list of batch arguments
     */
    private List<Object[]> prepareBatchUpdateArgs(final List<Card> cards, final LocalDateTime timestamp) {
        List<Object[]> batchArgs = new ArrayList<>(cards.size());

        for (Card card : cards) {
            batchArgs.add(new Object[] {
                card.getDeckId(),
                card.getFrontText(),
                card.getBackText(),
                card.getExample(),
                card.getImageUrl(),
                Timestamp.valueOf(timestamp),
                card.getId()
            });
        }

        return batchArgs;
    }
}
