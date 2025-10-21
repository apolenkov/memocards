package org.apolenkov.application.views.practice.business;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.domain.dto.SessionStatsDto;
import org.apolenkov.application.domain.usecase.CardUseCase;
import org.apolenkov.application.domain.usecase.DeckUseCase;
import org.apolenkov.application.model.Card;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.PracticeDirection;
import org.apolenkov.application.service.settings.PracticeSettingsService;
import org.apolenkov.application.service.stats.StatsService;
import org.springframework.stereotype.Component;

/**
 * Service for managing practice session preparation and configuration.
 * Handles deck loading, card filtering, and session initialization.
 */
@Component
public final class PracticeSessionService {

    // Dependencies
    private final DeckUseCase deckUseCase;
    private final CardUseCase cardUseCase;
    private final StatsService statsService;
    private final PracticeSettingsService practiceSettingsService;

    /**
     * Creates a new PracticeSessionService with required dependencies.
     *
     * @param useCase service for deck operations (non-null)
     * @param cardUseCaseValue service for card operations (non-null)
     * @param stats service for statistics recording (non-null)
     * @param practiceSettings service for practice configuration (non-null)
     * @throws IllegalArgumentException if any parameter is null
     */
    public PracticeSessionService(
            final DeckUseCase useCase,
            final CardUseCase cardUseCaseValue,
            final StatsService stats,
            final PracticeSettingsService practiceSettings) {

        if (useCase == null) {
            throw new IllegalArgumentException("DeckUseCase cannot be null");
        }
        if (cardUseCaseValue == null) {
            throw new IllegalArgumentException("CardUseCase cannot be null");
        }
        if (stats == null) {
            throw new IllegalArgumentException("StatsService cannot be null");
        }
        if (practiceSettings == null) {
            throw new IllegalArgumentException("PracticeSettingsService cannot be null");
        }

        this.deckUseCase = useCase;
        this.cardUseCase = cardUseCaseValue;
        this.statsService = stats;
        this.practiceSettingsService = practiceSettings;
    }

    /**
     * Loads a deck by its ID.
     *
     * @param deckId the ID of the deck to load (must be positive)
     * @return an Optional containing the deck if found, empty otherwise, never null
     * @throws IllegalArgumentException if deckId is not positive
     */
    public Optional<Deck> loadDeck(final long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive, got: " + deckId);
        }
        return deckUseCase.getDeckById(deckId);
    }

    /**
     * Gets cards that are not yet marked as known in a deck.
     *
     * @param deckId the ID of the deck to check (must be positive)
     * @return a list of cards not yet known by the user, never null (maybe empty)
     * @throws IllegalArgumentException if deckId is not positive
     */
    public List<Card> getNotKnownCards(final long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive, got: " + deckId);
        }
        List<Card> all = cardUseCase.getCardsByDeckId(deckId);
        Set<Long> known = statsService.getKnownCardIds(deckId);
        return all.stream().filter(fc -> !known.contains(fc.getId())).toList();
    }

    /**
     * Determines the default number of cards for a practice session.
     *
     * @param deckId the ID of the deck to calculate count for (must be positive)
     * @return the number of cards to include in the practice session (1 to configured default)
     * @throws IllegalArgumentException if deckId is not positive
     */
    public int resolveDefaultCount(final long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive, got: " + deckId);
        }
        int configured = practiceSettingsService.getDefaultCount();
        int notKnown = getNotKnownCards(deckId).size();
        return Math.clamp(notKnown, 1, configured);
    }

    /**
     * Determines the default number of cards for a practice session.
     * Optimized version that accepts pre-loaded cards to avoid redundant database queries.
     *
     * @param notKnownCards list of not-known cards already fetched from database
     * @return the number of cards to include in the practice session (1 to configured default)
     * @throws IllegalArgumentException if notKnownCards is null
     */
    public int resolveDefaultCount(final List<Card> notKnownCards) {
        if (notKnownCards == null) {
            throw new IllegalArgumentException("notKnownCards cannot be null");
        }
        int configured = practiceSettingsService.getDefaultCount();
        int notKnown = notKnownCards.size();
        return Math.clamp(notKnown, 1, configured);
    }

    /**
     * Determines if practice sessions should use random card order.
     *
     * @return true if random ordering is enabled, false for sequential ordering
     */
    public boolean isRandom() {
        return practiceSettingsService.isDefaultRandomOrder();
    }

    /**
     * Gets the default practice direction for sessions.
     *
     * @return the default practice direction, never null
     */
    public PracticeDirection defaultDirection() {
        return Optional.ofNullable(practiceSettingsService.getDefaultDirection())
                .orElse(PracticeDirection.FRONT_TO_BACK);
    }

    /**
     * Prepares a practice session with the specified number of cards.
     * Creates a practice session by filtering unknown cards and optionally
     * randomizing their order, limited to available unknown cards or requested count.
     *
     * @param deckId the ID of the deck to practice
     * @param count the desired number of cards in the session
     * @param random whether to randomize the card order
     * @return a list of cards prepared for the practice session
     */
    public List<Card> prepareSession(final long deckId, final int count, final boolean random) {
        // Filter to only unknown cards for focused practice
        List<Card> filtered = new ArrayList<>(getNotKnownCards(deckId));
        if (filtered.isEmpty()) {
            return filtered;
        }

        // Randomize card order if requested for varied practice experience
        if (random) {
            Collections.shuffle(filtered);
        }

        // Limit session size to requested count or available cards
        if (count < filtered.size()) {
            return new ArrayList<>(filtered.subList(0, count));
        }
        return filtered;
    }

    /**
     * Records completed practice session.
     *
     * @param deckId the ID of the deck that was practiced
     * @param totalViewed the total number of cards viewed in the session
     * @param correct the number of cards answered correctly
     * @param hard the number of cards marked as difficult
     * @param sessionDuration the total duration of the practice session
     * @param totalAnswerDelayMs the total time spent before answering
     * @param knownCardIdsDelta the collection of card IDs that changed knowledge status
     */
    public void recordSession(
            final long deckId,
            final int totalViewed,
            final int correct,
            final int hard,
            final Duration sessionDuration,
            final long totalAnswerDelayMs,
            final List<Long> knownCardIdsDelta) {
        SessionStatsDto sessionData = SessionStatsDto.builder()
                .deckId(deckId)
                .viewed(totalViewed)
                .correct(correct)
                .hard(hard)
                .sessionDurationMs(sessionDuration.toMillis())
                .totalAnswerDelayMs(totalAnswerDelayMs)
                .knownCardIdsDelta(knownCardIdsDelta)
                .build();
        statsService.recordSession(sessionData);
    }

    /**
     * Starts a new practice session.
     * Creates and initializes a new practice session with the specified parameters.
     *
     * @param deckId the ID of the deck to practice
     * @param count the number of cards to include in the session
     * @param random whether to randomize the card order
     * @return a new Session instance ready for practice
     */
    public PracticeSession startSession(final long deckId, final int count, final boolean random) {
        List<Card> cards = prepareSession(deckId, count, random);
        return PracticeSession.create(deckId, cards, Instant.now());
    }

    /**
     * Starts a new practice session using preloaded cards.
     * Optimized version that uses already fetched cards to avoid redundant database queries.
     *
     * @param deckId the ID of the deck to practice
     * @param preloadedCards list of cards already fetched from database
     * @param count the number of cards to include in the session
     * @param random whether to randomize the card order
     * @return a new Session instance ready for practice
     * @throws IllegalArgumentException if deckId is not positive or preloadedCards is null
     */
    public PracticeSession startSessionWithCards(
            final long deckId, final List<Card> preloadedCards, final int count, final boolean random) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive, got: " + deckId);
        }
        if (preloadedCards == null) {
            throw new IllegalArgumentException("Preloaded cards cannot be null");
        }

        List<Card> cards = new ArrayList<>(preloadedCards);
        if (cards.isEmpty()) {
            return PracticeSession.create(deckId, cards, Instant.now());
        }

        // Randomize card order if requested for varied practice experience
        if (random) {
            Collections.shuffle(cards);
        }

        // Limit session size to requested count or available cards
        if (count < cards.size()) {
            cards = new ArrayList<>(cards.subList(0, count));
        }

        return PracticeSession.create(deckId, cards, Instant.now());
    }

    /**
     * Calculates session completion metrics.
     *
     * @param session the completed session
     * @return completion metrics record
     */
    public SessionCompletionMetrics calculateCompletionMetrics(final PracticeSession session) {
        int totalCards = (session.getCards() != null) ? session.getCards().size() : session.getTotalViewed();
        long sessionDurationSec =
                Instant.now().getEpochSecond() - session.getSessionStart().getEpochSecond();
        long sessionMinutes = Math.clamp(sessionDurationSec / 60, 1L, Integer.MAX_VALUE);

        double denom = Math.clamp(session.getTotalViewed(), 1.0, Double.MAX_VALUE);
        long avgSeconds =
                Math.clamp(Math.round((session.getTotalAnswerDelayMs() / denom) / 1000.0), 1L, Long.MAX_VALUE);

        return new SessionCompletionMetrics(totalCards, sessionMinutes, avgSeconds);
    }

    /**
     * Gets list of failed cards that are still not known.
     *
     * @param deckId the deck ID
     * @param failedCardIds list of failed card IDs from session
     * @return list of failed cards
     */
    public List<Card> getFailedCards(final long deckId, final List<Long> failedCardIds) {
        if (failedCardIds == null || failedCardIds.isEmpty()) {
            return List.of();
        }

        List<Card> notKnownCards = getNotKnownCards(deckId);
        return notKnownCards.stream()
                .filter(fc -> failedCardIds.contains(fc.getId()))
                .toList();
    }

    /**
     * Starts a new practice session with failed cards.
     *
     * @param deckId the deck ID
     * @param failedCards list of failed cards to practice
     * @return new practice session
     */
    public PracticeSession startRepeatSession(final long deckId, final List<Card> failedCards) {
        List<Card> cards = new ArrayList<>(failedCards);
        Collections.shuffle(cards);
        return PracticeSession.create(deckId, cards, Instant.now());
    }

    /**
     * Session completion metrics record.
     *
     * @param totalCards total number of cards in session
     * @param sessionMinutes session duration in minutes
     * @param avgSeconds average answer time in seconds
     */
    public record SessionCompletionMetrics(int totalCards, long sessionMinutes, long avgSeconds) {}
}
