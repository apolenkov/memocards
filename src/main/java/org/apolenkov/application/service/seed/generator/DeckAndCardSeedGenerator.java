package org.apolenkov.application.service.seed.generator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.User;
import org.apolenkov.application.service.seed.DataGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Generator for test decks and flashcards.
 * Uses Virtual Threads for parallel processing.
 */
@Component
@Profile({"dev", "test"})
public class DeckAndCardSeedGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckAndCardSeedGenerator.class);

    private static final String[] DECK_TITLES = {
        "English Vocabulary", "Programming Terms", "Math Basics", "History Facts"
    };
    private static final String[] CARD_FRONTS = {"Algorithm", "Database", "API", "Framework", "Bug", "Testing"};
    private static final String[] CARD_BACKS = {
        "Step-by-step procedure",
        "Data storage",
        "Application interface",
        "Development platform",
        "Program error",
        "Quality assurance"
    };
    private static final String[] CARD_EXAMPLES = {
        "Binary search, Quick sort, Dijkstra's algorithm",
        "MySQL, PostgreSQL, MongoDB collections",
        "REST endpoints, GraphQL queries, HTTP requests",
        "Spring Boot, React, Angular, Vue.js",
        "NullPointerException, IndexOutOfBounds, StackOverflow",
        "Unit tests, Integration tests, E2E testing"
    };

    private final DataSeedRepository seedRepository;
    private final TransactionTemplate transactionTemplate;
    private final SecureRandom random = new SecureRandom();

    /**
     * Creates DeckAndCardSeedGenerator with required dependencies.
     *
     * @param seedRepositoryValue repository for batch operations
     * @param transactionTemplateValue transaction template for TX control
     */
    public DeckAndCardSeedGenerator(
            final DataSeedRepository seedRepositoryValue, final TransactionTemplate transactionTemplateValue) {
        this.seedRepository = seedRepositoryValue;
        this.transactionTemplate = transactionTemplateValue;
    }

    /**
     * Generates decks and flashcards using Virtual Threads for parallel processing.
     *
     * @param users list of users to generate data for
     * @param decksPerUser number of decks per user
     * @param cardsPerDeck number of cards per deck
     * @param batchSize batch size for user chunks
     * @return array with [totalDecks, totalCards] counts
     */
    @SuppressWarnings("java:S2139") // Audit requires logging before rethrow
    public int[] generateDecksAndCards(
            final List<User> users, final int decksPerUser, final int cardsPerDeck, final int batchSize) {
        LOGGER.info("Generating decks and flashcards using Virtual Threads...");
        LOGGER.info("Using generation limits: {} decks per user, {} cards per deck", decksPerUser, cardsPerDeck);

        int totalDecks = 0;
        int totalCards = 0;

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<java.util.concurrent.Future<int[]>> futures = new ArrayList<>();

            for (int i = 0; i < users.size(); i += batchSize) {
                int endIdx = Math.min(i + batchSize, users.size());
                List<User> userChunk = users.subList(i, endIdx);

                futures.add(executor.submit(() -> processUserChunk(userChunk, decksPerUser, cardsPerDeck)));
            }

            // Collect results
            for (var future : futures) {
                int[] counts = future.get();
                totalDecks += counts[0];
                totalCards += counts[1];
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Thread interrupted during parallel processing", e);
            throw new DataGenerationException("Failed to generate decks and cards - interrupted", e);
        } catch (Exception e) {
            LOGGER.error("Error in parallel processing", e);
            throw new DataGenerationException("Failed to generate decks and cards", e);
        }

        LOGGER.info("Successfully generated {} decks and {} flashcards", totalDecks, totalCards);
        return new int[] {totalDecks, totalCards};
    }

    /**
     * Processes a chunk of users to generate their decks and flashcards.
     *
     * @param userChunk chunk of users to process
     * @param deckCount number of decks per user
     * @param cardCount number of cards per deck
     * @return array with [decks, cards] counts
     */
    private int[] processUserChunk(final List<User> userChunk, final int deckCount, final int cardCount) {
        return transactionTemplate.execute(status -> {
            int decks = 0;
            int cards = 0;

            for (User user : userChunk) {
                // Generate decks for this user in batch
                List<Deck> userDecks = new ArrayList<>(deckCount);
                for (int i = 0; i < deckCount; i++) {
                    userDecks.add(createTestDeck(user, i));
                }

                List<Deck> savedDecks = seedRepository.batchInsertDecks(userDecks);
                decks += savedDecks.size();

                // Generate flashcards for all decks in batch
                List<Flashcard> allCards = new ArrayList<>(deckCount * cardCount);
                for (Deck deck : savedDecks) {
                    for (int j = 0; j < cardCount; j++) {
                        allCards.add(createTestCard(deck, j));
                    }
                }

                seedRepository.batchInsertFlashcards(allCards);
                cards += allCards.size();
            }

            return new int[] {decks, cards};
        });
    }

    /**
     * Creates a test deck for a user.
     *
     * @param user the deck owner
     * @param index deck index for unique title
     * @return configured test deck
     */
    private Deck createTestDeck(final User user, final int index) {
        String title = DECK_TITLES[random.nextInt(DECK_TITLES.length)] + " " + (index + 1);
        return new Deck(null, user.getId(), title, "Test deck for load testing");
    }

    /**
     * Creates a test flashcard.
     *
     * @param deck the parent deck
     * @param index card index for unique content
     * @return configured test flashcard
     */
    private Flashcard createTestCard(final Deck deck, final int index) {
        int cardIndex = random.nextInt(CARD_FRONTS.length);
        String front = CARD_FRONTS[cardIndex] + " " + (index + 1);
        String back = CARD_BACKS[cardIndex];
        String example = CARD_EXAMPLES[cardIndex];
        return new Flashcard(null, deck.getId(), front, back, example);
    }
}
