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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

/**
 * Facade service for user authentication and registration operations.
 * Provides simplified interface for user authentication and registration,
 * coordinating between Spring Security's authentication manager and user registration service.
 */
@Component
public class AuthFacade {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final org.apolenkov.application.service.user.JpaRegistrationService jpaRegistrationService;

    /**
     * Creates AuthFacade with required dependencies.
     *
     * @param authenticationConfiguration Spring Security authentication configuration
     * @param jpaRegistrationService service for user registration operations
     */
    public AuthFacade(
            AuthenticationConfiguration authenticationConfiguration,
            org.apolenkov.application.service.user.JpaRegistrationService jpaRegistrationService) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jpaRegistrationService = jpaRegistrationService;
    }

    /**
     * Registers new user with specified credentials.
     * Validates password against security requirements and creates new user account.
     * Password must be at least 8 characters and contain letters and digits.
     *
     * @param username email address to use as username
     * @param rawPassword plain text password to validate and hash
     * @throws IllegalArgumentException if password does not meet security requirements
     */
    public void registerUser(String username, String rawPassword) {
        if (rawPassword == null
                || rawPassword.length() < 8
                || !rawPassword.matches(".*\\d.*")
                || !rawPassword.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException("Password must be at least 8 characters and contain letters and digits");
        }

        jpaRegistrationService.register(username, username, rawPassword);
    }

    /**
     * Authenticates user and persists authentication session.
     * Performs user authentication using Spring Security's authentication manager
     * and persists authentication context to HTTP session for subsequent requests.
     *
     * @param username email address of user to authenticate
     * @param rawPassword plain text password for authentication
     * @throws InvalidPasswordException if authentication fails due to invalid credentials
     */
    public void authenticateAndPersist(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(username, rawPassword);
        Authentication auth;
        try {
            auth = authenticationConfiguration.getAuthenticationManager().authenticate(authRequest);
        } catch (Exception e) {
            throw new InvalidPasswordException(e);
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

    /**
     * Exception thrown when user authentication fails.
     * Wraps underlying authentication failure and provides consistent
     * error handling interface for authentication operations.
     */
    public static class InvalidPasswordException extends RuntimeException {
        /**
         * Creates InvalidPasswordException with specified cause.
         *
         * @param cause the underlying cause of authentication failure (may be null)
         */
        public InvalidPasswordException(Throwable cause) {
            super(cause);
        }
    }
}
