package org.apolenkov.application.usecase;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.User;

/**
 * Use case interface for user management operations.
 * Defines core business operations for managing users in system.
 * Provides clean abstraction layer between application services and
 * underlying data access layer.
 */
public interface UserUseCase {
    /**
     * Gets all users in system.
     *
     * @return list of all registered users
     */
    List<User> getAllUsers();

    /**
     * Gets specific user by ID.
     *
     * @param id unique identifier of user
     * @return Optional containing user if found, empty otherwise
     */
    Optional<User> getUserById(long id);

    /**
     * Gets currently authenticated user.
     * Returns user associated with current security context.
     * Typically used to get user information for operations
     * that require user-specific data.
     *
     * @return currently authenticated user
     */
    User getCurrentUser();
}
