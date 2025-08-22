package org.apolenkov.application.usecase;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.User;

/**
 * Use case interface for user management operations.
 *
 * <p>This interface defines the core business operations for managing users
 * in the system. It provides a clean abstraction layer between the application
 * services and the underlying data access layer.</p>
 *
 * <p>Key operations include:</p>
 * <ul>
 *   <li>Retrieving users by various criteria</li>
 *   <li>Accessing current authenticated user information</li>
 *   <li>User data management and retrieval</li>
 * </ul>
 */
public interface UserUseCase {
    /**
     * Retrieves all users in the system.
     *
     * @return a list of all registered users
     */
    List<User> getAllUsers();

    /**
     * Retrieves a specific user by their ID.
     *
     * @param id the unique identifier of the user
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> getUserById(Long id);

    /**
     * Retrieves the currently authenticated user.
     *
     * <p>Returns the user associated with the current security context.
     * This method is typically used to get user information for operations
     * that require user-specific data.</p>
     *
     * @return the currently authenticated user
     */
    User getCurrentUser();
}
