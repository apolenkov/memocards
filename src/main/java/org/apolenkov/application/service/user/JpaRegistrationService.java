package org.apolenkov.application.service.user;

import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA-based implementation of user registration service with secure password hashing.
 */
@Service
@Profile({"dev", "prod"})
public class JpaRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new JpaRegistrationService with required dependencies.
     *
     * @param userRepository the repository for user persistence operations
     * @param passwordEncoder the encoder for secure password hashing
     */
    public JpaRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
    @Transactional
    public void register(String email, String name, String rawPassword) {
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("User already exists");
        });
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.addRole(org.apolenkov.application.config.SecurityConstants.ROLE_USER);

        userRepository.save(user);
    }
}
