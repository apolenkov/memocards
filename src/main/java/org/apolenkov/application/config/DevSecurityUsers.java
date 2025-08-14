package org.apolenkov.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@Profile({"dev", "memory"})
public class DevSecurityUsers {

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("u")
                .password(encoder.encode("u"))
                .roles("USER")
                .build());
        manager.createUser(User.withUsername("a")
                .password(encoder.encode("a"))
                .roles("USER", "ADMIN")
                .build());
        return manager;
    }
}
