package org.apolenkov.application.config;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.domain.port.RoleAuditRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
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

    private final MessageSource messageSource;

    /**
     * Creates bootstrap with message source dependency.
     *
     * @param messageSource message source for internationalized user names
     */
    public DevDomainBootstrap(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Creates CommandLineRunner for ensuring domain users exist.
     *
     * @param users user repository for user operations
     * @param audit role audit repository for tracking changes
     * @param passwordEncoder password encoder for secure password hashing
     * @return CommandLineRunner that ensures domain users exist
     */
    @Bean
    @Order(10)
    CommandLineRunner ensureDomainUsers(
            UserRepository users, RoleAuditRepository audit, PasswordEncoder passwordEncoder) {
        return args -> {
            // Get localized user names from message bundles with fallback defaults
            String userName = messageSource.getMessage(
                    "dev.user.user.name", null, "Ivan Petrov", LocaleContextHolder.getLocale());
            String adminName = messageSource.getMessage(
                    "dev.user.admin.name", null, "Administrator", LocaleContextHolder.getLocale());

            syncUser(
                    users,
                    audit,
                    passwordEncoder,
                    "user@example.com",
                    "Password1",
                    userName,
                    Set.of(SecurityConstants.ROLE_USER));
            syncUser(
                    users,
                    audit,
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
     * @param audit role audit repository for tracking changes
     * @param passwordEncoder password encoder for secure password hashing
     * @param email email address for the user
     * @param rawPassword plain text password to hash and store
     * @param fullName display name for the user
     * @param desiredRoles set of roles the user should have
     */
    private void syncUser(
            UserRepository users,
            RoleAuditRepository audit,
            PasswordEncoder passwordEncoder,
            String email,
            String rawPassword,
            String fullName,
            Set<String> desiredRoles) {
        Optional<User> existingOpt = users.findByEmail(email);
        if (existingOpt.isEmpty()) {
            // Create new user with specified properties
            User created = new User(null, email, fullName);
            created.setPasswordHash(passwordEncoder.encode(rawPassword));
            desiredRoles.forEach(created::addRole);
            created = users.save(created);
            // Record initial role assignment in audit log
            audit.recordChange("system", created.getId(), Set.of(), created.getRoles(), LocalDateTime.now());
            return;
        }

        User existing = existingOpt.get();
        // Update existing user properties
        existing.setPasswordHash(passwordEncoder.encode(rawPassword));
        existing.setName(fullName);

        // Check if roles have changed and record audit if necessary
        if (!existing.getRoles().equals(desiredRoles)) {
            Set<String> before = new HashSet<>(existing.getRoles());
            existing.setRoles(new HashSet<>(desiredRoles));
            users.save(existing);
            // Record role change in audit log
            audit.recordChange("system", existing.getId(), before, existing.getRoles(), LocalDateTime.now());
        }
        users.save(existing);
    }
}
