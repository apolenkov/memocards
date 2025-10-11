package org.apolenkov.application.service.stats;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apolenkov.application.domain.dto.SessionStatsDto;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.domain.usecase.StatsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for statistics use cases and business operations.
 */
@Service
public class StatsService implements StatsUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsService.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

    private final StatsRepository statsRepository;

    /**
     * Constructs StatsService with required dependencies.
     *
     * @param statsRepositoryValue repository for statistics operations
     */
    public StatsService(final StatsRepository statsRepositoryValue) {
        this.statsRepository = statsRepositoryValue;
    }

    /**
     * Records practice session and updates daily statistics.
     *
     * @param sessionData session data containing all required parameters
     * @throws IllegalArgumentException if any parameter violates constraints
     */
    @Override
    @Transactional
    public void recordSession(final SessionStatsDto sessionData) {
        LOGGER.debug(
                "Recording session: deckId={}, viewed={}, correct={}, hard={}",
                sessionData.deckId(),
                sessionData.viewed(),
                sessionData.correct(),
                sessionData.hard());

        // Early return if no cards were viewed (including negative values)
        if (sessionData.viewed() <= 0) {
            LOGGER.warn("Skipped recording session with invalid viewed count: {}", sessionData.viewed());
            return;
        }

        LocalDate today = LocalDate.now();
        statsRepository.appendSession(sessionData, today);

        LOGGER.info(
                "Session recorded: deckId={}, viewed={}, correct={}, hard={}, durationMs={}, knownDelta={}",
                sessionData.deckId(),
                sessionData.viewed(),
                sessionData.correct(),
                sessionData.hard(),
                sessionData.sessionDurationMs(),
                sessionData.knownCardIdsDelta() != null
                        ? sessionData.knownCardIdsDelta().size()
                        : 0);
    }

    /**
     * Calculates progress percentage for deck based on known cards (0-100%).
     *
     * @param deckId ID of deck to calculate progress for
     * @param deckSize total number of cards in deck
     * @return progress percentage (0-100), or 0 if deck size is invalid
     */
    @Override
    @Transactional(readOnly = true)
    public int getDeckProgressPercent(final long deckId, final int deckSize) {
        // Handle edge case: invalid deck size
        if (deckSize <= 0) {
            return 0;
        }

        // Calculate percentage of known cards
        int known = statsRepository.getKnownCardIds(deckId).size();
        int percent = (int) Math.round(100.0 * known / deckSize);

        // Clamp percentage to valid range [0, 100]
        if (percent < 0) {
            percent = 0;
        }
        if (percent > 100) {
            percent = 100;
        }
        return percent;
    }

    /**
     * Checks if specific card is marked as known in deck.
     *
     * @param deckId ID of deck containing the card
     * @param cardId ID of card to check
     * @return true if card is marked as known
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isCardKnown(final long deckId, final long cardId) {
        return statsRepository.getKnownCardIds(deckId).contains(cardId);
    }

    /**
     * Retrieves all card IDs marked as known in specific deck.
     *
     * @param deckId ID of deck to retrieve known cards for
     * @return set of card IDs marked as known
     */
    @Override
    @Transactional(readOnly = true)
    public Set<Long> getKnownCardIds(final long deckId) {
        return statsRepository.getKnownCardIds(deckId);
    }

    /**
     * Retrieves known card IDs for multiple decks in single database query.
     *
     * @param deckIds collection of deck IDs to retrieve known cards for
     * @return map of deck ID to set of known card IDs (empty map if deckIds is empty)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Long, Set<Long>> getKnownCardIdsBatch(final Collection<Long> deckIds) {
        if (deckIds == null || deckIds.isEmpty()) {
            LOGGER.debug("getKnownCardIdsBatch called with empty collection, returning empty map");
            return Map.of();
        }

        LOGGER.debug("Batch retrieving known cards for {} decks", deckIds.size());
        Map<Long, Set<Long>> result = statsRepository.getKnownCardIdsBatch(deckIds);
        LOGGER.debug("Batch retrieval completed: {} decks have known cards", result.size());

        return result;
    }

    /**
     * Sets knowledge status of specific card in deck.
     * Updates knowledge status of card, marking it as either known or unknown
     * based on user's performance and feedback.
     *
     * @param deckId ID of deck containing the card
     * @param cardId ID of card to update
     * @param known true to mark card as known, false to mark as unknown
     */
    @Override
    @Transactional
    public void setCardKnown(final long deckId, final long cardId, final boolean known) {
        LOGGER.debug("Setting card {} as {} for deck {}", cardId, known ? "KNOWN" : "UNKNOWN", deckId);

        statsRepository.setCardKnown(deckId, cardId, known);

        LOGGER.info("Card marked as {} in deck {}: cardId={}", known ? "known" : "unknown", deckId, cardId);
    }

    /**
     * Resets all progress for specific deck.
     * Removes all known card associations and resets daily statistics.
     *
     * @param deckId ID of deck to reset progress for
     */
    @Override
    @Transactional
    public void resetDeckProgress(final long deckId) {
        LOGGER.debug("Resetting progress for deck: {}", deckId);

        int knownCardsBefore = statsRepository.getKnownCardIds(deckId).size();

        statsRepository.resetDeckProgress(deckId);

        AUDIT_LOGGER.warn("Deck progress reset: deckId={}, knownCardsCleared={}", deckId, knownCardsBefore);
        LOGGER.info("Deck progress reset successfully: deckId={}, cleared {} known cards", deckId, knownCardsBefore);
    }

    /**
     * Retrieves aggregated statistics for multiple decks.
     * Provides summary information for dashboard and progress tracking.
     *
     * @param deckIds list of deck IDs to aggregate statistics for
     * @return map of deck ID to aggregated statistics, never null
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Long, StatsRepository.DeckAggregate> getDeckAggregates(final List<Long> deckIds) {
        if (deckIds == null || deckIds.isEmpty()) {
            return Map.of();
        }
        return statsRepository.getAggregatesForDecks(deckIds, LocalDate.now());
    }
}
