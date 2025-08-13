package org.apolenkov.application.infrastructure.repository.memory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.model.Flashcard;
import org.springframework.stereotype.Repository;

@org.springframework.context.annotation.Profile("memory")
@Repository
public class InMemoryFlashcardRepository implements FlashcardRepository {

  private final ConcurrentMap<Long, Flashcard> idToCard = new ConcurrentHashMap<>();
  private final AtomicLong idSequence = new AtomicLong(1);

  @Override
  public List<Flashcard> findByDeckId(Long deckId) {
    return idToCard.values().stream()
        .filter(c -> deckId != null && deckId.equals(c.getDeckId()))
        .collect(Collectors.toList());
  }

  @Override
  public Optional<Flashcard> findById(Long id) {
    return Optional.ofNullable(idToCard.get(id));
  }

  @Override
  public Flashcard save(Flashcard flashcard) {
    if (flashcard.getId() == null) {
      flashcard.setId(idSequence.getAndIncrement());
    }
    idToCard.put(flashcard.getId(), flashcard);
    return flashcard;
  }

  @Override
  public void deleteById(Long id) {
    idToCard.remove(id);
  }
}
