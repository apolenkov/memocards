package org.apolenkov.application.service.user;

import java.util.Collection;
import java.util.List;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * JPA-based implementation of Spring Security's UserDetailsService.
 *
 * <p>This service integrates the application's user management with Spring Security
 * by implementing the UserDetailsService interface. It loads user details from the
 * JPA repository and converts them into Spring Security's UserDetails format for
 * authentication and authorization purposes.</p>
 *
 * <p>The service handles role mapping, password validation, and provides fallback
 * role assignment for users without explicit roles. It is only active in specific
 * profiles (dev, jpa, prod) to allow for different authentication strategies.</p>
 *
 */
@Service
@Profile({"dev", "jpa", "prod"})
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructs a new JpaUserDetailsService with the required repository dependency.
     *
     * @param userRepository the repository for user persistence operations
     */
    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user details by username (email) for Spring Security authentication.
     *
     * <p>This method is called by Spring Security during the authentication process.
     * It retrieves user information from the repository and converts it into the
     * format required by Spring Security. The method handles role mapping and
     * provides fallback role assignment for users without explicit roles.</p>
     *
     * <p>The method performs validation to ensure the user has a valid password
     * hash before creating the UserDetails object.</p>
     *
     * @param username the email address of the user to load (Spring Security uses email as username)
     * @return UserDetails object containing user authentication and authorization information
     * @throws UsernameNotFoundException if no user exists with the specified email
     * @throws IllegalStateException if the user exists but has no password hash
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        Collection<? extends GrantedAuthority> authorities = user.getRoles().isEmpty()
                ? List.of(new SimpleGrantedAuthority("ROLE_USER"))
                : user.getRoles().stream().map(SimpleGrantedAuthority::new).toList();
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new IllegalStateException("User has no password hash: " + username);
        }
        String password = user.getPasswordHash();
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(password)
                .authorities(authorities)
                .build();
    }
}
