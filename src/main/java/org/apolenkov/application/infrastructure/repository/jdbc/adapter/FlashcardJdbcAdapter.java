package org.apolenkov.application.infrastructure.repository.jdbc.adapter;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.infrastructure.repository.jdbc.dto.FlashcardDto;
import org.apolenkov.application.infrastructure.repository.jdbc.exception.FlashcardPersistenceException;
import org.apolenkov.application.infrastructure.repository.jdbc.exception.FlashcardRetrievalException;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.FlashcardSqlQueries;
import org.apolenkov.application.model.Flashcard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JDBC adapter for flashcard repository operations.
 *
 * <p>Implements FlashcardRepository using direct JDBC operations.
 * Provides CRUD operations for flashcards within decks.
 * Active in JDBC profiles only.</p>
 */
@Profile({"dev", "prod"})
@Repository
public class FlashcardJdbcAdapter implements FlashcardRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlashcardJdbcAdapter.class);
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
        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");

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

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates adapter with JdbcTemplate dependency.
     *
     * @param jdbcTemplateValue the JdbcTemplate for database operations
     * @throws IllegalArgumentException if jdbcTemplate is null
     */
    public FlashcardJdbcAdapter(final JdbcTemplate jdbcTemplateValue) {
        if (jdbcTemplateValue == null) {
            throw new IllegalArgumentException("JdbcTemplate cannot be null");
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
     * @return the saved flashcard with updated fields
     * @throws IllegalArgumentException if flashcard is null
     */
    @Override
    @Transactional
    public Flashcard save(final Flashcard flashcard) {
        if (flashcard == null) {
            throw new IllegalArgumentException("Flashcard cannot be null");
        }

        LOGGER.debug("Saving flashcard: {}", flashcard.getFrontText());
        try {
            if (flashcard.getId() == null) {
                return createFlashcard(flashcard);
            } else {
                return updateFlashcard(flashcard);
            }
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
    @Transactional
    public void deleteById(final long id) {
        LOGGER.debug("Deleting flashcard by ID: {}", id);
        try {
            int deleted = jdbcTemplate.update(FlashcardSqlQueries.DELETE_FLASHCARD, id);
            if (deleted == 0) {
                LOGGER.warn("No flashcard found with ID: {}", id);
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
    @Transactional(readOnly = true)
    public long countByDeckId(final long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive");
        }

        LOGGER.debug("Counting flashcards for deck ID: {}", deckId);
        try {
            Long count =
                    jdbcTemplate.queryForObject(FlashcardSqlQueries.COUNT_FLASHCARDS_BY_DECK_ID, Long.class, deckId);
            return count != null ? count : 0L;
        } catch (DataAccessException e) {
            throw new FlashcardRetrievalException("Failed to count flashcards for deck ID: " + deckId, e);
        }
    }

    /**
     * Deletes all flashcards belonging to a specific deck.
     *
     * @param deckId the ID of the deck whose flashcards to delete
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Override
    @Transactional
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
     * @return created flashcard with generated ID
     */
    private Flashcard createFlashcard(final Flashcard flashcard) {
        FlashcardDto flashcardDto = FlashcardDto.forNewFlashcard(
                flashcard.getDeckId(),
                flashcard.getFrontText(),
                flashcard.getBackText(),
                flashcard.getExample(),
                flashcard.getImageUrl());

        // Insert flashcard
        jdbcTemplate.update(
                FlashcardSqlQueries.INSERT_FLASHCARD,
                flashcardDto.deckId(),
                flashcardDto.frontText(),
                flashcardDto.backText(),
                flashcardDto.example(),
                flashcardDto.imageUrl(),
                flashcardDto.timestamps().createdAt(),
                flashcardDto.timestamps().updatedAt());

        // Get generated ID
        Long generatedId = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);

        // Return created flashcard
        FlashcardDto createdDto = FlashcardDto.forExistingFlashcard(
                generatedId,
                flashcardDto.deckId(),
                flashcardDto.frontText(),
                flashcardDto.backText(),
                flashcardDto.example(),
                flashcardDto.imageUrl(),
                flashcardDto.timestamps());

        return toModel(createdDto);
    }

    /**
     * Updates existing flashcard in database.
     *
     * @param flashcard flashcard to update
     * @return updated flashcard
     */
    private Flashcard updateFlashcard(final Flashcard flashcard) {
        // Update flashcard
        jdbcTemplate.update(
                FlashcardSqlQueries.UPDATE_FLASHCARD,
                flashcard.getDeckId(),
                flashcard.getFrontText(),
                flashcard.getBackText(),
                flashcard.getExample(),
                flashcard.getImageUrl(),
                java.time.LocalDateTime.now(), // Update timestamp
                flashcard.getId());

        return flashcard;
    }
}
