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
 * JDBC-based implementation of Spring Security's UserDetailsService with role mapping and validation.
 */
@Service
@Profile({"dev", "prod"})
public class JdbcUserDetailsService implements UserDetailsService {

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
     * @throws UsernameNotFoundException if no user exists with the specified email
     * @throws IllegalStateException if the user exists but has no password hash
     */
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
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
