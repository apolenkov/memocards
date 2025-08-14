package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import java.util.Set;
import org.apolenkov.application.infrastructure.repository.jpa.entity.KnownCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KnownCardJpaRepository extends JpaRepository<KnownCardEntity, Long> {
    @Query("select k.cardId from KnownCardEntity k where k.deckId=:deckId")
    Set<Long> findKnownCardIds(@Param("deckId") long deckId);

    @Modifying
    @Query("delete from KnownCardEntity k where k.deckId=:deckId and k.cardId=:cardId")
    void deleteKnown(@Param("deckId") long deckId, @Param("cardId") long cardId);

    @Modifying
    @Query("delete from KnownCardEntity k where k.deckId=:deckId")
    void deleteByDeckId(@Param("deckId") long deckId);
}
