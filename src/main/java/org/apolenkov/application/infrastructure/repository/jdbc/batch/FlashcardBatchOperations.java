package org.apolenkov.application.infrastructure.repository.jdbc.batch;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.FlashcardSqlQueries;
import org.apolenkov.application.model.Flashcard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Helper class for batch operations on flashcards.
 * Separates batch logic from adapter for better maintainability.
 */
@Component
public class FlashcardBatchOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlashcardBatchOperations.class);
    private static final int DEFAULT_BATCH_SIZE = 1000;

    /**
     * Performs batch save operations for flashcards.
     *
     * @param jdbcTemplate JDBC template for database operations
     * @param flashcards collection of flashcards to save
     */
    public void saveAll(final JdbcTemplate jdbcTemplate, final Collection<Flashcard> flashcards) {
        if (flashcards == null || flashcards.isEmpty()) {
            return;
        }

        LOGGER.debug("Batch saving {} flashcards", flashcards.size());

        // Separate new cards from updates
        List<Flashcard> newCards =
                flashcards.stream().filter(f -> f.getId() == null).toList();

        List<Flashcard> existingCards =
                flashcards.stream().filter(f -> f.getId() != null).toList();

        // Batch insert new cards
        if (!newCards.isEmpty()) {
            batchInsert(jdbcTemplate, newCards);
        }

        // Batch update existing cards
        if (!existingCards.isEmpty()) {
            batchUpdate(jdbcTemplate, existingCards);
        }

        LOGGER.debug("Batch saved {} flashcards successfully", flashcards.size());
    }

    /**
     * Batch inserts new flashcards in chunks for optimal performance.
     *
     * @param jdbcTemplate JDBC template for database operations
     * @param newCards list of new flashcards
     */
    private void batchInsert(final JdbcTemplate jdbcTemplate, final List<Flashcard> newCards) {
        LocalDateTime now = LocalDateTime.now();

        // Process in chunks to avoid memory issues
        for (int i = 0; i < newCards.size(); i += DEFAULT_BATCH_SIZE) {
            int end = Math.min(i + DEFAULT_BATCH_SIZE, newCards.size());
            List<Flashcard> chunk = newCards.subList(i, end);

            List<Object[]> batchArgs = prepareBatchInsertArgs(chunk, now);
            jdbcTemplate.batchUpdate(FlashcardSqlQueries.INSERT_FLASHCARD_RETURNING_ID, batchArgs);

            logProgress(i, newCards.size());
        }
    }

    /**
     * Prepares batch arguments for insert operation.
     *
     * @param cards list of flashcards
     * @param timestamp timestamp for created_at and updated_at
     * @return list of batch arguments
     */
    private List<Object[]> prepareBatchInsertArgs(final List<Flashcard> cards, final LocalDateTime timestamp) {
        List<Object[]> batchArgs = new ArrayList<>(cards.size());

        for (Flashcard card : cards) {
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
     * Batch updates existing flashcards.
     *
     * @param jdbcTemplate JDBC template for database operations
     * @param existingCards list of flashcards to update
     */
    private void batchUpdate(final JdbcTemplate jdbcTemplate, final List<Flashcard> existingCards) {
        LocalDateTime now = LocalDateTime.now();
        List<Object[]> batchArgs = prepareBatchUpdateArgs(existingCards, now);
        jdbcTemplate.batchUpdate(FlashcardSqlQueries.UPDATE_FLASHCARD, batchArgs);
    }

    /**
     * Prepares batch arguments for update operation.
     *
     * @param cards list of flashcards
     * @param timestamp timestamp for updated_at
     * @return list of batch arguments
     */
    private List<Object[]> prepareBatchUpdateArgs(final List<Flashcard> cards, final LocalDateTime timestamp) {
        List<Object[]> batchArgs = new ArrayList<>(cards.size());

        for (Flashcard card : cards) {
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

    /**
     * Logs progress for large batch operations.
     *
     * @param processed number of processed items
     * @param total total number of items
     */
    private void logProgress(final int processed, final int total) {
        if ((processed / DEFAULT_BATCH_SIZE) % 10 == 0) {
            LOGGER.debug("Processed {}/{} flashcards", Math.min(processed + DEFAULT_BATCH_SIZE, total), total);
        }
    }
}
