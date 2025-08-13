package org.apolenkov.application.infrastructure.repository.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.model.Deck;
import org.springframework.stereotype.Repository;

@org.springframework.context.annotation.Profile("memory")
@Repository
public class InMemoryDeckRepository implements DeckRepository {

    private final ConcurrentMap<Long, Deck> idToDeck = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public List<Deck> findAll() {
        return new ArrayList<>(idToDeck.values());
    }

    @Override
    public List<Deck> findByUserId(Long userId) {
        return idToDeck.values().stream()
                .filter(d -> userId != null && userId.equals(d.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Deck> findById(Long id) {
        return Optional.ofNullable(idToDeck.get(id));
    }

    @Override
    public Deck save(Deck deck) {
        if (deck.getId() == null) {
            deck.setId(idSequence.getAndIncrement());
        }
        idToDeck.put(deck.getId(), deck);
        return deck;
    }

    @Override
    public void deleteById(Long id) {
        idToDeck.remove(id);
    }
}
