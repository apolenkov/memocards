package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.FlashcardEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.FlashcardJpaRepository;
import org.apolenkov.application.model.Flashcard;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for flashcard repository operations.
 *
 * <p>Bridges domain layer and JPA persistence for flashcard CRUD operations.
 * Active in dev/prod profiles only.</p>
 */
@Profile({"dev", "prod"})
@Repository
public class FlashcardJpaAdapter implements FlashcardRepository {

    private final FlashcardJpaRepository repo;

    /**
     * Creates adapter with JPA repository dependency.
     *
     * @param repoValue the Spring Data JPA repository for flashcard operations
     * @throws IllegalArgumentException if repo is null
     */
    public FlashcardJpaAdapter(final FlashcardJpaRepository repoValue) {
        if (repoValue == null) {
            throw new IllegalArgumentException("FlashcardJpaRepository cannot be null");
        }
        this.repo = repoValue;
    }

    /**
     * Converts JPA entity to domain model.
     *
     * @param entity the JPA entity to convert
     * @return the corresponding domain model
     * @throws IllegalArgumentException if entity is null
     */
    private static Flashcard toModel(final FlashcardEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("FlashcardEntity cannot be null");
        }

        final Flashcard flashcard =
                new Flashcard(entity.getId(), entity.getDeckId(), entity.getFrontText(), entity.getBackText());
        flashcard.setExample(entity.getExample());
        flashcard.setImageUrl(entity.getImageUrl());
        flashcard.setCreatedAt(entity.getCreatedAt());
        flashcard.setUpdatedAt(entity.getUpdatedAt());
        return flashcard;
    }

    /**
     * Converts domain model to JPA entity with timestamp handling.
     *
     * @param flashcard the domain model to convert
     * @return the corresponding JPA entity
     * @throws IllegalArgumentException if flashcard is null
     */
    private static FlashcardEntity toEntity(final Flashcard flashcard) {
        if (flashcard == null) {
            throw new IllegalArgumentException("Flashcard cannot be null");
        }

        final FlashcardEntity entity = new FlashcardEntity();
        entity.setId(flashcard.getId());
        entity.setDeckId(flashcard.getDeckId());
        entity.setFrontText(flashcard.getFrontText());
        entity.setBackText(flashcard.getBackText());
        entity.setExample(flashcard.getExample());
        entity.setImageUrl(flashcard.getImageUrl());
        entity.setCreatedAt(
                flashcard.getCreatedAt() != null ? flashcard.getCreatedAt() : java.time.LocalDateTime.now());
        entity.setUpdatedAt(
                flashcard.getUpdatedAt() != null ? flashcard.getUpdatedAt() : java.time.LocalDateTime.now());
        return entity;
    }

    /**
     * Retrieves all flashcards belonging to a specific deck.
     *
     * @param deckId the ID of the deck whose flashcards to retrieve
     * @return list of flashcards in the deck
     * @throws IllegalArgumentException if deckId is null
     */
    @Override
    public List<Flashcard> findByDeckId(final long deckId) {
        return repo.findByDeckId(deckId).stream()
                .map(FlashcardJpaAdapter::toModel)
                .toList();
    }

    /**
     * Retrieves a flashcard by its unique identifier.
     *
     * @param id the unique identifier of the flashcard
     * @return Optional containing the flashcard if found
     * @throws IllegalArgumentException if id is null
     */
    @Override
    public Optional<Flashcard> findById(final long id) {
        return repo.findById(id).map(FlashcardJpaAdapter::toModel);
    }

    /**
     * Saves a flashcard to the database.
     *
     * @param flashcard the flashcard to save
     * @return the saved flashcard with updated fields
     * @throws IllegalArgumentException if flashcard is null
     */
    @Override
    public Flashcard save(final Flashcard flashcard) {
        if (flashcard == null) {
            throw new IllegalArgumentException("Flashcard cannot be null");
        }
        return toModel(repo.save(toEntity(flashcard)));
    }

    /**
     * Deletes a flashcard by its unique identifier.
     *
     * @param id the unique identifier of the flashcard to delete
     * @throws IllegalArgumentException if id is null
     */
    @Override
    public void deleteById(final long id) {
        repo.deleteById(id);
    }

    /**
     * Counts the total number of flashcards in a specific deck.
     *
     * @param deckId the ID of the deck to count flashcards for
     * @return the total number of flashcards in the deck
     * @throws IllegalArgumentException if deckId is null
     */
    @Override
    public long countByDeckId(final long deckId) {
        return repo.countByDeckId(deckId);
    }

    /**
     * Deletes all flashcards belonging to a specific deck.
     *
     * @param deckId the ID of the deck whose flashcards to delete
     * @throws IllegalArgumentException if deckId is null
     */
    @Override
    public void deleteByDeckId(final long deckId) {
        repo.deleteByDeckId(deckId);
    }
}
