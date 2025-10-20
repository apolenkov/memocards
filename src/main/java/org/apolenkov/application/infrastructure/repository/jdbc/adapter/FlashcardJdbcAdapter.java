package org.apolenkov.application.infrastructure.repository.jdbc.adapter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apolenkov.application.domain.model.FilterOption;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.infrastructure.repository.jdbc.batch.FlashcardBatchOperations;
import org.apolenkov.application.infrastructure.repository.jdbc.dto.FlashcardDto;
import org.apolenkov.application.infrastructure.repository.jdbc.exception.FlashcardPersistenceException;
import org.apolenkov.application.infrastructure.repository.jdbc.exception.FlashcardRetrievalException;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.FlashcardQueryBuilder;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.FlashcardSqlQueries;
import org.apolenkov.application.model.Flashcard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * JDBC adapter for flashcard repository operations.
 *
 * <p>Implements FlashcardRepository using direct JDBC operations.
 * Provides CRUD operations for flashcards within decks.
 * Active in JDBC profiles only.</p>
 */
@Profile({"dev", "prod", "test"})
@Repository
public class FlashcardJdbcAdapter implements FlashcardRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlashcardJdbcAdapter.class);

    // ==================== Row Mappers ====================

    /**
     * RowMapper for FlashcardDto.
     */
    private static final RowMapper<FlashcardDto> FLASHCARD_ROW_MAPPER = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        long deckId = rs.getLong("deck_id");
        String frontText = rs.getString("front_text");
        String backText = rs.getString("back_text");
        String example = rs.getString("example");
        String imageUrl = rs.getString("image_url");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return FlashcardDto.forExistingFlashcard(
                id,
                deckId,
                frontText,
                backText,
                example,
                imageUrl,
                new FlashcardDto.FlashcardTimestamps(
                        createdAt != null ? createdAt.toLocalDateTime() : null,
                        updatedAt != null ? updatedAt.toLocalDateTime() : null));
    };

    // ==================== Fields ====================

    private final JdbcTemplate jdbcTemplate;

    // ==================== Constructor ====================

    /**
     * Creates adapter with JdbcTemplate dependency.
     *
     * @param jdbcTemplateValue the JdbcTemplate for database operations
     * @param batchOperationsValue helper for batch operations
     * @throws IllegalArgumentException if jdbcTemplate is null
     */
    public FlashcardJdbcAdapter(
            final JdbcTemplate jdbcTemplateValue, final FlashcardBatchOperations batchOperationsValue) {
        if (jdbcTemplateValue == null) {
            throw new IllegalArgumentException("JdbcTemplate cannot be null");
        }
        if (batchOperationsValue == null) {
            throw new IllegalArgumentException("BatchOperations cannot be null");
        }
        this.jdbcTemplate = jdbcTemplateValue;
    }

    /**
     * Converts FlashcardDto to domain Flashcard model.
     *
     * @param flashcardDto DTO to convert
     * @return corresponding domain model
     */
    private static Flashcard toModel(final FlashcardDto flashcardDto) {
        final Flashcard flashcard = new Flashcard(
                flashcardDto.id(), flashcardDto.deckId(), flashcardDto.frontText(), flashcardDto.backText());
        flashcard.setExample(flashcardDto.example());
        flashcard.setImageUrl(flashcardDto.imageUrl());
        flashcard.setCreatedAt(flashcardDto.timestamps().createdAt());
        flashcard.setUpdatedAt(flashcardDto.timestamps().updatedAt());
        return flashcard;
    }

    /**
     * Retrieves all flashcards belonging to a specific deck.
     *
     * @param deckId the ID of the deck whose flashcards to retrieve
     * @return list of flashcards in the deck
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Override
    public List<Flashcard> findByDeckId(final long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive");
        }

        LOGGER.debug("Retrieving flashcards for deck ID: {}", deckId);
        try {
            List<FlashcardDto> flashcardDtos =
                    jdbcTemplate.query(FlashcardSqlQueries.SELECT_FLASHCARDS_BY_DECK_ID, FLASHCARD_ROW_MAPPER, deckId);
            return flashcardDtos.stream().map(FlashcardJdbcAdapter::toModel).toList();
        } catch (DataAccessException e) {
            throw new FlashcardRetrievalException("Failed to retrieve flashcards for deck ID: " + deckId, e);
        }
    }

    /**
     * Retrieves a flashcard by its unique identifier.
     *
     * @param id the unique identifier of the flashcard
     * @return Optional containing the flashcard if found
     */
    @Override
    public Optional<Flashcard> findById(final long id) {
        LOGGER.debug("Retrieving flashcard by ID: {}", id);
        try {
            List<FlashcardDto> flashcards =
                    jdbcTemplate.query(FlashcardSqlQueries.SELECT_FLASHCARD_BY_ID, FLASHCARD_ROW_MAPPER, id);
            if (flashcards.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(toModel(flashcards.getFirst()));
        } catch (DataAccessException e) {
            throw new FlashcardRetrievalException("Failed to retrieve flashcard by ID: " + id, e);
        }
    }

    /**
     * Saves a flashcard to the database.
     *
     * @param flashcard the flashcard to save
     * @throws IllegalArgumentException if flashcard is null
     */
    @Override
    public void save(final Flashcard flashcard) {
        if (flashcard == null) {
            throw new IllegalArgumentException("Flashcard cannot be null");
        }

        boolean isNew = flashcard.getId() == null;
        LOGGER.debug("Saving flashcard: frontText='{}', isNew={}", flashcard.getFrontText(), isNew);

        try {
            if (isNew) {
                createFlashcard(flashcard);
            } else {
                updateFlashcard(flashcard);
            }
            LOGGER.debug(
                    "Flashcard saved: id={}, frontText='{}', isNew={}",
                    flashcard.getId(),
                    flashcard.getFrontText(),
                    isNew);
        } catch (DataAccessException e) {
            throw new FlashcardPersistenceException("Failed to save flashcard: " + flashcard.getFrontText(), e);
        }
    }

    /**
     * Deletes a flashcard by its unique identifier.
     *
     * @param id the unique identifier of the flashcard to delete
     */
    @Override
    public void deleteById(final long id) {
        LOGGER.debug("Deleting flashcard by ID: {}", id);
        try {
            int deleted = jdbcTemplate.update(FlashcardSqlQueries.DELETE_FLASHCARD, id);
            if (deleted == 0) {
                LOGGER.warn("No flashcard found with ID: {}", id);
            } else {
                LOGGER.debug("Flashcard deleted from database: id={}", id);
            }
        } catch (DataAccessException e) {
            throw new FlashcardPersistenceException("Failed to delete flashcard by ID: " + id, e);
        }
    }

    /**
     * Counts the total number of flashcards in a specific deck.
     *
     * @param deckId the ID of the deck to count flashcards for
     * @return the total number of flashcards in the deck
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Override
    public long countByDeckId(final long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive");
        }

        LOGGER.debug("Counting flashcards for deck ID: {}", deckId);
        try {
            return jdbcTemplate
                    .query(FlashcardSqlQueries.COUNT_FLASHCARDS_BY_DECK_ID, (rs, rowNum) -> rs.getLong(1), deckId)
                    .stream()
                    .findFirst()
                    .orElse(0L);
        } catch (DataAccessException e) {
            throw new FlashcardRetrievalException("Failed to count flashcards for deck ID: " + deckId, e);
        }
    }

    /**
     * Counts flashcards for multiple decks.
     *
     * @param deckIds collection of deck identifiers (non-null, may be empty)
     * @return map of deck ID to flashcard count (decks with zero flashcards may be absent)
     */
    @Override
    public Map<Long, Long> countByDeckIds(final Collection<Long> deckIds) {
        if (deckIds == null || deckIds.isEmpty()) {
            return Map.of();
        }

        LOGGER.debug("Batch counting flashcards for {} decks using single query", deckIds.size());
        try {
            String sql = buildInClauseSql(deckIds.size());
            Map<Long, Long> counts =
                    jdbcTemplate.query(sql, ps -> setLongParameters(ps, deckIds), this::extractCountsByDeck);

            Map<Long, Long> safeCounts = (counts != null) ? counts : Map.of();
            LOGGER.debug(
                    "Batch count completed: {} decks have flashcards (out of {} requested)",
                    safeCounts.size(),
                    deckIds.size());
            return safeCounts;

        } catch (DataAccessException e) {
            throw new FlashcardRetrievalException("Failed to count flashcards for deck IDs: " + deckIds, e);
        }
    }

    /**
     * Deletes all flashcards belonging to a specific deck.
     *
     * @param deckId the ID of the deck whose flashcards to delete
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Override
    public void deleteByDeckId(final long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive");
        }

        LOGGER.debug("Deleting all flashcards for deck ID: {}", deckId);
        try {
            int deleted = jdbcTemplate.update(FlashcardSqlQueries.DELETE_FLASHCARDS_BY_DECK_ID, deckId);
            LOGGER.debug("Deleted {} flashcards for deck ID: {}", deleted, deckId);
        } catch (DataAccessException e) {
            throw new FlashcardPersistenceException("Failed to delete flashcards for deck ID: " + deckId, e);
        }
    }

    /**
     * Creates new flashcard in database.
     *
     * @param flashcard flashcard to create
     */
    private void createFlashcard(final Flashcard flashcard) {
        FlashcardDto flashcardDto = FlashcardDto.forNewFlashcard(
                flashcard.getDeckId(),
                flashcard.getFrontText(),
                flashcard.getBackText(),
                flashcard.getExample(),
                flashcard.getImageUrl());

        // Insert flashcard and get generated ID using RETURNING clause
        Long generatedId = jdbcTemplate.queryForObject(
                FlashcardSqlQueries.INSERT_FLASHCARD_RETURNING_ID,
                Long.class,
                flashcardDto.deckId(),
                flashcardDto.frontText(),
                flashcardDto.backText(),
                flashcardDto.example(),
                flashcardDto.imageUrl(),
                flashcardDto.timestamps().createdAt(),
                flashcardDto.timestamps().updatedAt());

        // Set generated ID on the flashcard object
        flashcard.setId(generatedId);
    }

    /**
     * Updates existing flashcard in database.
     *
     * @param flashcard flashcard to update
     */
    private void updateFlashcard(final Flashcard flashcard) {
        // Update flashcard
        jdbcTemplate.update(
                FlashcardSqlQueries.UPDATE_FLASHCARD,
                flashcard.getDeckId(),
                flashcard.getFrontText(),
                flashcard.getBackText(),
                flashcard.getExample(),
                flashcard.getImageUrl(),
                LocalDateTime.now(), // Update timestamp
                flashcard.getId());
    }

    /**
     * Builds SQL with IN clause placeholders.
     *
     * @param paramCount number of parameters for IN clause
     * @return SQL with placeholders
     */
    private String buildInClauseSql(final int paramCount) {
        String placeholders = "?,".repeat(Math.max(0, paramCount - 1)) + "?";
        return String.format(FlashcardSqlQueries.COUNT_FLASHCARDS_BY_DECK_IDS_TEMPLATE, placeholders);
    }

    /**
     * Sets long parameters in PreparedStatement.
     *
     * @param ps PreparedStatement to set parameters in
     * @param values collection of long values
     */
    private void setLongParameters(final java.sql.PreparedStatement ps, final Collection<Long> values)
            throws java.sql.SQLException {
        int index = 1;
        for (Long value : values) {
            ps.setLong(index++, value);
        }
    }

    /**
     * Extracts flashcard counts by deck from ResultSet.
     *
     * @param rs ResultSet to extract from
     * @return map of deck ID to flashcard count
     */
    private Map<Long, Long> extractCountsByDeck(final java.sql.ResultSet rs) throws java.sql.SQLException {
        Map<Long, Long> results = new java.util.HashMap<>();
        while (rs.next()) {
            Long deckId = rs.getLong("deck_id");
            Long count = rs.getLong("count");
            results.put(deckId, count);
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Flashcard> findFlashcardsWithFilter(
            final long deckId,
            final String searchQuery,
            final FilterOption filterOption,
            final org.springframework.data.domain.Pageable pageable) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }

        int limit = pageable.getPageSize();
        long offset = pageable.getOffset();

        LOGGER.debug(
                "Finding flashcards with dynamic filter: deckId={}, searchQuery='{}', filterOption={}, limit={}, offset={}",
                deckId,
                searchQuery,
                filterOption,
                limit,
                offset);

        try {
            // Build dynamic query
            FlashcardQueryBuilder queryBuilder = new FlashcardQueryBuilder().withDeckId(deckId);

            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                queryBuilder.withSearchQuery(searchQuery);
            }

            if (filterOption == FilterOption.KNOWN_ONLY) {
                queryBuilder.withKnownStatus();
            } else if (filterOption == FilterOption.UNKNOWN_ONLY) {
                queryBuilder.withUnknownStatus();
            }

            String sql = queryBuilder.buildSelectQueryWithPagination(FlashcardSqlQueries.SELECT_FLASHCARDS_BASE);
            Object[] params = queryBuilder.getParametersWithPagination(limit, offset);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Dynamic SQL: {} | Parameters: {}", sql, java.util.Arrays.toString(params));
            }

            List<FlashcardDto> flashcardDtos = jdbcTemplate.query(sql, FLASHCARD_ROW_MAPPER, params);
            return flashcardDtos.stream().map(FlashcardJdbcAdapter::toModel).toList();
        } catch (DataAccessException e) {
            throw new FlashcardRetrievalException(
                    "Failed to find flashcards with dynamic filter for deck ID: " + deckId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countFlashcardsWithFilter(
            final long deckId, final String searchQuery, final FilterOption filterOption) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive");
        }

        LOGGER.debug(
                "Counting flashcards with dynamic filter: deckId={}, searchQuery='{}', filterOption={}",
                deckId,
                searchQuery,
                filterOption);

        try {
            // Build dynamic query
            FlashcardQueryBuilder queryBuilder = new FlashcardQueryBuilder().withDeckId(deckId);

            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                queryBuilder.withSearchQuery(searchQuery);
            }

            if (filterOption == FilterOption.KNOWN_ONLY) {
                queryBuilder.withKnownStatus();
            } else if (filterOption == FilterOption.UNKNOWN_ONLY) {
                queryBuilder.withUnknownStatus();
            }

            String sql = queryBuilder.buildCountQuery(FlashcardSqlQueries.COUNT_FLASHCARDS_BASE);
            Object[] params = queryBuilder.getParameters();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Dynamic COUNT SQL: {} | Parameters: {}", sql, java.util.Arrays.toString(params));
            }

            Long count = jdbcTemplate.queryForObject(sql, Long.class, params);
            return count != null ? count : 0L;
        } catch (DataAccessException e) {
            throw new FlashcardRetrievalException(
                    "Failed to count flashcards with dynamic filter for deck ID: " + deckId, e);
        }
    }
}
