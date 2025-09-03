package org.apolenkov.application.service.user;

import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JDBC-based implementation of user registration service with secure password hashing.
 */
@Service
@Profile({"dev", "prod"})
public class JdbcRegistrationService implements RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new JdbcRegistrationService with required dependencies.
     *
     * @param userRepositoryValue the repository for user persistence operations
     * @param passwordEncoderValue the encoder for secure password hashing
     */
    public JdbcRegistrationService(
            final UserRepository userRepositoryValue, final PasswordEncoder passwordEncoderValue) {
        this.userRepository = userRepositoryValue;
        this.passwordEncoder = passwordEncoderValue;
    }

    /**
     * Registers a new user account with secure password hashing and default USER role.
     * Validates email uniqueness.
     *
     * @param email the email address for the new user account
     * @param name the display name for the new user
     * @param rawPassword the plain text password to be hashed and stored
     * @throws IllegalArgumentException if a user with the specified email already exists
     */
    @Override
    @Transactional
    public void register(final String email, final String name, final String rawPassword) {
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("User already exists");
        });
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.addRole(org.apolenkov.application.config.security.SecurityConstants.ROLE_USER);

        userRepository.save(user);
    }
}
