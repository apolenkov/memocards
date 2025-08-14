package org.apolenkov.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

class AuthFacadeTest {

    private UserDetailsService uds;
    private PasswordEncoder encoder;
    private AuthenticationManager authManager;
    private AuthFacade facade;

    @BeforeEach
    void setUp() {
        uds = new InMemoryUserDetailsManager();
        encoder = mock(PasswordEncoder.class);
        when(encoder.encode("pwd")).thenReturn("ENC");
        authManager = mock(AuthenticationManager.class);
        when(authManager.authenticate(any())).thenReturn(mock(Authentication.class));
        facade = new AuthFacade(uds, encoder, authManager);
    }

    @Test
    void registerAndExists() {
        facade.registerUser("u", "pwd");
        assertThat(facade.userExists("u")).isTrue();
    }
}
