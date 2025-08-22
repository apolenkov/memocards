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
 * Configuration class for bootstrapping domain users in development environment.
 *
 * <p>This class ensures that essential domain users exist in the development
 * environment, including a standard user and an administrator. It creates
 * these users if they don't exist and synchronizes their properties with
 * the current configuration.</p>
 *
 * <p>The bootstrap process runs only in the "dev" profile and creates users
 * with localized names using the application's internationalization system.
 * It also maintains audit trails for role changes.</p>
 *
 */
@Configuration
@Profile({"dev"})
class DevDomainBootstrap {

    private final MessageSource messageSource;

    /**
     * Constructs a new DevDomainBootstrap with the required message source.
     *
     * @param messageSource the message source for internationalized user names
     */
    public DevDomainBootstrap(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Creates a CommandLineRunner bean for ensuring domain users exist.
     *
     * <p>This bean runs early in the application startup process (order 10)
     * and ensures that essential users exist in the system. It creates
     * both a standard user and an administrator with appropriate roles
     * and localized names.</p>
     *
     * <p>The method uses internationalized names from message bundles
     * and falls back to default names if translations are not available.</p>
     *
     * @param users the user repository for user operations
     * @param audit the role audit repository for tracking changes
     * @param passwordEncoder the password encoder for secure password hashing
     * @return a CommandLineRunner that ensures domain users exist
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
     * Synchronizes a user with the desired configuration.
     *
     * <p>This method ensures that a user exists with the specified properties.
     * If the user doesn't exist, it creates a new one. If the user exists,
     * it updates the user's properties to match the desired configuration.</p>
     *
     * <p>The method maintains audit trails for role changes and ensures
     * that passwords are properly hashed before storage.</p>
     *
     * @param users the user repository for user operations
     * @param audit the role audit repository for tracking changes
     * @param passwordEncoder the password encoder for secure password hashing
     * @param email the email address for the user
     * @param rawPassword the plain text password to hash and store
     * @param fullName the display name for the user
     * @param desiredRoles the set of roles the user should have
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
