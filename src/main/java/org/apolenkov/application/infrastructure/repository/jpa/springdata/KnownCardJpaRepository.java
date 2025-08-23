package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import java.util.Set;
import org.apolenkov.application.infrastructure.repository.jpa.entity.KnownCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for managing known cards.
 *
 * <p>Provides CRUD operations and custom queries for tracking
 * which cards users have already learned in specific decks.</p>
 */
public interface KnownCardJpaRepository extends JpaRepository<KnownCardEntity, Long> {

    /**
     * Finds all known card IDs for a specific deck.
     *
     * @param deckId the deck identifier
     * @return set of card IDs that are known for this deck
     */
    @Query("select k.cardId from KnownCardEntity k where k.deckId=:deckId")
    Set<Long> findKnownCardIds(@Param("deckId") long deckId);

    /**
     * Removes a specific card from known cards for a deck.
     *
     * @param deckId the deck identifier
     * @param cardId the card identifier to remove
     */
    @Modifying
    @Query("delete from KnownCardEntity k where k.deckId=:deckId and k.cardId=:cardId")
    void deleteKnown(@Param("deckId") long deckId, @Param("cardId") long cardId);

    /**
     * Removes all known cards for a specific deck.
     *
     * @param deckId the deck identifier
     */
    @Modifying
    @Query("delete from KnownCardEntity k where k.deckId=:deckId")
    void deleteByDeckId(@Param("deckId") long deckId);
}
