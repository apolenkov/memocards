package org.apolenkov.application.service.auth;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

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
     * <p>Failed authentication attempts are automatically logged in performAuthentication()
     * with detailed reason codes for security audit trail (OWASP compliance).
     *
     * @param username email address of user to authenticate
     * @param rawPassword plain text password for authentication
     * @throws IllegalArgumentException if authentication fails due to invalid credentials or invalid parameters
     */
    public void authenticateAndPersist(final String username, final String rawPassword) {
        validateCredentials(username, rawPassword);

        LOGGER.debug("Attempting authentication for user: {}", username);

        // performAuthentication() handles detailed audit logging for failures
        Authentication auth = performAuthentication(username, rawPassword);
        persistAuthenticationContext(auth);

        AUDIT_LOGGER.info("User logged in successfully: {}", username);
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
     * Handles different authentication failure scenarios with detailed audit logging.
     * SecurityAuditAspect tracks additional context (IP, attempt count, user agent) via AOP.
     *
     * @param username the username
     * @param rawPassword the password
     * @return authenticated Authentication object
     * @throws IllegalArgumentException if authentication fails
     */
    @SuppressWarnings("java:S2139") // Security audit requires logging before rethrow (OWASP compliance)
    private Authentication performAuthentication(final String username, final String rawPassword) {
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(username, rawPassword);
        try {
            return authenticationConfiguration.getAuthenticationManager().authenticate(authRequest);
        } catch (BadCredentialsException e) {
            AUDIT_LOGGER.warn("Failed login attempt: username={}, reason=BAD_CREDENTIALS", username);
            throw new IllegalArgumentException("Invalid username or password", e);
        } catch (DisabledException e) {
            AUDIT_LOGGER.warn("Failed login attempt: username={}, reason=ACCOUNT_DISABLED", username);
            throw new IllegalArgumentException("Account is disabled", e);
        } catch (LockedException e) {
            AUDIT_LOGGER.warn("Failed login attempt: username={}, reason=ACCOUNT_LOCKED", username);
            throw new IllegalArgumentException("Account is locked", e);
        } catch (Exception e) {
            AUDIT_LOGGER.warn(
                    "Failed login attempt: username={}, reason=UNKNOWN ({})",
                    username,
                    e.getClass().getSimpleName());
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
