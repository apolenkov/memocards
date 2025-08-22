package org.apolenkov.application.service.user;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.config.TransactionAnnotations;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.apolenkov.application.usecase.UserUseCase;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service implementation for user use cases and business operations.
 *
 * <p>This service implements the UserUseCase interface and provides user management
 * functionality including user retrieval and current user context management. It
 * integrates with Spring Security to provide authenticated user information and
 * maintains transaction boundaries for data consistency.</p>
 *
 * <p>The service handles both general user operations and security context-aware
 * operations for the currently authenticated user.</p>
 *
 */
@Service
public class UserUseCaseService implements UserUseCase {

    private final UserRepository userRepository;

    /**
     * Constructs a new UserUseCaseService with the required repository dependency.
     *
     * @param userRepository the repository for user persistence operations
     */
    public UserUseCaseService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all users in the system.
     *
     * <p>Returns a complete list of all registered users. This method is typically
     * used for administrative purposes and user listing functionality.</p>
     *
     * @return a list of all users in the system
     */
    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a specific user by their unique identifier.
     *
     * <p>Returns an Optional containing the user if found, or an empty Optional
     * if no user exists with the specified ID.</p>
     *
     * @param id the unique identifier of the user to retrieve
     * @return an Optional containing the user if found, empty otherwise
     */
    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Retrieves the currently authenticated user.
     *
     * <p>This method extracts the current user from the Spring Security context
     * and retrieves the corresponding domain user object. It handles different
     * types of authentication principals and provides meaningful error messages
     * for various failure scenarios.</p>
     *
     * <p>The method requires an active authentication context and will throw
     * exceptions if the user is not authenticated or if the authenticated
     * principal cannot be resolved to a domain user.</p>
     *
     * @return the currently authenticated user
     * @throws IllegalStateException if the user is not authenticated, if the principal
     *         type is unsupported, or if the authenticated principal has no corresponding domain user
     */
    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Unauthenticated");
        }
        Object principal = authentication.getPrincipal();
        String username;

        if (principal instanceof UserDetails ud) {
            username = ud.getUsername();
        } else if (principal instanceof String s) {
            username = s;
        } else {
            throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
        }
        return userRepository
                .findByEmail(username)
                .orElseThrow(
                        () -> new IllegalStateException("Authenticated principal has no domain user: " + username));
    }
}
