package org.apolenkov.application.domain.port;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.User;

/**
 * Domain port for user management operations.
 *
 * <p>Defines the contract for CRUD operations on users,
 * including authentication and profile management.</p>
 */
public interface UserRepository {

    /**
     * Retrieves all users in the system.
     *
     * @return list of all users
     */
    List<User> findAll();

    /**
     * Finds a user by their identifier.
     *
     * @param id the user identifier
     * @return user if found, empty otherwise
     */
    Optional<User> findById(Long id);

    /**
     * Finds a user by their email address.
     *
     * @param email the user's email address
     * @return user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Saves a user (creates new or updates existing).
     *
     * @param user the user to save
     * @return the saved user with generated ID
     */
    User save(User user);

    /**
     * Deletes a user by their identifier.
     *
     * @param id the user identifier to delete
     */
    void deleteById(Long id);
}
