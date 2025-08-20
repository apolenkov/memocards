package org.apolenkov.application.service;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

@Component
public class AuthFacade {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final org.apolenkov.application.service.user.JpaRegistrationService jpaRegistrationService;

    public AuthFacade(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            AuthenticationConfiguration authenticationConfiguration,
            org.apolenkov.application.service.user.JpaRegistrationService jpaRegistrationService) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationConfiguration = authenticationConfiguration;
        this.jpaRegistrationService = jpaRegistrationService;
    }

    public boolean userExists(String username) {
        try {
            UserDetails existing = userDetailsService.loadUserByUsername(username);
            return existing != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    public void registerUser(String username, String rawPassword) {
        // Simple password policy
        if (rawPassword == null
                || rawPassword.length() < 8
                || !rawPassword.matches(".*\\d.*")
                || !rawPassword.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException("Password must be at least 8 characters and contain letters and digits");
        }

        // Register user via JPA service
        jpaRegistrationService.register(username, username, rawPassword); // name == email fallback
    }

    public void authenticateAndPersist(String username, String rawPassword) {
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(username, rawPassword);
        Authentication auth;
        try {
            auth = authenticationConfiguration.getAuthenticationManager().authenticate(authRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        VaadinServletRequest vsr = (VaadinServletRequest) VaadinService.getCurrentRequest();
        VaadinServletResponse vsp = (VaadinServletResponse) VaadinService.getCurrentResponse();
        if (vsr != null && vsp != null) {
            HttpServletRequest req = vsr.getHttpServletRequest();
            HttpServletResponse resp = vsp.getHttpServletResponse();
            new HttpSessionSecurityContextRepository().saveContext(context, req, resp);
        }
    }

    // no Vaadin lookups
}
