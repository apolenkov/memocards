package org.apolenkov.application.domain.port;

import java.util.List;
import java.util.Optional;

import org.apolenkov.application.model.User;

/**
 * Domain port for user management operations.
 *
 * <p>Defines contract for CRUD operations on users,
 * including authentication and profile management.</p>
 */
public interface UserRepository {

    /**
     * Gets all users in system.
     *
     * @return list of all users
     */
    List<User> findAll();

    /**
     * Finds user by identifier.
     *
     * @param id user identifier
     * @return user if found, empty otherwise
     */
    Optional<User> findById(long id);

    /**
     * Finds user by email address.
     *
     * @param email user's email address
     * @return user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Saves user (creates new or updates existing).
     *
     * @param user user to save
     * @return saved user with generated ID
     */
    User save(User user);

    /**
     * Deletes user by identifier.
     *
     * @param id user identifier to delete
     */
    void deleteById(long id);
}
