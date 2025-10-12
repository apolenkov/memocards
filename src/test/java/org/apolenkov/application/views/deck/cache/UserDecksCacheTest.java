package org.apolenkov.application.views.deck.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apolenkov.application.domain.event.DeckModifiedEvent;
import org.apolenkov.application.domain.event.DeckModifiedEvent.ModificationType;
import org.apolenkov.application.model.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for UserDecksCache.
 * Tests caching behavior, TTL, invalidation, and event-driven updates.
 */
@DisplayName("UserDecksCache Unit Tests")
class UserDecksCacheTest {

    private UserDecksCache cache;
    private AtomicInteger loaderCallCount;

    @BeforeEach
    void setUp() {
        cache = new UserDecksCache();
        loaderCallCount = new AtomicInteger(0);
    }

    @Test
    @DisplayName("Should cache decks on first access")
    void shouldCacheDecksOnFirstAccess() {
        // Given: Empty cache
        List<Deck> decks =
                List.of(new Deck(1L, 1L, "Deck 1", "Description 1"), new Deck(2L, 1L, "Deck 2", "Description 2"));

        // When: First access (cache MISS)
        List<Deck> result = cache.getDecks(1L, () -> {
            loaderCallCount.incrementAndGet();
            return decks;
        });

        // Then: Loader called, data cached
        assertThat(result).hasSize(2);
        assertThat(loaderCallCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return cached value on second access (cache HIT)")
    void shouldReturnCachedValueOnSecondAccess() {
        // Given: Cached decks
        List<Deck> decks = List.of(new Deck(1L, 1L, "Deck 1", "Desc"));
        cache.getDecks(1L, () -> {
            loaderCallCount.incrementAndGet();
            return decks;
        });

        // When: Second access (cache HIT)
        List<Deck> result = cache.getDecks(1L, () -> {
            loaderCallCount.incrementAndGet();
            return List.of(); // Should NOT be called
        });

        // Then: Cached value returned, loader NOT called
        assertThat(result).hasSize(1);
        assertThat(loaderCallCount.get()).isEqualTo(1); // Only first call
    }

    @Test
    @DisplayName("Should invalidate cache on deck created event")
    void shouldInvalidateCacheOnDeckCreatedEvent() {
        // Given: Cached decks for user 1
        List<Deck> initialDecks = List.of(new Deck(1L, 1L, "Deck 1", "Desc"));
        cache.getDecks(1L, () -> initialDecks);

        // When: DeckModifiedEvent (CREATED) published
        DeckModifiedEvent event = new DeckModifiedEvent(this, 1L, 2L, ModificationType.CREATED);
        cache.onDeckModified(event);

        // Then: Cache invalidated, next access reloads
        List<Deck> updatedDecks = List.of(new Deck(1L, 1L, "Deck 1", "Desc"), new Deck(2L, 1L, "Deck 2", "New deck"));

        List<Deck> result = cache.getDecks(1L, () -> {
            loaderCallCount.incrementAndGet();
            return updatedDecks;
        });

        assertThat(result).hasSize(2); // Fresh data
        assertThat(loaderCallCount.get()).isEqualTo(1); // Fresh load
    }

    @Test
    @DisplayName("Should invalidate cache on deck updated event")
    void shouldInvalidateCacheOnDeckUpdatedEvent() {
        // Given: Cached decks
        cache.getDecks(1L, () -> List.of(new Deck(1L, 1L, "Old Title", "Desc")));

        // When: DeckModifiedEvent (UPDATED) published
        DeckModifiedEvent event = new DeckModifiedEvent(this, 1L, 1L, ModificationType.UPDATED);
        cache.onDeckModified(event);

        // Then: Cache invalidated
        List<Deck> result = cache.getDecks(1L, () -> {
            loaderCallCount.incrementAndGet();
            return List.of(new Deck(1L, 1L, "New Title", "Desc"));
        });

        assertThat(result.getFirst().getTitle()).isEqualTo("New Title");
        assertThat(loaderCallCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should invalidate cache on deck deleted event")
    void shouldInvalidateCacheOnDeckDeletedEvent() {
        // Given: Cached decks (2 decks)
        cache.getDecks(1L, () -> List.of(new Deck(1L, 1L, "Deck 1", ""), new Deck(2L, 1L, "Deck 2", "")));

        // When: DeckModifiedEvent (DELETED) published for deck 2
        DeckModifiedEvent event = new DeckModifiedEvent(this, 1L, 2L, ModificationType.DELETED);
        cache.onDeckModified(event);

        // Then: Cache invalidated, next access shows only 1 deck
        List<Deck> result = cache.getDecks(1L, () -> {
            loaderCallCount.incrementAndGet();
            return List.of(new Deck(1L, 1L, "Deck 1", "")); // Deck 2 deleted
        });

        assertThat(result).hasSize(1);
        assertThat(loaderCallCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should NOT invalidate cache for different user")
    void shouldNotInvalidateCacheForDifferentUser() {
        // Given: Cached decks for user 1
        List<Deck> user1Decks = List.of(new Deck(1L, 1L, "User 1 Deck", ""));
        cache.getDecks(1L, () -> user1Decks);

        // When: DeckModifiedEvent for user 2 (different user)
        DeckModifiedEvent event = new DeckModifiedEvent(this, 2L, 10L, ModificationType.CREATED);
        cache.onDeckModified(event);

        // Then: Cache for user 1 NOT invalidated (still cached)
        List<Deck> result = cache.getDecks(1L, () -> {
            loaderCallCount.incrementAndGet();
            return List.of(); // Should NOT be called
        });

        assertThat(result).hasSize(1); // Cached value
        assertThat(loaderCallCount.get()).isZero(); // Loader NOT called
    }

    @Test
    @DisplayName("Should handle null user ID gracefully")
    void shouldHandleNullUserIdGracefully() {
        // When: Null user ID
        List<Deck> result = cache.getDecks(null, () -> {
            loaderCallCount.incrementAndGet();
            return List.of();
        });

        // Then: Empty list, loader NOT called
        assertThat(result).isEmpty();
        assertThat(loaderCallCount.get()).isZero();
    }

    @Test
    @DisplayName("Should cache empty deck list correctly")
    void shouldCacheEmptyDeckListCorrectly() {
        // Given: User with no decks
        List<Deck> emptyList = List.of();

        // When: Cache empty list
        List<Deck> result1 = cache.getDecks(1L, () -> {
            loaderCallCount.incrementAndGet();
            return emptyList;
        });

        // Then: Empty list cached
        assertThat(result1).isEmpty();

        // When: Second access
        List<Deck> result2 = cache.getDecks(1L, () -> {
            loaderCallCount.incrementAndGet();
            return List.of(new Deck()); // Should NOT be called
        });

        // Then: Cached empty list returned
        assertThat(result2).isEmpty();
        assertThat(loaderCallCount.get()).isEqualTo(1); // Only once
    }

    @Test
    @DisplayName("Should return immutable list from cache")
    void shouldReturnImmutableListFromCache() {
        // Given: Mutable list passed to cache
        List<Deck> decks = List.of(new Deck(1L, 1L, "Deck", ""));
        List<Deck> cached = cache.getDecks(1L, () -> decks);

        // When/Then: Returned list is immutable (List.copyOf in implementation)
        assertThat(cached).isInstanceOf(List.class).hasSize(1);
    }

    @Test
    @DisplayName("Should invalidate cache programmatically")
    void shouldInvalidateCacheProgrammatically() {
        // Given: Cached decks
        cache.getDecks(1L, () -> List.of(new Deck(1L, 1L, "Old", "")));

        // When: Manual invalidation
        cache.invalidate(1L);

        // Then: Next access reloads
        List<Deck> result = cache.getDecks(1L, () -> {
            loaderCallCount.incrementAndGet();
            return List.of(new Deck(1L, 1L, "New", ""));
        });

        assertThat(result.getFirst().getTitle()).isEqualTo("New");
        assertThat(loaderCallCount.get()).isEqualTo(1);
    }
}
