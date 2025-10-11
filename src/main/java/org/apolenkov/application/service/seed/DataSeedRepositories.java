package org.apolenkov.application.service.seed;

import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.domain.port.NewsRepository;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.domain.port.UserRepository;

/**
 * Groups all repository dependencies required for data seed operations.
 * This reduces constructor parameter count in DataSeedService.
 *
 * @param userRepository repository for user operations
 * @param deckRepository repository for deck operations
 * @param flashcardRepository repository for flashcard operations
 * @param statsRepository repository for statistics operations
 * @param newsRepository repository for news operations
 */
public record DataSeedRepositories(
        UserRepository userRepository,
        DeckRepository deckRepository,
        FlashcardRepository flashcardRepository,
        StatsRepository statsRepository,
        NewsRepository newsRepository) {}
