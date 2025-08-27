package org.apolenkov.application.config;

import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Bootstraps domain users in development environment.
 * Creates essential users (standard user and administrator) with localized names.
 * Runs only in "dev" profile.
 */
@Configuration
@Profile({"dev"})
class DevDomainBootstrap {
    /**
     * Creates CommandLineRunner for ensuring domain users exist.
     *
     * @param users user repository for user operations
     * @param passwordEncoder password encoder for secure password hashing
     * @return CommandLineRunner that ensures domain users exist
     */
    @Bean
    @Order(10)
    CommandLineRunner ensureDomainUsers(final UserRepository users, final PasswordEncoder passwordEncoder) {
        return args -> {
            // Get localized usernames from i18n provider with fallback defaults
            final String userName = "Ivan Petrov";
            final String adminName = "Administrator";

            syncUser(
                    users,
                    passwordEncoder,
                    "user@example.com",
                    "Password1",
                    userName,
                    Set.of(SecurityConstants.ROLE_USER));
            syncUser(
                    users,
                    passwordEncoder,
                    "admin@example.com",
                    "admin",
                    adminName,
                    Set.of(SecurityConstants.ROLE_ADMIN));
        };
    }

    /**
     * Synchronizes user with desired configuration.
     *
     * @param users user repository for user operations
     * @param passwordEncoder password encoder for secure password hashing
     * @param email email address for the user
     * @param rawPassword plain text password to hash and store
     * @param fullName display name for the user
     * @param desiredRoles set of roles the user should have
     */
    private void syncUser(
            final UserRepository users,
            final PasswordEncoder passwordEncoder,
            final String email,
            final String rawPassword,
            final String fullName,
            final Set<String> desiredRoles) {
        Optional<User> existingOpt = users.findByEmail(email);
        if (existingOpt.isEmpty()) {
            // Create new user with specified properties
            User created = new User(null, email, fullName);
            created.setPasswordHash(passwordEncoder.encode(rawPassword));
            desiredRoles.forEach(created::addRole);
            users.save(created);
            return;
        }

        User existing = existingOpt.get();
        // Update existing user properties
        existing.setPasswordHash(passwordEncoder.encode(rawPassword));
        existing.setName(fullName);
        users.save(existing);
    }
}
