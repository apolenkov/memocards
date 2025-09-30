package org.apolenkov.application.views.stats.components;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import java.util.Map;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component responsible for loading and preparing statistics data.
 * Handles data retrieval from services and prepares it for display in the stats view.
 */
public final class StatsDataLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsDataLoader.class);

    // Dependencies
    private final DeckUseCase deckUseCase;
    private final UserUseCase userUseCase;
    private final StatsService statsService;

    // Translation provider interface
    private TranslationProvider translationProvider;

    /**
     * Creates a new StatsDataLoader with required dependencies.
     *
     * @param deckUseCaseParam service for deck operations
     * @param userUseCaseParam service for user operations
     * @param statsServiceParam service for statistics and progress tracking
     */
    public StatsDataLoader(
            final DeckUseCase deckUseCaseParam,
            final UserUseCase userUseCaseParam,
            final StatsService statsServiceParam) {
        this.deckUseCase = deckUseCaseParam;
        this.userUseCase = userUseCaseParam;
        this.statsService = statsServiceParam;
    }

    /**
     * Sets the translation provider for internationalization.
     *
     * @param translationProviderParam provider for translations
     */
    public void setTranslationProvider(final TranslationProvider translationProviderParam) {
        this.translationProvider = translationProviderParam;
    }

    /**
     * Loads statistics data and adds the main title to the page section.
     *
     * @param pageSection the container to add stats sections to
     * @return prepared statistics data
     */
    public StatsData loadStatsData(final VerticalLayout pageSection) {
        LOGGER.debug("Loading statistics data for current user");

        // Add main title
        addMainTitle(pageSection);

        // Load decks and statistics
        List<Deck> decks = loadUserDecks();
        Map<Long, StatsRepository.DeckAggregate> aggregates = loadDeckAggregates(decks);

        LOGGER.debug("Loaded {} decks with statistics", decks.size());

        return new StatsData(decks, aggregates);
    }

    /**
     * Adds the main statistics title to the page section.
     *
     * @param pageSection the container to add the title to
     */
    private void addMainTitle(final VerticalLayout pageSection) {
        H2 mainTitle = new H2(getTranslation("stats.title"));
        mainTitle.addClassName("stats-view__title");
        pageSection.add(mainTitle);
    }

    /**
     * Loads decks for the current user.
     *
     * @return list of user's decks
     */
    private List<Deck> loadUserDecks() {
        long userId = userUseCase.getCurrentUser().getId();
        return deckUseCase.getDecksByUserId(userId);
    }

    /**
     * Loads aggregated statistics for the given decks.
     *
     * @param decks list of decks to get statistics for
     * @return map of deck ID to aggregated statistics
     */
    private Map<Long, StatsRepository.DeckAggregate> loadDeckAggregates(final List<Deck> decks) {
        List<Long> deckIds = decks.stream().map(Deck::getId).toList();
        return statsService.getDeckAggregates(deckIds);
    }

    /**
     * Gets translation for the given key.
     *
     * @param key the translation key
     * @param params optional parameters for message formatting
     * @return translated text
     */
    private String getTranslation(final String key, final Object... params) {
        if (translationProvider == null) {
            LOGGER.warn("Translation provider not set, returning key: {}", key);
            return key;
        }
        return translationProvider.getTranslation(key, params);
    }

    /**
     * Data container for statistics information.
     *
     * @param decks list of user's decks
     * @param aggregates aggregated statistics for decks
     */
    public record StatsData(List<Deck> decks, Map<Long, StatsRepository.DeckAggregate> aggregates) {}

    /**
     * Interface for providing translations.
     */
    @FunctionalInterface
    public interface TranslationProvider {
        /**
         * Gets translation for the given key.
         *
         * @param key the translation key
         * @param params optional parameters for message formatting
         * @return translated text
         */
        String getTranslation(String key, Object... params);
    }
}
