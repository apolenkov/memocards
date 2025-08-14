package org.apolenkov.application.config;

import org.apolenkov.application.service.user.JpaUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * In JPA profile, UserDetailsService provided by a dedicated adapter.
 * This placeholder ensures no in-memory users are registered in prod/jpa.
 */
@Configuration
@Profile({"jpa", "prod"})
public class JpaSecurityUsers {

    @Bean
    public UserDetailsService userDetailsService(JpaUserDetailsService jpaUserDetailsService) {
        return jpaUserDetailsService;
    }
}
