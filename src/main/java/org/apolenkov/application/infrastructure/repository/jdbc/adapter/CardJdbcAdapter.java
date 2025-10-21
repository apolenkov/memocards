package org.apolenkov.application.infrastructure.repository.jdbc.adapter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apolenkov.application.domain.model.FilterOption;
import org.apolenkov.application.domain.port.CardRepository;
import org.apolenkov.application.infrastructure.repository.jdbc.batch.CardBatchOperations;
import org.apolenkov.application.infrastructure.repository.jdbc.dto.CardDto;
import org.apolenkov.application.infrastructure.repository.jdbc.exception.CardPersistenceException;
import org.apolenkov.application.infrastructure.repository.jdbc.exception.CardRetrievalException;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.CardQueryBuilder;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.CardSqlQueries;
import org.apolenkov.application.model.Card;
import org.apolenkov.application.service.stats.PaginationCountCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * JDBC adapter for card repository operations.
 *
 * <p>Implements CardRepository using direct JDBC operations.
 * Provides CRUD operations for cards within decks.
 * Active in JDBC profiles only.</p>
 */
@Profile({"dev", "prod", "test"})
@Repository
public class CardJdbcAdapter implements CardRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardJdbcAdapter.class);

    // ==================== Row Mappers ====================

    /**
     * RowMapper for CardDto.
     */
    private static final RowMapper<CardDto> CARD_ROW_MAPPER = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        long deckId = rs.getLong("deck_id");
        String frontText = rs.getString("front_text");
        String backText = rs.getString("back_text");
        String example = rs.getString("example");
        String imageUrl = rs.getString("image_url");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return CardDto.forExistingCard(
                id,
                deckId,
                frontText,
                backText,
                example,
                imageUrl,
                new CardDto.CardTimestamps(
                        createdAt != null ? createdAt.toLocalDateTime() : null,
                        updatedAt != null ? updatedAt.toLocalDateTime() : null));
    };

    // ==================== Fields ====================

    private final JdbcTemplate jdbcTemplate;
    private final PaginationCountCache paginationCountCache;

    // ==================== Constructor ====================

    /**
     * Creates adapter with JdbcTemplate dependency.
     *
     * @param jdbcTemplateValue the JdbcTemplate for database operations
     * @param batchOperationsValue helper for batch operations
     * @param paginationCountCacheValue cache for pagination COUNT queries
     * @throws IllegalArgumentException if jdbcTemplate is null
     */
    public CardJdbcAdapter(
            final JdbcTemplate jdbcTemplateValue,
            final CardBatchOperations batchOperationsValue,
            final PaginationCountCache paginationCountCacheValue) {
        if (jdbcTemplateValue == null) {
            throw new IllegalArgumentException("JdbcTemplate cannot be null");
        }
        if (batchOperationsValue == null) {
            throw new IllegalArgumentException("BatchOperations cannot be null");
        }
        if (paginationCountCacheValue == null) {
            throw new IllegalArgumentException("PaginationCountCache cannot be null");
        }
        this.jdbcTemplate = jdbcTemplateValue;
        this.paginationCountCache = paginationCountCacheValue;
    }

    /**
     * Converts CardDto to domain Card model.
     *
     * @param cardDto DTO to convert
     * @return corresponding domain model
     */
    private static Card toModel(final CardDto cardDto) {
        final Card card = new Card(cardDto.id(), cardDto.deckId(), cardDto.frontText(), cardDto.backText());
        card.setExample(cardDto.example());
        card.setImageUrl(cardDto.imageUrl());
        card.setCreatedAt(cardDto.timestamps().createdAt());
        card.setUpdatedAt(cardDto.timestamps().updatedAt());
        return card;
    }

    /**
     * Retrieves all cards belonging to a specific deck.
     *
     * @param deckId the ID of the deck whose cards to retrieve
     * @return list of cards in the deck
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Override
    public List<Card> findByDeckId(final long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive");
        }

        LOGGER.debug("Retrieving cards for deck ID: {}", deckId);
        try {
            List<CardDto> cardDtos =
                    jdbcTemplate.query(CardSqlQueries.SELECT_CARDS_BY_DECK_ID, CARD_ROW_MAPPER, deckId);
            return cardDtos.stream().map(CardJdbcAdapter::toModel).toList();
        } catch (DataAccessException e) {
            throw new CardRetrievalException("Failed to retrieve cards for deck ID: " + deckId, e);
        }
    }

    /**
     * Retrieves a card by its unique identifier.
     *
     * @param id the unique identifier of the card
     * @return Optional containing the card if found
     */
    @Override
    public Optional<Card> findById(final long id) {
        LOGGER.debug("Retrieving card by ID: {}", id);
        try {
            List<CardDto> cards = jdbcTemplate.query(CardSqlQueries.SELECT_CARD_BY_ID, CARD_ROW_MAPPER, id);
            if (cards.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(toModel(cards.getFirst()));
        } catch (DataAccessException e) {
            throw new CardRetrievalException("Failed to retrieve card by ID: " + id, e);
        }
    }

    /**
     * Saves a card to the database.
     *
     * @param card the card to save
     * @throws IllegalArgumentException if card is null
     */
    @Override
    public void save(final Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }

        boolean isNew = card.getId() == null;
        LOGGER.debug("Saving card: frontText='{}', isNew={}", card.getFrontText(), isNew);

        try {
            if (isNew) {
                createCard(card);
            } else {
                updateCard(card);
            }
            LOGGER.debug("Card saved: id={}, frontText='{}', isNew={}", card.getId(), card.getFrontText(), isNew);
        } catch (DataAccessException e) {
            throw new CardPersistenceException("Failed to save card: " + card.getFrontText(), e);
        }
    }

    /**
     * Deletes a card by its unique identifier.
     *
     * @param id the unique identifier of the card to delete
     */
    @Override
    public void deleteById(final long id) {
        LOGGER.debug("Deleting card by ID: {}", id);
        try {
            int deleted = jdbcTemplate.update(CardSqlQueries.DELETE_CARD, id);
            if (deleted == 0) {
                LOGGER.warn("No card found with ID: {}", id);
            } else {
                LOGGER.debug("Card deleted from database: id={}", id);
            }
        } catch (DataAccessException e) {
            throw new CardPersistenceException("Failed to delete card by ID: " + id, e);
        }
    }

    /**
     * Counts the total number of cards in a specific deck.
     *
     * @param deckId the ID of the deck to count cards for
     * @return the total number of cards in the deck
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Override
    public long countByDeckId(final long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive");
        }

        LOGGER.debug("Counting cards for deck ID: {}", deckId);
        try {
            return jdbcTemplate
                    .query(CardSqlQueries.COUNT_CARDS_BY_DECK_ID, (rs, rowNum) -> rs.getLong(1), deckId)
                    .stream()
                    .findFirst()
                    .orElse(0L);
        } catch (DataAccessException e) {
            throw new CardRetrievalException("Failed to count cards for deck ID: " + deckId, e);
        }
    }

    /**
     * Counts cards for multiple decks.
     *
     * @param deckIds collection of deck identifiers (non-null, may be empty)
     * @return map of deck ID to card count (decks with zero cards may be absent)
     */
    @Override
    public Map<Long, Long> countByDeckIds(final Collection<Long> deckIds) {
        if (deckIds == null || deckIds.isEmpty()) {
            return Map.of();
        }

        LOGGER.debug("Batch counting cards for {} decks using single query", deckIds.size());
        try {
            String sql = buildInClauseSql(deckIds.size());
            Map<Long, Long> counts =
                    jdbcTemplate.query(sql, ps -> setLongParameters(ps, deckIds), this::extractCountsByDeck);

            Map<Long, Long> safeCounts = (counts != null) ? counts : Map.of();
            LOGGER.debug(
                    "Batch count completed: {} decks have cards (out of {} requested)",
                    safeCounts.size(),
                    deckIds.size());
            return safeCounts;

        } catch (DataAccessException e) {
            throw new CardRetrievalException("Failed to count cards for deck IDs: " + deckIds, e);
        }
    }

    /**
     * Deletes all cards belonging to a specific deck.
     *
     * @param deckId the ID of the deck whose cards to delete
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Override
    public void deleteByDeckId(final long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive");
        }

        LOGGER.debug("Deleting all cards for deck ID: {}", deckId);
        try {
            int deleted = jdbcTemplate.update(CardSqlQueries.DELETE_CARDS_BY_DECK_ID, deckId);
            LOGGER.debug("Deleted {} cards for deck ID: {}", deleted, deckId);
        } catch (DataAccessException e) {
            throw new CardPersistenceException("Failed to delete cards for deck ID: " + deckId, e);
        }
    }

    /**
     * Creates new card in database.
     *
     * @param card card to create
     */
    private void createCard(final Card card) {
        CardDto cardDto = CardDto.forNewCard(
                card.getDeckId(), card.getFrontText(), card.getBackText(), card.getExample(), card.getImageUrl());

        Long generatedId = jdbcTemplate.queryForObject(
                CardSqlQueries.INSERT_CARD_RETURNING_ID,
                Long.class,
                cardDto.deckId(),
                cardDto.frontText(),
                cardDto.backText(),
                cardDto.example(),
                cardDto.imageUrl(),
                cardDto.timestamps().createdAt(),
                cardDto.timestamps().updatedAt());

        card.setId(generatedId);
    }

    /**
     * Updates existing card in database.
     *
     * @param card card to update
     */
    private void updateCard(final Card card) {
        jdbcTemplate.update(
                CardSqlQueries.UPDATE_CARD,
                card.getDeckId(),
                card.getFrontText(),
                card.getBackText(),
                card.getExample(),
                card.getImageUrl(),
                LocalDateTime.now(),
                card.getId());
    }

    /**
     * Builds SQL with IN clause placeholders.
     *
     * @param paramCount number of parameters for IN clause
     * @return SQL with placeholders
     */
    private String buildInClauseSql(final int paramCount) {
        String placeholders = "?,".repeat(Math.max(0, paramCount - 1)) + "?";
        return String.format(CardSqlQueries.COUNT_CARDS_BY_DECK_IDS_TEMPLATE, placeholders);
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
     * Extracts card counts by deck from ResultSet.
     *
     * @param rs ResultSet to extract from
     * @return map of deck ID to card count
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
    public List<Card> findCardsWithFilter(
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
                "Finding cards with dynamic filter: deckId={}, searchQuery='{}', filterOption={}, limit={}, offset={}",
                deckId,
                searchQuery,
                filterOption,
                limit,
                offset);

        try {
            CardQueryBuilder queryBuilder = new CardQueryBuilder().withDeckId(deckId);

            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                queryBuilder.withSearchQuery(searchQuery);
            }

            if (filterOption == FilterOption.KNOWN_ONLY) {
                queryBuilder.withKnownStatus();
            } else if (filterOption == FilterOption.UNKNOWN_ONLY) {
                queryBuilder.withUnknownStatus();
            }

            String sql = queryBuilder.buildSelectQueryWithPagination(CardSqlQueries.SELECT_CARDS_BASE);
            Object[] params = queryBuilder.getParametersWithPagination(limit, offset);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Dynamic SQL: {} | Parameters: {}", sql, java.util.Arrays.toString(params));
            }

            List<CardDto> cardDtos = jdbcTemplate.query(sql, CARD_ROW_MAPPER, params);
            return cardDtos.stream().map(CardJdbcAdapter::toModel).toList();
        } catch (DataAccessException e) {
            throw new CardRetrievalException("Failed to find cards with dynamic filter for deck ID: " + deckId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countCardsWithFilter(final long deckId, final String searchQuery, final FilterOption filterOption) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive");
        }

        LOGGER.debug(
                "Counting cards with dynamic filter: deckId={}, searchQuery='{}', filterOption={}",
                deckId,
                searchQuery,
                filterOption);

        // Use cache for COUNT queries (30 sec TTL + event-driven invalidation)
        return paginationCountCache.getCount(deckId, searchQuery, filterOption, () -> {
            try {
                CardQueryBuilder queryBuilder = new CardQueryBuilder().withDeckId(deckId);

                if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                    queryBuilder.withSearchQuery(searchQuery);
                }

                if (filterOption == FilterOption.KNOWN_ONLY) {
                    queryBuilder.withKnownStatus();
                } else if (filterOption == FilterOption.UNKNOWN_ONLY) {
                    queryBuilder.withUnknownStatus();
                }

                String sql = queryBuilder.buildCountQuery(CardSqlQueries.COUNT_CARDS_BASE);
                Object[] params = queryBuilder.getParameters();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Dynamic COUNT SQL: {} | Parameters: {}", sql, java.util.Arrays.toString(params));
                }

                Long count = jdbcTemplate.queryForObject(sql, Long.class, params);
                return count != null ? count : 0L;
            } catch (DataAccessException e) {
                throw new CardRetrievalException("Failed to count cards with dynamic filter for deck ID: " + deckId, e);
            }
        });
    }
}
