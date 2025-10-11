package org.apolenkov.application.service.user;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.config.cache.RequestScopedUserCache;
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
    private final RequestScopedUserCache userCache;

    /**
     * Creates a new UserUseCaseService with the required dependencies.
     *
     * @param userRepositoryValue the repository for user persistence operations (non-null)
     * @param userCacheValue request-scoped cache for current user (non-null)
     * @throws IllegalArgumentException if any dependency is null
     */
    public UserUseCaseService(final UserRepository userRepositoryValue, final RequestScopedUserCache userCacheValue) {
        if (userRepositoryValue == null) {
            throw new IllegalArgumentException("UserRepository cannot be null");
        }
        if (userCacheValue == null) {
            throw new IllegalArgumentException("RequestScopedUserCache cannot be null");
        }
        this.userRepository = userRepositoryValue;
        this.userCache = userCacheValue;
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
     *
     * @return the currently authenticated user
     * @throws IllegalStateException if the user is not authenticated, if the principal
     *                               type is unsupported, or if the authenticated principal has no corresponding domain user
     */
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("java:S2139") // Security audit requires logging before rethrow (OWASP compliance)
    public User getCurrentUser() {
        User cachedUser = userCache.get();
        if (cachedUser != null) {
            return cachedUser;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            AUDIT_LOGGER.warn("Unauthenticated access attempt to getCurrentUser()");
            LOGGER.warn("Attempted to get current user without authentication");
            throw new IllegalStateException("Unauthenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            AUDIT_LOGGER.error("Authenticated principal is null");
            LOGGER.error("Authentication principal is null");
            throw new IllegalStateException("Authenticated principal is null");
        }

        String username = getUsername(principal);

        try {
            User user = userRepository.findByEmail(username).orElseThrow(() -> {
                AUDIT_LOGGER.error("Authenticated principal has no domain user: username={}", username);
                LOGGER.error("Domain user not found for authenticated principal: {}", username);
                return new IllegalStateException("Authenticated principal has no domain user: " + username);
            });

            // Cache for current request to avoid repeated database calls
            userCache.set(user);

            LOGGER.debug("Current user retrieved from database: userId={}, email={}", user.getId(), user.getEmail());
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
}
