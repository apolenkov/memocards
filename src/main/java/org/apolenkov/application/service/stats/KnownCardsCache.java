package org.apolenkov.application.service.stats;

import com.vaadin.flow.spring.annotation.UIScope;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * UI-scoped cache for known card IDs to avoid repeated database queries during navigation.
 * Cache lifetime is bound to UI session and automatically cleared when UI is destroyed.
 *
 * <p>Uses TTL (Time-To-Live) to ensure data freshness within the UI session.
 * Cache is invalidated automatically after practice sessions or manually via invalidate().
 */
@Component
@UIScope
public class KnownCardsCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(KnownCardsCache.class);
    private static final long TTL_MILLIS = 300_000; // 5 minutes TTL (longer than decks - less volatile)

    private final Map<Long, CachedKnownCards> cache = new ConcurrentHashMap<>();

    /**
     * Gets known card IDs for a deck, using cache if available and not expired.
     * If cache miss or expired, loads fresh data using the provided supplier.
     *
     * @param deckId the deck ID
     * @param loader supplier to load known card IDs from database
     * @return set of known card IDs
     */
    public Set<Long> getKnownCards(final Long deckId, final Supplier<Set<Long>> loader) {
        if (deckId == null) {
            LOGGER.warn("Cannot get known cards: deckId is null");
            return Set.of();
        }

        CachedKnownCards cached = cache.get(deckId);

        if (cached != null && cached.isValid()) {
            LOGGER.debug("Cache HIT: Returning {} known cards for deckId={}", cached.cardIds.size(), deckId);
            return cached.cardIds;
        }

        LOGGER.debug("Cache MISS: Loading known cards for deckId={}", deckId);
        Set<Long> cardIds = loader.get();
        cache.put(deckId, new CachedKnownCards(cardIds));
        LOGGER.debug("Cache updated: {} known cards cached for deckId={}", cardIds.size(), deckId);

        return cardIds;
    }

    /**
     * Gets known card IDs for multiple decks in batch.
     * Uses cache for individual decks, loads missing ones in batch.
     *
     * @param deckIds deck IDs to get known cards for
     * @param batchLoader supplier to load known cards for missing deck IDs in batch
     * @return map of deck ID to known card IDs
     */
    public Map<Long, Set<Long>> getKnownCardsBatch(
            final Set<Long> deckIds, final Supplier<Map<Long, Set<Long>>> batchLoader) {
        if (deckIds == null || deckIds.isEmpty()) {
            return Map.of();
        }

        // Check which decks are already cached
        Map<Long, Set<Long>> result = new ConcurrentHashMap<>();
        Set<Long> missingDeckIds = ConcurrentHashMap.newKeySet();

        for (Long deckId : deckIds) {
            CachedKnownCards cached = cache.get(deckId);
            if (cached != null && cached.isValid()) {
                result.put(deckId, cached.cardIds);
            } else {
                missingDeckIds.add(deckId);
            }
        }

        // Load missing decks in batch
        if (!missingDeckIds.isEmpty()) {
            LOGGER.debug("Cache MISS: Loading known cards for {} decks in batch", missingDeckIds.size());
            Map<Long, Set<Long>> loaded = batchLoader.get();

            // Cache newly loaded data
            loaded.forEach((deckId, cardIds) -> {
                cache.put(deckId, new CachedKnownCards(cardIds));
                result.put(deckId, cardIds);
            });
        }

        LOGGER.debug(
                "Batch retrieval: {} decks from cache, {} loaded from DB",
                deckIds.size() - missingDeckIds.size(),
                missingDeckIds.size());

        return result;
    }

    /**
     * Invalidates cache for a specific deck.
     * Call this after practice sessions or progress reset.
     *
     * @param deckId the deck ID
     */
    public void invalidate(final Long deckId) {
        if (deckId != null) {
            cache.remove(deckId);
            LOGGER.debug("Cache invalidated for deckId={}", deckId);
        }
    }

    /**
     * Cached known cards with TTL.
     */
    private static final class CachedKnownCards {
        private final Set<Long> cardIds;
        private final Instant cachedAt;

        CachedKnownCards(final Set<Long> ids) {
            this.cardIds = Set.copyOf(ids); // Immutable copy
            this.cachedAt = Instant.now();
        }

        boolean isValid() {
            return Instant.now().isBefore(cachedAt.plusMillis(TTL_MILLIS));
        }
    }
}
