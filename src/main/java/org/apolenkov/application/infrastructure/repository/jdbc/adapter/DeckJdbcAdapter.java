package org.apolenkov.application.infrastructure.repository.jdbc.adapter;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.infrastructure.repository.jdbc.dto.DeckDto;
import org.apolenkov.application.infrastructure.repository.jdbc.exception.DeckPersistenceException;
import org.apolenkov.application.infrastructure.repository.jdbc.exception.DeckRetrievalException;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.DeckSqlQueries;
import org.apolenkov.application.model.Deck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JDBC adapter for deck repository operations.
 *
 * <p>Implements DeckRepository using direct JDBC operations.
 * Provides CRUD operations for flashcard decks.
 * Active in JDBC profiles only.</p>
 */
@Profile({"dev", "prod"})
@Repository
public class DeckJdbcAdapter implements DeckRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckJdbcAdapter.class);
    /**
     * RowMapper for DeckDto.
     */
    private static final RowMapper<DeckDto> DECK_ROW_MAPPER = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        long userId = rs.getLong("user_id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");

        return DeckDto.forExistingDeck(
                id,
                userId,
                title,
                description,
                createdAt != null ? createdAt.toLocalDateTime() : null,
                updatedAt != null ? updatedAt.toLocalDateTime() : null);
    };

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates adapter with JdbcTemplate dependency.
     *
     * @param jdbcTemplateValue the JdbcTemplate for database operations
     * @throws IllegalArgumentException if jdbcTemplate is null
     */
    public DeckJdbcAdapter(final JdbcTemplate jdbcTemplateValue) {
        if (jdbcTemplateValue == null) {
            throw new IllegalArgumentException("JdbcTemplate cannot be null");
        }
        this.jdbcTemplate = jdbcTemplateValue;
    }

    /**
     * Converts DeckDto to domain Deck model.
     *
     * @param deckDto DTO to convert
     * @return corresponding domain model
     */
    private static Deck toModel(final DeckDto deckDto) {
        final Deck deck = new Deck(deckDto.id(), deckDto.userId(), deckDto.title(), deckDto.description());
        deck.setCreatedAt(deckDto.createdAt());
        deck.setUpdatedAt(deckDto.updatedAt());
        return deck;
    }

    /**
     * Retrieves all decks from database.
     *
     * @return list of all decks
     */
    @Override
    @Transactional(readOnly = true)
    public List<Deck> findAll() {
        LOGGER.debug("Retrieving all decks");
        try {
            List<DeckDto> deckDtos = jdbcTemplate.query(DeckSqlQueries.SELECT_ALL_DECKS, DECK_ROW_MAPPER);
            return deckDtos.stream().map(DeckJdbcAdapter::toModel).toList();
        } catch (DataAccessException e) {
            throw new DeckRetrievalException("Failed to retrieve all decks", e);
        }
    }

    /**
     * Retrieves all decks owned by specific user.
     *
     * @param userId ID of user whose decks to retrieve
     * @return list of decks owned by user
     * @throws IllegalArgumentException if userId is invalid
     */
    @Override
    @Transactional(readOnly = true)
    public List<Deck> findByUserId(final long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }

        LOGGER.debug("Retrieving decks for user ID: {}", userId);
        try {
            List<DeckDto> deckDtos =
                    jdbcTemplate.query(DeckSqlQueries.SELECT_DECKS_BY_USER_ID, DECK_ROW_MAPPER, userId);
            return deckDtos.stream().map(DeckJdbcAdapter::toModel).toList();
        } catch (DataAccessException e) {
            throw new DeckRetrievalException("Failed to retrieve decks for user ID: " + userId, e);
        }
    }

    /**
     * Retrieves deck by unique identifier.
     *
     * @param id unique identifier of deck
     * @return Optional containing deck if found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Deck> findById(final long id) {
        LOGGER.debug("Retrieving deck by ID: {}", id);
        try {
            List<DeckDto> decks = jdbcTemplate.query(DeckSqlQueries.SELECT_DECK_BY_ID, DECK_ROW_MAPPER, id);
            if (decks.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(toModel(decks.getFirst()));
        } catch (DataAccessException e) {
            throw new DeckRetrievalException("Failed to retrieve deck by ID: " + id, e);
        }
    }

    /**
     * Saves deck to database.
     *
     * @param deck deck to save
     * @return saved deck with updated fields
     * @throws IllegalArgumentException if deck is null
     */
    @Override
    @Transactional
    public Deck save(final Deck deck) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }

        LOGGER.debug("Saving deck: {}", deck.getTitle());
        try {
            if (deck.getId() == null) {
                return createDeck(deck);
            } else {
                return updateDeck(deck);
            }
        } catch (DataAccessException e) {
            throw new DeckPersistenceException("Failed to save deck: " + deck.getTitle(), e);
        }
    }

    /**
     * Deletes deck by unique identifier.
     *
     * @param id unique identifier of deck to delete
     */
    @Override
    @Transactional
    public void deleteById(final long id) {
        LOGGER.debug("Deleting deck by ID: {}", id);
        try {
            int deleted = jdbcTemplate.update(DeckSqlQueries.DELETE_DECK, id);
            if (deleted == 0) {
                LOGGER.warn("No deck found with ID: {}", id);
            }
        } catch (DataAccessException e) {
            throw new DeckPersistenceException("Failed to delete deck by ID: " + id, e);
        }
    }

    /**
     * Creates new deck in database.
     *
     * @param deck deck to create
     * @return created deck with generated ID
     */
    private Deck createDeck(final Deck deck) {
        DeckDto deckDto = DeckDto.forNewDeck(deck.getUserId(), deck.getTitle(), deck.getDescription());

        // Insert deck
        jdbcTemplate.update(
                DeckSqlQueries.INSERT_DECK,
                deckDto.userId(),
                deckDto.title(),
                deckDto.description(),
                deckDto.createdAt(),
                deckDto.updatedAt());

        // Get generated ID
        Long generatedId = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);

        // Return created deck
        DeckDto createdDto = DeckDto.forExistingDeck(
                generatedId,
                deckDto.userId(),
                deckDto.title(),
                deckDto.description(),
                deckDto.createdAt(),
                deckDto.updatedAt());

        return toModel(createdDto);
    }

    /**
     * Updates existing deck in database.
     *
     * @param deck deck to update
     * @return updated deck
     */
    private Deck updateDeck(final Deck deck) {
        // Update deck
        jdbcTemplate.update(
                DeckSqlQueries.UPDATE_DECK,
                deck.getUserId(),
                deck.getTitle(),
                deck.getDescription(),
                java.time.LocalDateTime.now(), // Update timestamp
                deck.getId());

        return deck;
    }
}
