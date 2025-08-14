package org.apolenkov.application.service.user;

import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile({"jpa", "prod"})
public class JpaRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public JpaRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
