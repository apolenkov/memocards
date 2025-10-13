package org.apolenkov.application.views.deck.cache;

import com.vaadin.flow.spring.annotation.UIScope;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.apolenkov.application.domain.event.DeckModifiedEvent;
import org.apolenkov.application.model.Deck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * UI-scoped cache for user decks.
 * Cache lifetime is bound to UI session and automatically cleared when UI is destroyed.
 * Data freshness is ensured through automatic expiration and event-based invalidation.
 */
@Component
@UIScope
public class UserDecksCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDecksCache.class);
    private static final long TTL_MILLIS = 60_000; // 1 minute TTL

    private final Map<Long, CachedDecks> cache = new ConcurrentHashMap<>();

    /**
     * Gets decks for a user.
     * Uses cached data when available and valid, otherwise loads fresh data.
     *
     * @param userId the user ID
     * @param loader supplier to load decks when cache miss occurs
     * @return list of user's decks, never null
     */
    public List<Deck> getDecks(final Long userId, final Supplier<List<Deck>> loader) {
        if (userId == null) {
            LOGGER.warn("Cannot get decks: userId is null");
            return List.of();
        }

        CachedDecks cached = cache.get(userId);

        if (cached != null && cached.isValid()) {
            LOGGER.debug("Cache HIT: Returning {} decks for userId={}", cached.decks.size(), userId);
            return cached.decks;
        }

        LOGGER.debug("Cache MISS: Loading decks for userId={}", userId);
        List<Deck> decks = loader.get();
        cache.put(userId, new CachedDecks(decks));
        LOGGER.debug("Cache updated: {} decks cached for userId={}", decks.size(), userId);

        return decks;
    }

    /**
     * Invalidates cache for a specific user.
     * Call after deck modifications to ensure data freshness.
     *
     * @param userId the user ID
     */
    public void invalidate(final Long userId) {
        if (userId != null) {
            cache.remove(userId);
            LOGGER.debug("Cache invalidated for userId={}", userId);
        }
    }

    /**
     * Handles deck modification events to invalidate cache.
     * Ensures UI displays current data after deck changes.
     *
     * @param event the deck modified event
     */
    @EventListener
    public void onDeckModified(final DeckModifiedEvent event) {
        LOGGER.debug(
                "Deck modified event received: userId={}, deckId={}, type={}",
                event.getUserId(),
                event.getDeckId(),
                event.getType());
        invalidate(event.getUserId());
    }

    /**
     * Cached decks with TTL.
     */
    private static final class CachedDecks {
        private final List<Deck> decks;
        private final Instant cachedAt;

        CachedDecks(final List<Deck> decksList) {
            this.decks = List.copyOf(decksList); // Immutable copy
            this.cachedAt = Instant.now();
        }

        boolean isValid() {
            return Instant.now().isBefore(cachedAt.plusMillis(TTL_MILLIS));
        }
    }
}
