package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.DeckEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.DeckJpaRepository;
import org.apolenkov.application.model.Deck;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for deck repository operations.
 *
 * <p>Bridges domain layer and JPA persistence for deck CRUD operations.
 * Active in dev/prod profiles only.</p>
 */
@Profile({"dev", "prod"})
@Repository
public class DeckJpaAdapter implements DeckRepository {

    private final DeckJpaRepository repo;

    /**
     * Creates adapter with JPA repository dependency.
     *
     * @param repoValue Spring Data JPA repository for deck operations
     * @throws IllegalArgumentException if repo is null
     */
    public DeckJpaAdapter(final DeckJpaRepository repoValue) {
        if (repoValue == null) {
            throw new IllegalArgumentException("DeckJpaRepository cannot be null");
        }
        this.repo = repoValue;
    }

    /**
     * Converts JPA entity to domain model.
     *
     * @param entity JPA entity to convert
     * @return corresponding domain model
     * @throws IllegalArgumentException if entity is null
     */
    private static Deck toModel(final DeckEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("DeckEntity cannot be null");
        }

        final Deck deck = new Deck(entity.getId(), entity.getUserId(), entity.getTitle(), entity.getDescription());
        deck.setCreatedAt(entity.getCreatedAt());
        deck.setUpdatedAt(entity.getUpdatedAt());
        return deck;
    }

    /**
     * Converts domain model to JPA entity with timestamp handling.
     *
     * @param deck domain model to convert
     * @return corresponding JPA entity
     * @throws IllegalArgumentException if deck is null
     */
    private static DeckEntity toEntity(final Deck deck) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }

        final DeckEntity entity = new DeckEntity();
        entity.setId(deck.getId());
        entity.setUserId(deck.getUserId());
        entity.setTitle(deck.getTitle());
        entity.setDescription(deck.getDescription());
        entity.setCreatedAt(deck.getCreatedAt() != null ? deck.getCreatedAt() : java.time.LocalDateTime.now());
        entity.setUpdatedAt(deck.getUpdatedAt() != null ? deck.getUpdatedAt() : java.time.LocalDateTime.now());
        return entity;
    }

    /**
     * Retrieves all decks from database.
     *
     * @return list of all decks
     */
    @Override
    public List<Deck> findAll() {
        return repo.findAll().stream().map(DeckJpaAdapter::toModel).toList();
    }

    /**
     * Retrieves all decks owned by specific user.
     *
     * @param userId ID of user whose decks to retrieve
     * @return list of decks owned by user
     * @throws IllegalArgumentException if userId is null
     */
    @Override
    public List<Deck> findByUserId(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return repo.findByUserId(userId).stream().map(DeckJpaAdapter::toModel).toList();
    }

    /**
     * Retrieves deck by unique identifier.
     *
     * @param id unique identifier of deck
     * @return Optional containing deck if found
     * @throws IllegalArgumentException if id is null
     */
    @Override
    public Optional<Deck> findById(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        return repo.findById(id).map(DeckJpaAdapter::toModel);
    }

    /**
     * Saves deck to database.
     *
     * @param deck deck to save
     * @return saved deck with updated fields
     * @throws IllegalArgumentException if deck is null
     */
    @Override
    public Deck save(final Deck deck) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }

        final DeckEntity saved = repo.save(toEntity(deck));
        return toModel(saved);
    }

    /**
     * Deletes deck by unique identifier.
     *
     * @param id unique identifier of deck to delete
     * @throws IllegalArgumentException if id is null
     */
    @Override
    public void deleteById(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        repo.deleteById(id);
    }
}
