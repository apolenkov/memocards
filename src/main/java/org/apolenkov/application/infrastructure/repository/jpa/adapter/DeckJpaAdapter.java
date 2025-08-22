package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.DeckEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.DeckJpaRepository;
import org.apolenkov.application.model.Deck;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile({"dev", "prod"})
@Repository
public class DeckJpaAdapter implements DeckRepository {

    private final DeckJpaRepository repo;

    public DeckJpaAdapter(DeckJpaRepository repo) {
        this.repo = repo;
    }

    private static Deck toModel(DeckEntity e) {
        Deck d = new Deck(e.getId(), e.getUserId(), e.getTitle(), e.getDescription());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }

    private static DeckEntity toEntity(Deck d) {
        DeckEntity e = new DeckEntity();
        e.setId(d.getId());
        e.setUserId(d.getUserId());
        e.setTitle(d.getTitle());
        e.setDescription(d.getDescription());
        e.setCreatedAt(d.getCreatedAt() != null ? d.getCreatedAt() : java.time.LocalDateTime.now());
        e.setUpdatedAt(d.getUpdatedAt() != null ? d.getUpdatedAt() : java.time.LocalDateTime.now());
        return e;
    }

    @Override
    public List<Deck> findAll() {
        return repo.findAll().stream().map(DeckJpaAdapter::toModel).toList();
    }

    @Override
    public List<Deck> findByUserId(Long userId) {
        return repo.findByUserId(userId).stream().map(DeckJpaAdapter::toModel).toList();
    }

    @Override
    public Optional<Deck> findById(Long id) {
        return repo.findById(id).map(DeckJpaAdapter::toModel);
    }

    @Override
    public Deck save(Deck deck) {
        DeckEntity saved = repo.save(toEntity(deck));
        return toModel(saved);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
