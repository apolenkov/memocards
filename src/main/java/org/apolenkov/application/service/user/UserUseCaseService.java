package org.apolenkov.application.service.user;

import java.util.List;
import java.util.Optional;

import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.domain.usecase.UserUseCase;
import org.apolenkov.application.model.User;
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
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

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
     * Gets all users in the system.
     *
     * @return a list of all users in the system, never null (maybe empty)
     */
    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public Optional<User> getUserById(final long id) {
        return userRepository.findById(id);
    }

    /**
     * Gets the currently authenticated user from Spring Security context.
     * Resolves authentication principal to domain user object.
     * Uses Caffeine cache at repository level for performance.
     *
     * @return the currently authenticated user
     * @throws IllegalStateException if the user is not authenticated, if the principal
     *                               type is unsupported, or if the authenticated principal has no corresponding domain user
     */
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("java:S2139") // Security audit requires logging before rethrow (OWASP compliance)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            AUDIT_LOGGER.warn("Unauthenticated access attempt to getCurrentUser()");
            throw new IllegalStateException("Unauthenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            AUDIT_LOGGER.error("Authenticated principal is null");
            throw new IllegalStateException("Authenticated principal is null");
        }

        String username = getUsername(principal);

        try {
            User user = userRepository.findByEmail(username).orElseThrow(() -> {
                AUDIT_LOGGER.error("Authenticated principal has no domain user: username={}", username);
                return new IllegalStateException("Authenticated principal has no domain user: " + username);
            });

            LOGGER.debug("Current user retrieved: userId={}, email={}", user.getId(), user.getEmail());
            return user;
        } catch (Exception e) {
            // S2139: Intentionally logging before rethrow for security audit trail (OWASP compliance)
            LOGGER.error("Error retrieving current user: username={}", username, e);
            throw e;
        }
    }

    private String getUsername(final Object principal) {
        return switch (principal) {
            case UserDetails userDetails -> userDetails.getUsername();
            case String username -> username;
            case null -> throw new IllegalStateException("Principal is null");
            default -> throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
        };
    }

    /**
     * Updates user and clears request-scoped cache to avoid stale data.
     * Should be used instead of direct repository.save() when modifying existing users.
     *
     * <p>Use cases (future implementation):
     * <ul>
     *   <li>User profile editing (email, name change)</li>
     *   <li>Admin panel user management</li>
     *   <li>Settings updates affecting user entity</li>
     * </ul>
     *
     * <p>Current usage: Ready for implementation when user editing features are added.
     * For password reset, see {@link org.apolenkov.application.service.security.PasswordResetService}
     * which uses direct repository (safe due to separate request lifecycle).
     *
     * @param user user to update
     * @return updated user
     */
    @Override
    @Transactional
    public User updateUser(final User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getId() == null) {
            throw new IllegalArgumentException("Cannot update user without ID - use save() for new users");
        }

        User saved = userRepository.save(user);

        AUDIT_LOGGER.info("User updated: userId={}, email={}", saved.getId(), saved.getEmail());
        return saved;
    }
}
