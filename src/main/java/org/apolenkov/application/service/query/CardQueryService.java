package org.apolenkov.application.service.query;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application-level query service for flashcards.
 * Centralizes search/filter/known-cards logic for UI.
 */
@Service
public class CardQueryService {

    private final FlashcardUseCase flashcardUseCase;
    private final StatsService statsService;

    public CardQueryService(FlashcardUseCase flashcardUseCase, StatsService statsService) {
        this.flashcardUseCase = flashcardUseCase;
        this.statsService = statsService;
    }

    @Transactional(readOnly = true)
    public List<Flashcard> listFilteredFlashcards(long deckId, String query, boolean hideKnown) {
        List<Flashcard> all = flashcardUseCase.getFlashcardsByDeckId(deckId);
        Set<Long> known = statsService.getKnownCardIds(deckId);
        return filterFlashcards(all, query, known, hideKnown);
    }

    @Transactional(readOnly = true)
    public List<Flashcard> filterFlashcards(List<Flashcard> base, String query, Set<Long> knownIds, boolean hideKnown) {
        String q = query != null ? query.toLowerCase(Locale.ROOT).trim() : "";
        return base.stream()
                .filter(fc -> q.isEmpty()
                        || contains(fc.getFrontText(), q)
                        || contains(fc.getBackText(), q)
                        || contains(fc.getExample(), q))
                .filter(fc -> !hideKnown || !knownIds.contains(fc.getId()))
                .collect(Collectors.toList());
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
