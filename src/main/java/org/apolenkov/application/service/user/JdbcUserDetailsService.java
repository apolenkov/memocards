package org.apolenkov.application.service.user;

import java.util.Collection;
import java.util.List;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * JDBC-based implementation of Spring Security's UserDetailsService with role mapping and validation.
 */
@Service
@Profile({"dev", "prod", "test"})
public class JdbcUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcUserDetailsService.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

    private final UserRepository userRepository;

    /**
     * Creates a new JdbcUserDetailsService with the required repository dependency.
     *
     * @param userRepositoryValue the repository for user persistence operations
     */
    public JdbcUserDetailsService(final UserRepository userRepositoryValue) {
        this.userRepository = userRepositoryValue;
    }

    /**
     * Loads user details by username (email) for Spring Security authentication.
     * Converts user data to Spring Security format with role mapping and validation.
     *
     * @param username the email address of the user to load (Spring Security uses email as username)
     * @return UserDetails object containing user authentication and authorization information
     * @throws UsernameNotFoundException if authentication fails (generic message to prevent user enumeration)
     */
    @Override
    @SuppressWarnings("java:S2139") // Security audit requires logging before rethrow (OWASP compliance)
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        // Validate username
        if (username == null || username.trim().isEmpty()) {
            LOGGER.warn("Attempted to load user with null or empty username");
            throw new UsernameNotFoundException("Bad credentials");
        }

        LOGGER.debug("Loading user details for username: {}", username);

        try {
            // Find user by email
            User user = findUser(username).orElse(null);

            // Validate user exists and has password hash
            if (user == null || !hasValidPasswordHash(user)) {
                if (user == null) {
                    AUDIT_LOGGER.warn("Authentication failed: username={}", username);
                    LOGGER.warn("Authentication failed for: {}", username);
                } else {
                    AUDIT_LOGGER.error("User has no password hash: userId={}, email={}", user.getId(), username);
                    LOGGER.error("User has no password hash: {}", username);
                }
                // Generic error message to prevent user enumeration
                throw new UsernameNotFoundException("Bad credentials");
            }

            Collection<? extends GrantedAuthority> authorities = mapAuthorities(user);

            LOGGER.debug("User details loaded successfully: {}", username);
            return buildUserDetails(user, authorities);

        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            // S2139: Intentionally logging before rethrow for security audit trail (OWASP compliance)
            logAuthenticationError(username, e);
            throw new UsernameNotFoundException("Bad credentials");
        }
    }

    /**
     * Finds user by email without throwing exceptions.
     *
     * @param username the email to search for
     * @return Optional containing the user if found
     */
    private java.util.Optional<User> findUser(final String username) {
        return userRepository.findByEmail(username.trim());
    }

    /**
     * Checks if user has a valid password hash.
     *
     * @param user the user to validate
     * @return true if user has valid password hash, false otherwise
     */
    private boolean hasValidPasswordHash(final User user) {
        return user.getPasswordHash() != null && !user.getPasswordHash().isBlank();
    }

    /**
     * Maps user roles to Spring Security authorities.
     *
     * @param user the user with roles
     * @return collection of granted authorities
     */
    private Collection<? extends GrantedAuthority> mapAuthorities(final User user) {
        if (user.getRoles().isEmpty()) {
            return List.of(new SimpleGrantedAuthority(SecurityConstants.ROLE_USER));
        }
        return user.getRoles().stream().map(SimpleGrantedAuthority::new).toList();
    }

    /**
     * Builds Spring Security UserDetails from User.
     *
     * @param user the user entity
     * @param authorities the granted authorities
     * @return UserDetails for Spring Security
     */
    private UserDetails buildUserDetails(final User user, final Collection<? extends GrantedAuthority> authorities) {
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .build();
    }

    /**
     * Logs authentication error to both audit and application logs.
     *
     * @param username the username that failed
     * @param e the exception that occurred
     */
    private void logAuthenticationError(final String username, final Exception e) {
        AUDIT_LOGGER.error("Error loading user details: username={}, error={}", username, e.getMessage());
        LOGGER.error("Unexpected error loading user: {}", username, e);
    }
}
