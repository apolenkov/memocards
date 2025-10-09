package org.apolenkov.application.service.auth;

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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

/**
 * Service for authentication operations.
 * Handles user authentication and session persistence for Vaadin applications.
 */
@Service
public class AuthService {

    private final AuthenticationConfiguration authenticationConfiguration;

    /**
     * Creates a new AuthService with authentication configuration.
     *
     * @param authConfig Spring Security authentication configuration
     * @throws IllegalArgumentException if authConfig is null
     */
    public AuthService(final AuthenticationConfiguration authConfig) {
        if (authConfig == null) {
            throw new IllegalArgumentException("AuthenticationConfiguration cannot be null");
        }
        this.authenticationConfiguration = authConfig;
    }

    /**
     * Authenticates user and persists authentication session.
     * Performs user authentication using Spring Security's authentication manager
     * and persists authentication context to HTTP session for subsequent requests.
     *
     * @param username email address of user to authenticate
     * @param rawPassword plain text password for authentication
     * @throws IllegalArgumentException if authentication fails due to invalid credentials or invalid parameters
     */
    public void authenticateAndPersist(final String username, final String rawPassword) {
        validateCredentials(username, rawPassword);

        Authentication auth = performAuthentication(username, rawPassword);
        persistAuthenticationContext(auth);
    }

    /**
     * Validates authentication credentials.
     *
     * @param username the username to validate
     * @param rawPassword the password to validate
     * @throws IllegalArgumentException if credentials are invalid
     */
    private void validateCredentials(final String username, final String rawPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
    }

    /**
     * Performs authentication using Spring Security.
     *
     * @param username the username
     * @param rawPassword the password
     * @return authenticated Authentication object
     * @throws IllegalArgumentException if authentication fails
     */
    private Authentication performAuthentication(final String username, final String rawPassword) {
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(username, rawPassword);
        try {
            return authenticationConfiguration.getAuthenticationManager().authenticate(authRequest);
        } catch (Exception e) {
            throw new IllegalArgumentException("Authentication failed", e);
        }
    }

    /**
     * Persists authentication context to HTTP session.
     *
     * @param auth the authenticated Authentication object
     */
    private void persistAuthenticationContext(final Authentication auth) {
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
}
