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
     * @param repo Spring Data JPA repository for deck operations
     * @throws IllegalArgumentException if repo is null
     */
    public DeckJpaAdapter(DeckJpaRepository repo) {
        if (repo == null) {
            throw new IllegalArgumentException("DeckJpaRepository cannot be null");
        }
        this.repo = repo;
    }

    /**
     * Converts JPA entity to domain model.
     *
     * @param e JPA entity to convert
     * @return corresponding domain model
     * @throws IllegalArgumentException if e is null
     */
    private static Deck toModel(DeckEntity e) {
        if (e == null) {
            throw new IllegalArgumentException("DeckEntity cannot be null");
        }

        Deck d = new Deck(e.getId(), e.getUserId(), e.getTitle(), e.getDescription());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }

    /**
     * Converts domain model to JPA entity with timestamp handling.
     *
     * @param d domain model to convert
     * @return corresponding JPA entity
     * @throws IllegalArgumentException if d is null
     */
    private static DeckEntity toEntity(Deck d) {
        if (d == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }

        DeckEntity e = new DeckEntity();
        e.setId(d.getId());
        e.setUserId(d.getUserId());
        e.setTitle(d.getTitle());
        e.setDescription(d.getDescription());
        e.setCreatedAt(d.getCreatedAt() != null ? d.getCreatedAt() : java.time.LocalDateTime.now());
        e.setUpdatedAt(d.getUpdatedAt() != null ? d.getUpdatedAt() : java.time.LocalDateTime.now());
        return e;
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
    public List<Deck> findByUserId(Long userId) {
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
    public Optional<Deck> findById(Long id) {
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
    public Deck save(Deck deck) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }

        DeckEntity saved = repo.save(toEntity(deck));
        return toModel(saved);
    }

    /**
     * Deletes deck by unique identifier.
     *
     * @param id unique identifier of deck to delete
     * @throws IllegalArgumentException if id is null
     */
    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        repo.deleteById(id);
    }
}
