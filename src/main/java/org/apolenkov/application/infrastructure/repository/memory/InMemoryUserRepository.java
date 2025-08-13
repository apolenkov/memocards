package org.apolenkov.application.infrastructure.repository.memory;

import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final ConcurrentMap<Long, User> idToUser = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> emailToId = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public List<User> findAll() {
        return new ArrayList<>(idToUser.values());
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(idToUser.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Long id = emailToId.get(email);
        return id == null ? Optional.empty() : Optional.ofNullable(idToUser.get(id));
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idSequence.getAndIncrement());
        }
        idToUser.put(user.getId(), user);
        if (user.getEmail() != null) {
            emailToId.put(user.getEmail(), user.getId());
        }
        return user;
    }
}


