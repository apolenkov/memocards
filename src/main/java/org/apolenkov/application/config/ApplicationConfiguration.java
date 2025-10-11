package org.apolenkov.application.config;

import java.time.Clock;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.domain.port.NewsRepository;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.service.seed.DataSeedRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Application-wide configuration for common beans and utilities.
 */
@Configuration
public class ApplicationConfiguration {

    /**
     * Provides system UTC clock for time operations.
     *
     * @return system UTC clock instance
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * Groups repository dependencies for data seed service.
     * Only created in dev and test profiles where DataSeedService is active.
     *
     * @param userRepository repository for user operations
     * @param deckRepository repository for deck operations
     * @param flashcardRepository repository for flashcard operations
     * @param statsRepository repository for statistics operations
     * @param newsRepository repository for news operations
     * @return grouped repository dependencies
     */
    @Bean
    @Profile({"dev", "test"})
    public DataSeedRepositories dataSeedRepositories(
            final UserRepository userRepository,
            final DeckRepository deckRepository,
            final FlashcardRepository flashcardRepository,
            final StatsRepository statsRepository,
            final NewsRepository newsRepository) {
        return new DataSeedRepositories(
                userRepository, deckRepository, flashcardRepository, statsRepository, newsRepository);
    }
}
