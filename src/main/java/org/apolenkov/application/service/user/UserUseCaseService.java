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
 * Service implementation for user use cases with Spring Security integration
 * and transaction management.
 */
@Service
public class UserUseCaseService implements UserUseCase {

    private final UserRepository userRepository;

    /**
     * Creates a new UserUseCaseService with the required repository dependency.
     *
     * @param userRepository the repository for user persistence operations (non-null)
     * @throws IllegalArgumentException if userRepository is null
     */
    public UserUseCaseService(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("UserRepository cannot be null");
        }
        this.userRepository = userRepository;
    }

    /**
     * Gets all users in the system.
     *
     * @return a list of all users in the system, never null (may be empty)
     */
    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Gets a specific user by their unique identifier.
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
     * Gets the currently authenticated user from Spring Security context.
     * Resolves authentication principal to domain user object.
     *
     * @return the currently authenticated user
     * @throws IllegalStateException if the user is not authenticated, if the principal
     *                               type is unsupported, or if the authenticated principal has no corresponding domain user
     */
    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Unauthenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new IllegalStateException("Authenticated principal is null");
        }

        String username = getUsername(principal);

        return userRepository
                .findByEmail(username)
                .orElseThrow(
                        () -> new IllegalStateException("Authenticated principal has no domain user: " + username));
    }

    private String getUsername(Object principal) {
        if (principal instanceof UserDetails p) {
            return p.getUsername();
        }

        if (principal instanceof String s) {
            return s;
        }

        throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
    }
}
