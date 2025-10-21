package org.apolenkov.application.service.card;

import jakarta.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apolenkov.application.domain.model.FilterOption;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.domain.usecase.FlashcardUseCase;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.stats.PaginationCountCache;
import org.apolenkov.application.service.stats.event.CacheInvalidationEvent;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for flashcard use cases and business operations.
 */
@Service
public class FlashcardUseCaseService implements FlashcardUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlashcardUseCaseService.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");
    private static final int MAX_LOG_TEXT_LENGTH = 50;
    private static final String CACHE_TYPE = "pagination-count";

    // ==================== Fields ====================

    private final FlashcardRepository flashcardRepository;
    private final Validator validator;
    private final PaginationCountCache paginationCountCache;
    private final ApplicationEventPublisher eventPublisher;

    // ==================== Constructor ====================

    /**
     * Creates a new FlashcardUseCaseService with required dependencies.
     *
     * @param flashcardRepositoryValue the repository for flashcard persistence operations
     * @param validatorValue the validator for flashcard data validation
     * @param paginationCountCacheValue the cache for pagination count queries
     * @param eventPublisherValue the Spring event publisher for cache invalidation events
     */
    public FlashcardUseCaseService(
            final FlashcardRepository flashcardRepositoryValue,
            final Validator validatorValue,
            final PaginationCountCache paginationCountCacheValue,
            final ApplicationEventPublisher eventPublisherValue) {
        this.flashcardRepository = flashcardRepositoryValue;
        this.validator = validatorValue;
        this.paginationCountCache = paginationCountCacheValue;
        this.eventPublisher = eventPublisherValue;
    }

    // ==================== Public API ====================

    /**
     * Returns flashcards belonging to specific deck.
     *
     * @param deckId the ID of the deck to retrieve flashcards for
     * @return a list of flashcards belonging to the specified deck
     */
    @Override
    @Transactional(readOnly = true)
    public List<Flashcard> getFlashcardsByDeckId(final long deckId) {
        return flashcardRepository.findByDeckId(deckId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Flashcard> getFlashcardsWithFilter(
            final long deckId,
            final String searchQuery,
            final FilterOption filterOption,
            final org.springframework.data.domain.Pageable pageable) {
        return flashcardRepository.findFlashcardsWithFilter(deckId, searchQuery, filterOption, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public long countFlashcardsWithFilter(
            final long deckId, final String searchQuery, final FilterOption filterOption) {
        return flashcardRepository.countFlashcardsWithFilter(deckId, searchQuery, filterOption);
    }

    /**
     * Saves flashcard with validation.
     *
     * @param flashcard the flashcard to save
     * @throws IllegalArgumentException if flashcard validation fails
     */
    @Override
    @Transactional
    public void saveFlashcard(final Flashcard flashcard) {
        LOGGER.debug("Saving flashcard: frontText='{}', deckId={}", flashcard.getFrontText(), flashcard.getDeckId());

        var violations = validator.validate(flashcard);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + CoreConstants.SEPARATOR_SPACE + v.getMessage())
                    .collect(Collectors.joining(", "));
            LOGGER.warn("Flashcard validation failed: {}", message);
            throw new IllegalArgumentException("Validation failed: " + message);
        }

        boolean isNew = flashcard.getId() == null;
        flashcardRepository.save(flashcard);

        // Invalidate pagination count cache for this deck
        paginationCountCache.invalidate(flashcard.getDeckId());
        LOGGER.debug("Pagination count cache invalidated after save for deckId={}", flashcard.getDeckId());

        // Publish cache invalidation event for metrics
        CacheInvalidationEvent event = CacheInvalidationEvent.of(
                CACHE_TYPE, flashcard.getDeckId(), isNew ? "flashcard-created" : "flashcard-updated");
        eventPublisher.publishEvent(event);

        // Audit log with explicit action (truncate frontText for readability)
        String frontTextTruncated = truncate(flashcard.getFrontText());
        if (isNew) {
            AUDIT_LOGGER.info(
                    "Flashcard created: cardId={}, deckId={}, front='{}'",
                    flashcard.getId(),
                    flashcard.getDeckId(),
                    frontTextTruncated);
            LOGGER.info("New flashcard created: id={}, front='{}'", flashcard.getId(), frontTextTruncated);
        } else {
            AUDIT_LOGGER.info(
                    "Flashcard updated: cardId={}, deckId={}, front='{}'",
                    flashcard.getId(),
                    flashcard.getDeckId(),
                    frontTextTruncated);
            LOGGER.info("Flashcard updated: id={}, front='{}'", flashcard.getId(), frontTextTruncated);
        }
    }

    /**
     * Deletes flashcard by ID.
     * Logs flashcard details before deletion for audit trail.
     *
     * @param id the unique identifier of the flashcard to delete
     */
    @Override
    @Transactional
    public void deleteFlashcard(final long id) {
        LOGGER.debug("Deleting flashcard with ID: {}", id);

        // Get flashcard info before deletion for audit logging and cache invalidation
        flashcardRepository.findById(id).ifPresent(flashcard -> {
            String frontTextTruncated = truncate(flashcard.getFrontText());
            AUDIT_LOGGER.warn(
                    "Flashcard deleted: cardId={}, deckId={}, front='{}'",
                    id,
                    flashcard.getDeckId(),
                    frontTextTruncated);

            // Invalidate pagination count cache for this deck
            paginationCountCache.invalidate(flashcard.getDeckId());
            LOGGER.debug("Pagination count cache invalidated after delete for deckId={}", flashcard.getDeckId());

            // Publish cache invalidation event for metrics
            CacheInvalidationEvent event =
                    CacheInvalidationEvent.of(CACHE_TYPE, flashcard.getDeckId(), "flashcard-deleted");
            eventPublisher.publishEvent(event);
        });

        flashcardRepository.deleteById(id);

        LOGGER.info("Flashcard deleted successfully: id={}", id);
    }

    /**
     * Returns total number of flashcards in deck.
     *
     * @param deckId the ID of the deck to count flashcards for
     * @return the total number of flashcards in the specified deck
     */
    @Override
    @Transactional(readOnly = true)
    public long countByDeckId(final long deckId) {
        return flashcardRepository.countByDeckId(deckId);
    }

    /**
     * Returns flashcard counts for multiple decks in single database query.
     *
     * @param deckIds collection of deck IDs to count flashcards for
     * @return map of deck ID to flashcard count (empty map if deckIds is empty)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countByDeckIds(final Collection<Long> deckIds) {
        if (deckIds == null || deckIds.isEmpty()) {
            LOGGER.debug("countByDeckIds called with empty collection, returning empty map");
            return Map.of();
        }

        LOGGER.debug("Batch counting flashcards for {} decks", deckIds.size());
        Map<Long, Long> counts = flashcardRepository.countByDeckIds(deckIds);
        LOGGER.debug("Batch count completed: {} decks have flashcards", counts.size());

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
