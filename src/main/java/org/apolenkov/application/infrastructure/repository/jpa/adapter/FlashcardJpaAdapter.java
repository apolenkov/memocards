package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.FlashcardEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.FlashcardJpaRepository;
import org.apolenkov.application.model.Flashcard;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile({"dev", "jpa", "prod"})
@Repository
public class FlashcardJpaAdapter implements FlashcardRepository {

    private final FlashcardJpaRepository repo;

    public FlashcardJpaAdapter(FlashcardJpaRepository repo) {
        this.repo = repo;
    }

    private static Flashcard toModel(FlashcardEntity e) {
        Flashcard f = new Flashcard(e.getId(), e.getDeckId(), e.getFrontText(), e.getBackText());
        f.setExample(e.getExample());
        f.setImageUrl(e.getImageUrl());
        f.setCreatedAt(e.getCreatedAt());
        f.setUpdatedAt(e.getUpdatedAt());
        return f;
    }

    private static FlashcardEntity toEntity(Flashcard f) {
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

    @Override
    public List<Flashcard> findByDeckId(Long deckId) {
        return repo.findByDeckId(deckId).stream()
                .map(FlashcardJpaAdapter::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Flashcard> findById(Long id) {
        return repo.findById(id).map(FlashcardJpaAdapter::toModel);
    }

    @Override
    public Flashcard save(Flashcard flashcard) {
        return toModel(repo.save(toEntity(flashcard)));
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Override
    public long countByDeckId(Long deckId) {
        return repo.countByDeckId(deckId);
    }

    @Override
    public void deleteByDeckId(Long deckId) {
        repo.deleteByDeckId(deckId);
    }
}
