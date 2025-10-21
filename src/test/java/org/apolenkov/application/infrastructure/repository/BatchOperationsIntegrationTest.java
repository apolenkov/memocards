package org.apolenkov.application.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apolenkov.application.BaseIntegrationTest;
import org.apolenkov.application.domain.port.CardRepository;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.Card;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for batch operations.
 * Tests real database queries using TestContainers.
 */
@DisplayName("Batch Operations Integration Tests")
class BatchOperationsIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private StatsRepository statsRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user with unique email to avoid conflicts between test runs
        String uniqueEmail = "batch-test-" + UUID.randomUUID() + "@example.com";
        testUser = new User();
        testUser.setEmail(uniqueEmail);
        testUser.setName("Batch Test User");
        testUser.setPasswordHash("hashedPassword");
        testUser.addRole("USER");
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should count cards by multiple deck IDs in single query")
    void shouldCountByDeckIds() {
        // Given: 3 decks with different card counts
        Deck deck1 = createAndSaveDeck("Deck 1");
        Deck deck2 = createAndSaveDeck("Deck 2");
        Deck deck3 = createAndSaveDeck("Deck 3");

        createCards(deck1.getId(), 10);
        createCards(deck2.getId(), 20);
        createCards(deck3.getId(), 30);

        List<Long> deckIds = List.of(deck1.getId(), deck2.getId(), deck3.getId());

        // When: Batch count
        Map<Long, Long> counts = cardRepository.countByDeckIds(deckIds);

        // Then: All counts returned correctly
        assertThat(counts)
                .hasSize(3)
                .containsEntry(deck1.getId(), 10L)
                .containsEntry(deck2.getId(), 20L)
                .containsEntry(deck3.getId(), 30L);
    }

    @Test
    @DisplayName("Should handle empty deck IDs list")
    void shouldHandleEmptyDeckIdsList() {
        // When: Empty list
        Map<Long, Long> counts = cardRepository.countByDeckIds(List.of());

        // Then: Empty map returned
        assertThat(counts).isEmpty();
    }

    @Test
    @DisplayName("Should exclude decks with zero cards")
    void shouldExcludeDecksWithZeroCards() {
        // Given: deck1 has 10 cards, deck2 has 0 cards
        Deck deck1 = createAndSaveDeck("Deck with cards");
        Deck deck2 = createAndSaveDeck("Empty deck");

        createCards(deck1.getId(), 10);
        // deck2 has no cards

        List<Long> deckIds = List.of(deck1.getId(), deck2.getId());

        // When: Batch count
        Map<Long, Long> counts = cardRepository.countByDeckIds(deckIds);

        // Then: Only deck1 in results
        assertThat(counts).hasSize(1).containsKey(deck1.getId()).doesNotContainKey(deck2.getId());
    }

    @Test
    @DisplayName("Should get known card IDs for multiple decks in batch")
    void shouldGetKnownCardIdsBatch() {
        // Given: 2 decks with cards and known cards
        Deck deck1 = createAndSaveDeck("Deck 1");
        Deck deck2 = createAndSaveDeck("Deck 2");

        List<Long> deck1CardIds = createCards(deck1.getId(), 5);
        List<Long> deck2CardIds = createCards(deck2.getId(), 3);

        // Mark some cards as known
        statsRepository.setCardKnown(deck1.getId(), deck1CardIds.get(0), true);
        statsRepository.setCardKnown(deck1.getId(), deck1CardIds.get(1), true);
        statsRepository.setCardKnown(deck1.getId(), deck1CardIds.get(2), true);

        statsRepository.setCardKnown(deck2.getId(), deck2CardIds.get(0), true);
        statsRepository.setCardKnown(deck2.getId(), deck2CardIds.get(1), true);

        List<Long> deckIds = List.of(deck1.getId(), deck2.getId());

        // When: Batch get known cards
        Map<Long, Set<Long>> knownCards = statsRepository.getKnownCardIdsBatch(deckIds);

        // Then: Known cards for both decks returned
        assertThat(knownCards).hasSize(2);
        assertThat(knownCards.get(deck1.getId()))
                .hasSize(3)
                .containsExactlyInAnyOrder(deck1CardIds.get(0), deck1CardIds.get(1), deck1CardIds.get(2));
        assertThat(knownCards.get(deck2.getId()))
                .hasSize(2)
                .containsExactlyInAnyOrder(deck2CardIds.get(0), deck2CardIds.get(1));
    }

    @Test
    @DisplayName("Should handle deck with no known cards in batch query")
    void shouldHandleDeckWithNoKnownCards() {
        // Given: deck1 has known cards, deck2 has no known cards
        Deck deck1 = createAndSaveDeck("Deck with known");
        Deck deck2 = createAndSaveDeck("Deck without known");

        List<Long> deck1CardIds = createCards(deck1.getId(), 3);
        createCards(deck2.getId(), 3);

        statsRepository.setCardKnown(deck1.getId(), deck1CardIds.getFirst(), true);

        List<Long> deckIds = List.of(deck1.getId(), deck2.getId());

        // When: Batch get known cards
        Map<Long, Set<Long>> knownCards = statsRepository.getKnownCardIdsBatch(deckIds);

        // Then: Only deck1 in results
        assertThat(knownCards).hasSize(1).containsKey(deck1.getId()).doesNotContainKey(deck2.getId());
    }

    @Test
    @DisplayName("Should return empty map for empty deck IDs in batch query")
    void shouldReturnEmptyMapForEmptyDeckIds() {
        // When: Empty list
        Map<Long, Set<Long>> knownCards = statsRepository.getKnownCardIdsBatch(List.of());

        // Then: Empty map
        assertThat(knownCards).isEmpty();
    }

    @Test
    @DisplayName("Should handle large batch of deck IDs efficiently")
    void shouldHandleLargeBatchOfDeckIds() {
        // Given: 50 decks with varying card counts
        List<Deck> decks = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Deck deck = createAndSaveDeck("Deck " + i);
            decks.add(deck);
            createCards(deck.getId(), (i % 10) + 1); // 1-10 cards per deck
        }

        List<Long> deckIds = decks.stream().map(Deck::getId).toList();

        // When: Batch count
        Map<Long, Long> counts = cardRepository.countByDeckIds(deckIds);

        // Then: All 50 decks counted
        assertThat(counts).hasSize(50);
        for (int i = 0; i < 50; i++) {
            long expectedCount = (i % 10) + 1;
            assertThat(counts).containsEntry(decks.get(i).getId(), expectedCount);
        }
    }

    /**
     * Helper: Creates and saves a deck.
     *
     * @param title deck title
     * @return saved deck with generated ID
     */
    private Deck createAndSaveDeck(final String title) {
        Deck deck = new Deck(null, testUser.getId(), title, "Test description");
        return deckRepository.save(deck);
    }

    /**
     * Helper: Creates multiple cards for a deck.
     *
     * @param deckId deck ID to create cards for
     * @param count number of cards to create
     * @return list of created card IDs
     */
    private List<Long> createCards(final long deckId, final int count) {
        List<Long> cardIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Card card = new Card(null, deckId, "Front " + i, "Back " + i, "Example " + i);
            cardRepository.save(card);
            // ID is set directly on card object by save()
            cardIds.add(card.getId());
        }
        return cardIds;
    }
}
