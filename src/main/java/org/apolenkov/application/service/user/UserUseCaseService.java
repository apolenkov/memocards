package org.apolenkov.application.service.user;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.apolenkov.application.usecase.UserUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for user use cases with Spring Security integration
 * and transaction management.
 */
@Service
public class UserUseCaseService implements UserUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserUseCaseService.class);

    private final UserRepository userRepository;

    /**
     * Creates a new UserUseCaseService with the required repository dependency.
     *
     * @param userRepositoryValue the repository for user persistence operations (non-null)
     * @throws IllegalArgumentException if userRepository is null
     */
    public UserUseCaseService(final UserRepository userRepositoryValue) {
        if (userRepositoryValue == null) {
            throw new IllegalArgumentException("UserRepository cannot be null");
        }
        this.userRepository = userRepositoryValue;
    }

    /**
     * Gets all users in the system with caching.
     *
     * @return a list of all users in the system, never null (maybe empty)
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        LOGGER.debug("Retrieving all users from database");
        return userRepository.findAll();
    }

    /**
     * Gets a specific user by their unique identifier with caching.
     *
     * @param id the unique identifier of the user to retrieve
     * @return an Optional containing the user if found, empty otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(final long id) {
        LOGGER.debug("Retrieving user by ID: {}", id);
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
    @Transactional(readOnly = true)
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

    private String getUsername(final Object principal) {
        return switch (principal) {
            case UserDetails userDetails -> userDetails.getUsername();
            case String username -> username;
            case null -> throw new IllegalStateException("Principal is null");
            default -> throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
        };
    }
}
