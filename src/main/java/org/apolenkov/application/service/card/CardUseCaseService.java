package org.apolenkov.application.service.card;

import jakarta.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apolenkov.application.domain.model.FilterOption;
import org.apolenkov.application.domain.port.CardRepository;
import org.apolenkov.application.domain.usecase.CardUseCase;
import org.apolenkov.application.model.Card;
import org.apolenkov.application.service.stats.PaginationCountCache;
import org.apolenkov.application.service.stats.event.CacheInvalidationEvent;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for card use cases and business operations.
 */
@Service
public class CardUseCaseService implements CardUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardUseCaseService.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");
    private static final int MAX_LOG_TEXT_LENGTH = 50;
    private static final String CACHE_TYPE = "pagination-count";

    // ==================== Fields ====================

    private final CardRepository cardRepository;
    private final Validator validator;
    private final PaginationCountCache paginationCountCache;
    private final ApplicationEventPublisher eventPublisher;

    // ==================== Constructor ====================

    /**
     * Creates a new CardUseCaseService with required dependencies.
     *
     * @param cardRepositoryValue the repository for card persistence operations
     * @param validatorValue the validator for card data validation
     * @param paginationCountCacheValue the cache for pagination count queries
     * @param eventPublisherValue the Spring event publisher for cache invalidation events
     */
    public CardUseCaseService(
            final CardRepository cardRepositoryValue,
            final Validator validatorValue,
            final PaginationCountCache paginationCountCacheValue,
            final ApplicationEventPublisher eventPublisherValue) {
        this.cardRepository = cardRepositoryValue;
        this.validator = validatorValue;
        this.paginationCountCache = paginationCountCacheValue;
        this.eventPublisher = eventPublisherValue;
    }

    // ==================== Public API ====================

    /**
     * Returns cards belonging to specific deck.
     *
     * @param deckId the ID of the deck to retrieve cards for
     * @return a list of cards belonging to the specified deck
     */
    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsByDeckId(final long deckId) {
        return cardRepository.findByDeckId(deckId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsWithFilter(
            final long deckId,
            final String searchQuery,
            final FilterOption filterOption,
            final org.springframework.data.domain.Pageable pageable) {
        return cardRepository.findCardsWithFilter(deckId, searchQuery, filterOption, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public long countCardsWithFilter(final long deckId, final String searchQuery, final FilterOption filterOption) {
        return cardRepository.countCardsWithFilter(deckId, searchQuery, filterOption);
    }

    /**
     * Saves card with validation.
     *
     * @param card the card to save
     * @throws IllegalArgumentException if card validation fails
     */
    @Override
    @Transactional
    public void saveCard(final Card card) {
        LOGGER.debug("Saving card: frontText='{}', deckId={}", card.getFrontText(), card.getDeckId());

        var violations = validator.validate(card);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + CoreConstants.SEPARATOR_SPACE + v.getMessage())
                    .collect(Collectors.joining(", "));
            LOGGER.warn("Card validation failed: {}", message);
            throw new IllegalArgumentException("Validation failed: " + message);
        }

        boolean isNew = card.getId() == null;
        cardRepository.save(card);

        // Invalidate pagination count cache for this deck
        paginationCountCache.invalidate(card.getDeckId());
        LOGGER.debug("Pagination count cache invalidated after save for deckId={}", card.getDeckId());

        // Publish cache invalidation event for metrics
        CacheInvalidationEvent event =
                CacheInvalidationEvent.of(CACHE_TYPE, card.getDeckId(), isNew ? "card-created" : "card-updated");
        eventPublisher.publishEvent(event);

        // Audit log with explicit action (truncate frontText for readability)
        String frontTextTruncated = truncate(card.getFrontText());
        if (isNew) {
            AUDIT_LOGGER.info(
                    "Card created: cardId={}, deckId={}, front='{}'",
                    card.getId(),
                    card.getDeckId(),
                    frontTextTruncated);
            LOGGER.info("New card created: id={}, front='{}'", card.getId(), frontTextTruncated);
        } else {
            AUDIT_LOGGER.info(
                    "Card updated: cardId={}, deckId={}, front='{}'",
                    card.getId(),
                    card.getDeckId(),
                    frontTextTruncated);
            LOGGER.info("Card updated: id={}, front='{}'", card.getId(), frontTextTruncated);
        }
    }

    /**
     * Deletes card by ID.
     * Logs card details before deletion for audit trail.
     *
     * @param id the unique identifier of the card to delete
     */
    @Override
    @Transactional
    public void deleteCard(final long id) {
        LOGGER.debug("Deleting card with ID: {}", id);

        // Get card info before deletion for audit logging and cache invalidation
        cardRepository.findById(id).ifPresent(card -> {
            String frontTextTruncated = truncate(card.getFrontText());
            AUDIT_LOGGER.warn(
                    "Card deleted: cardId={}, deckId={}, front='{}'", id, card.getDeckId(), frontTextTruncated);

            // Invalidate pagination count cache for this deck
            paginationCountCache.invalidate(card.getDeckId());
            LOGGER.debug("Pagination count cache invalidated after delete for deckId={}", card.getDeckId());

            // Publish cache invalidation event for metrics
            CacheInvalidationEvent event = CacheInvalidationEvent.of(CACHE_TYPE, card.getDeckId(), "card-deleted");
            eventPublisher.publishEvent(event);
        });

        cardRepository.deleteById(id);

        LOGGER.info("Card deleted successfully: id={}", id);
    }

    /**
     * Returns total number of cards in deck.
     *
     * @param deckId the ID of the deck to count cards for
     * @return the total number of cards in the specified deck
     */
    @Override
    @Transactional(readOnly = true)
    public long countByDeckId(final long deckId) {
        return cardRepository.countByDeckId(deckId);
    }

    /**
     * Returns card counts for multiple decks in single database query.
     *
     * @param deckIds collection of deck IDs to count cards for
     * @return map of deck ID to card count (empty map if deckIds is empty)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countByDeckIds(final Collection<Long> deckIds) {
        if (deckIds == null || deckIds.isEmpty()) {
            LOGGER.debug("countByDeckIds called with empty collection, returning empty map");
            return Map.of();
        }

        LOGGER.debug("Batch counting cards for {} decks", deckIds.size());
        Map<Long, Long> counts = cardRepository.countByDeckIds(deckIds);
        LOGGER.debug("Batch count completed: {} decks have cards", counts.size());

        return counts;
    }

    /**
     * Truncates text to specified maximum length.
     * Adds ellipsis if text was truncated.
     *
     * @param text the text to truncate
     * @return truncated text or original if shorter than maxLength
     */
    private static String truncate(final String text) {
        if (text == null || text.length() <= MAX_LOG_TEXT_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_LOG_TEXT_LENGTH - 3) + "...";
    }
}
