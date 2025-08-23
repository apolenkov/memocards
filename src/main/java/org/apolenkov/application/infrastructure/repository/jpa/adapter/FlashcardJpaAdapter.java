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
     * @param repo the Spring Data JPA repository for flashcard operations
     * @throws IllegalArgumentException if repo is null
     */
    public FlashcardJpaAdapter(FlashcardJpaRepository repo) {
        if (repo == null) {
            throw new IllegalArgumentException("FlashcardJpaRepository cannot be null");
        }
        this.repo = repo;
    }

    /**
     * Converts JPA entity to domain model.
     *
     * @param e the JPA entity to convert
     * @return the corresponding domain model
     * @throws IllegalArgumentException if e is null
     */
    private static Flashcard toModel(FlashcardEntity e) {
        if (e == null) {
            throw new IllegalArgumentException("FlashcardEntity cannot be null");
        }

        Flashcard f = new Flashcard(e.getId(), e.getDeckId(), e.getFrontText(), e.getBackText());
        f.setExample(e.getExample());
        f.setImageUrl(e.getImageUrl());
        f.setCreatedAt(e.getCreatedAt());
        f.setUpdatedAt(e.getUpdatedAt());
        return f;
    }

    /**
     * Converts domain model to JPA entity with timestamp handling.
     *
     * @param f the domain model to convert
     * @return the corresponding JPA entity
     * @throws IllegalArgumentException if f is null
     */
    private static FlashcardEntity toEntity(Flashcard f) {
        if (f == null) {
            throw new IllegalArgumentException("Flashcard cannot be null");
        }

        FlashcardEntity e = new FlashcardEntity();
        e.setId(f.getId());
        e.setDeckId(f.getDeckId());
        e.setFrontText(f.getFrontText());
        e.setBackText(f.getBackText());
        e.setExample(f.getExample());
        e.setImageUrl(f.getImageUrl());
        e.setCreatedAt(f.getCreatedAt() != null ? f.getCreatedAt() : java.time.LocalDateTime.now());
        e.setUpdatedAt(f.getUpdatedAt() != null ? f.getUpdatedAt() : java.time.LocalDateTime.now());
        return e;
    }

    /**
     * Retrieves all flashcards belonging to a specific deck.
     *
     * @param deckId the ID of the deck whose flashcards to retrieve
     * @return list of flashcards in the deck
     * @throws IllegalArgumentException if deckId is null
     */
    @Override
    public List<Flashcard> findByDeckId(Long deckId) {
        if (deckId == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
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
    public Optional<Flashcard> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Flashcard ID cannot be null");
        }
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
    public Flashcard save(Flashcard flashcard) {
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
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Flashcard ID cannot be null");
        }
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
    public long countByDeckId(Long deckId) {
        if (deckId == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        return repo.countByDeckId(deckId);
    }

    /**
     * Deletes all flashcards belonging to a specific deck.
     *
     * @param deckId the ID of the deck whose flashcards to delete
     * @throws IllegalArgumentException if deckId is null
     */
    @Override
    public void deleteByDeckId(Long deckId) {
        if (deckId == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        repo.deleteByDeckId(deckId);
    }
}
