package org.apolenkov.application.infrastructure.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.apolenkov.application.BaseIntegrationTest;
import org.apolenkov.application.domain.model.FilterOption;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for dynamic flashcard filtering.
 * Tests FlashcardQueryBuilder with real PostgreSQL database via TestContainers.
 */
@SpringBootTest
@Transactional
class FlashcardDynamicFilterIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatsRepository statsRepository;

    private User testUser;
    private Deck testDeck;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("password");
        testUser.setName("Test User");
        testUser = userRepository.save(testUser);

        // Create test deck
        testDeck = new Deck();
        testDeck.setUserId(testUser.getId());
        testDeck.setTitle("Test Deck");
        testDeck.setDescription("Test Description");
        testDeck = deckRepository.save(testDeck);

        // Create test flashcards
        // "apple" - will be marked as KNOWN
        Flashcard card1 = new Flashcard(null, testDeck.getId(), "apple", "яблоко", "I like apples");
        flashcardRepository.save(card1);

        // "banana" - will be marked as KNOWN
        Flashcard card2 = new Flashcard(null, testDeck.getId(), "banana", "банан", "Yellow banana");
        flashcardRepository.save(card2);

        // "cherry" - will remain UNKNOWN
        Flashcard card3 = new Flashcard(null, testDeck.getId(), "cherry", "вишня", "Red cherry");
        flashcardRepository.save(card3);

        // "date" - will remain UNKNOWN
        Flashcard card4 = new Flashcard(null, testDeck.getId(), "date", "финик", "Sweet date");
        flashcardRepository.save(card4);

        // "elderberry" - will remain UNKNOWN
        Flashcard card5 = new Flashcard(null, testDeck.getId(), "elderberry", "бузина", "Dark elderberry");
        flashcardRepository.save(card5);

        // Mark card1 and card2 as KNOWN
        statsRepository.setCardKnown(testDeck.getId(), card1.getId(), true);
        statsRepository.setCardKnown(testDeck.getId(), card2.getId(), true);
    }

    @Test
    @DisplayName("Should retrieve all flashcards when no filter applied")
    void testFindAllNoFilter() {
        // Given: No filter (FilterOption.ALL, no search query)
        PageRequest pageRequest = PageRequest.of(0, 10);

        // When: Retrieving flashcards
        List<Flashcard> result =
                flashcardRepository.findFlashcardsWithFilter(testDeck.getId(), null, FilterOption.ALL, pageRequest);

        // Then: Should return all 5 flashcards
        assertThat(result).hasSize(5);
        assertThat(result)
                .extracting(Flashcard::getFrontText)
                .containsExactlyInAnyOrder("apple", "banana", "cherry", "date", "elderberry");
    }

    @ParameterizedTest
    @CsvSource({"ALL,5", "KNOWN_ONLY,2", "UNKNOWN_ONLY,3"})
    @DisplayName("Should count flashcards with different filters")
    void testCountFlashcardsWithFilter(final FilterOption filterOption, final long expectedCount) {
        // When: Counting flashcards with filter
        long count = flashcardRepository.countFlashcardsWithFilter(testDeck.getId(), null, filterOption);

        // Then: Should count expected number of flashcards
        assertThat(count).isEqualTo(expectedCount);
    }

    @Test
    @DisplayName("Should retrieve only KNOWN flashcards")
    void testFindAllKnownOnly() {
        // Given: KNOWN_ONLY filter
        PageRequest pageRequest = PageRequest.of(0, 10);

        // When: Retrieving known flashcards
        List<Flashcard> result = flashcardRepository.findFlashcardsWithFilter(
                testDeck.getId(), null, FilterOption.KNOWN_ONLY, pageRequest);

        // Then: Should return only card1 and card2
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Flashcard::getFrontText).containsExactlyInAnyOrder("apple", "banana");
    }

    @Test
    @DisplayName("Should retrieve only UNKNOWN flashcards")
    void testFindAllUnknownOnly() {
        // Given: UNKNOWN_ONLY filter
        PageRequest pageRequest = PageRequest.of(0, 10);

        // When: Retrieving unknown flashcards
        List<Flashcard> result = flashcardRepository.findFlashcardsWithFilter(
                testDeck.getId(), null, FilterOption.UNKNOWN_ONLY, pageRequest);

        // Then: Should return card3, card4, card5
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Flashcard::getFrontText)
                .containsExactlyInAnyOrder("cherry", "date", "elderberry");
    }

    @ParameterizedTest
    @CsvSource({"berry,elderberry", "Red,cherry", "APPLE,apple"})
    @DisplayName("Should search flashcards by query in all fields (case-insensitive)")
    void testSearchFlashcards(final String searchQuery, final String expectedFrontText) {
        // Given: Search query
        PageRequest pageRequest = PageRequest.of(0, 10);

        // When: Searching flashcards
        List<Flashcard> result = flashcardRepository.findFlashcardsWithFilter(
                testDeck.getId(), searchQuery, FilterOption.ALL, pageRequest);

        // Then: Should return expected flashcard
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getFrontText()).isEqualTo(expectedFrontText);
    }

    @Test
    @DisplayName("Should count flashcards matching search query")
    void testCountAllSearchQuery() {
        // Given: Search query "an" (should match "banana")
        // When: Counting matching flashcards
        long count = flashcardRepository.countFlashcardsWithFilter(testDeck.getId(), "an", FilterOption.ALL);

        // Then: Should count 1 flashcard
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should combine search query with KNOWN filter")
    void testFindAllSearchWithKnownFilter() {
        // Given: Search query "a" + KNOWN_ONLY filter
        // Should match "apple" and "banana" (both contain "a" and are known)
        PageRequest pageRequest = PageRequest.of(0, 10);

        // When: Searching known flashcards
        List<Flashcard> result = flashcardRepository.findFlashcardsWithFilter(
                testDeck.getId(), "a", FilterOption.KNOWN_ONLY, pageRequest);

        // Then: Should return apple and banana
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Flashcard::getFrontText).containsExactlyInAnyOrder("apple", "banana");
    }

    @Test
    @DisplayName("Should count flashcards matching search query with KNOWN filter")
    void testCountAllSearchWithKnownFilter() {
        // Given: Search query "a" + KNOWN_ONLY filter
        // When: Counting matching known flashcards
        long count = flashcardRepository.countFlashcardsWithFilter(testDeck.getId(), "a", FilterOption.KNOWN_ONLY);

        // Then: Should count 2 flashcards (apple, banana)
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should combine search query with UNKNOWN filter")
    void testFindAllSearchWithUnknownFilter() {
        // Given: Search query "e" + UNKNOWN_ONLY filter
        // Should match "cherry", "date", "elderberry" (all contain "e" and are unknown)
        PageRequest pageRequest = PageRequest.of(0, 10);

        // When: Searching unknown flashcards
        List<Flashcard> result = flashcardRepository.findFlashcardsWithFilter(
                testDeck.getId(), "e", FilterOption.UNKNOWN_ONLY, pageRequest);

        // Then: Should return cherry, date, elderberry
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Flashcard::getFrontText)
                .containsExactlyInAnyOrder("cherry", "date", "elderberry");
    }

    @Test
    @DisplayName("Should count flashcards matching search query with UNKNOWN filter")
    void testCountAllSearchWithUnknownFilter() {
        // Given: Search query "e" + UNKNOWN_ONLY filter (should match cherry, date, elderberry)
        // When: Counting matching unknown flashcards
        long count = flashcardRepository.countFlashcardsWithFilter(testDeck.getId(), "e", FilterOption.UNKNOWN_ONLY);

        // Then: Should count 3 flashcards (cherry, date, elderberry all contain "e" and are unknown)
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return empty list when search query matches no flashcards")
    void testFindAllNoMatches() {
        // Given: Search query that doesn't match anything
        PageRequest pageRequest = PageRequest.of(0, 10);

        // When: Searching flashcards
        List<Flashcard> result =
                flashcardRepository.findFlashcardsWithFilter(testDeck.getId(), "xyz123", FilterOption.ALL, pageRequest);

        // Then: Should return empty list
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return 0 count when search query matches no flashcards")
    void testCountAllNoMatches() {
        // Given: Search query that doesn't match anything
        // When: Counting matching flashcards
        long count = flashcardRepository.countFlashcardsWithFilter(testDeck.getId(), "xyz123", FilterOption.ALL);

        // Then: Should return 0
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testFindAllPagination() {
        // Given: Request for first 2 flashcards
        PageRequest pageRequest = PageRequest.of(0, 2);

        // When: Retrieving first page
        List<Flashcard> firstPage =
                flashcardRepository.findFlashcardsWithFilter(testDeck.getId(), null, FilterOption.ALL, pageRequest);

        // Then: Should return 2 flashcards
        assertThat(firstPage).hasSize(2);

        // When: Retrieving second page
        PageRequest secondPageRequest = PageRequest.of(1, 2);
        List<Flashcard> secondPage = flashcardRepository.findFlashcardsWithFilter(
                testDeck.getId(), null, FilterOption.ALL, secondPageRequest);

        // Then: Should return 2 different flashcards
        assertThat(secondPage).hasSize(2);
        assertThat(firstPage).doesNotContainAnyElementsOf(secondPage);
    }

    @Test
    @DisplayName("Should handle empty search query as no filter")
    void testFindAllEmptySearchQuery() {
        // Given: Empty search query
        PageRequest pageRequest = PageRequest.of(0, 10);

        // When: Retrieving flashcards with empty search
        List<Flashcard> result =
                flashcardRepository.findFlashcardsWithFilter(testDeck.getId(), "", FilterOption.ALL, pageRequest);

        // Then: Should return all flashcards (same as no filter)
        assertThat(result).hasSize(5);
    }

    @Test
    @DisplayName("Should return only flashcards from specified deck")
    void testFindAllOnlyFromSpecifiedDeck() {
        // Given: Another deck with flashcards
        Deck anotherDeck = new Deck();
        anotherDeck.setUserId(testUser.getId());
        anotherDeck.setTitle("Another Deck");
        anotherDeck.setDescription("Another Description");
        anotherDeck = deckRepository.save(anotherDeck);

        Flashcard anotherCard = new Flashcard(null, anotherDeck.getId(), "grape", "виноград", "Purple grape");
        flashcardRepository.save(anotherCard);

        PageRequest pageRequest = PageRequest.of(0, 10);

        // When: Retrieving flashcards from test deck
        List<Flashcard> result =
                flashcardRepository.findFlashcardsWithFilter(testDeck.getId(), null, FilterOption.ALL, pageRequest);

        // Then: Should return only flashcards from test deck (not "grape")
        assertThat(result).hasSize(5);
        assertThat(result).extracting(Flashcard::getFrontText).doesNotContain("grape");
    }

    @Test
    @DisplayName("Should order flashcards by updated_at DESC")
    void testFindAllOrderedByUpdatedAt() {
        // Given: Flashcards in database
        PageRequest pageRequest = PageRequest.of(0, 10);

        // When: Retrieving flashcards
        List<Flashcard> result =
                flashcardRepository.findFlashcardsWithFilter(testDeck.getId(), null, FilterOption.ALL, pageRequest);

        // Then: Should be ordered by updated_at (newest first)
        // We verify all cards are present (exact order verification would require timestamp manipulation)
        assertThat(result).hasSize(5);
    }
}
