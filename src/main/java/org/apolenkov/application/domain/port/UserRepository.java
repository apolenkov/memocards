package org.apolenkov.application.domain.port;

import org.apolenkov.application.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Port for accessing Users.
 */
public interface UserRepository {
    List<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    User save(User user);
}


