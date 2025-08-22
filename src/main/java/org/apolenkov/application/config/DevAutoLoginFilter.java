package org.apolenkov.application.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Developer convenience filter for automatic authentication in development environment.
 *
 * <p>This filter automatically authenticates a predefined user when enabled through
 * configuration properties. It's designed to streamline development workflow by
 * eliminating the need for manual login during development and testing.</p>
 *
 * <p>The filter only operates in non-production profiles and can be enabled/disabled
 * through configuration. It automatically skips authentication endpoints to avoid
 * conflicts with normal authentication flows.</p>
 *
 */
@Component
@Profile("!prod")
public class DevAutoLoginFilter extends OncePerRequestFilter {

    private static final Logger appLogger = LoggerFactory.getLogger(DevAutoLoginFilter.class);

    private final boolean autoLoginEnabled;
    private final String autoLoginUser;
    private final UserDetailsService userDetailsService;
    private final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

    /**
     * Constructs a new DevAutoLoginFilter with configuration and dependencies.
     *
     * @param autoLoginEnabled whether automatic login is enabled (default: false)
     * @param autoLoginUser the email of the user to automatically authenticate (default: user@example.com)
     * @param userDetailsService the service for loading user details
     */
    public DevAutoLoginFilter(
            @Value("${dev.auto-login.enabled:false}") boolean autoLoginEnabled,
            @Value("${dev.auto-login.user:user@example.com}") String autoLoginUser,
            UserDetailsService userDetailsService) {
        this.autoLoginEnabled = autoLoginEnabled;
        this.autoLoginUser = autoLoginUser;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Processes each request to apply automatic authentication when appropriate.
     *
     * <p>This method checks if auto-login is enabled and if the current request
     * is eligible for automatic authentication. If conditions are met, it loads
     * the predefined user and sets up the authentication context.</p>
     *
     * <p>The filter performs authentication as early as possible in the request
     * chain to ensure subsequent components have access to the authenticated
     * user context.</p>
     *
     * @param request the HTTP request being processed
     * @param response the HTTP response
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // Check current authentication status early in the request chain
        Authentication currentAuthentication =
                SecurityContextHolder.getContext().getAuthentication();
        // Determine if user needs authentication (null or anonymous)
        boolean notAuthenticated =
                currentAuthentication == null || authenticationTrustResolver.isAnonymous(currentAuthentication);

        // Apply auto-login if enabled, user is not authenticated, and request is eligible
        if (autoLoginEnabled && notAuthenticated && isEligibleRequest(request)) {
            try {
                // Load predefined user and create authentication token
                UserDetails userDetails = userDetailsService.loadUserByUsername(autoLoginUser);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, userDetails.getPassword(), userDetails.getAuthorities());
                // Set authentication in security context for this request
                SecurityContextHolder.getContext().setAuthentication(authentication);
                appLogger.info("Dev auto-login applied for user: {}", autoLoginUser);
            } catch (Exception e) {
                // Log failure but don't break the request flow
                appLogger.warn("Dev auto-login failed for user: {}. Reason: {}", autoLoginUser, e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Determines if a request is eligible for automatic authentication.
     *
     * <p>This method checks if the request path should be considered for
     * automatic authentication. It excludes authentication-related endpoints
     * like login and logout to avoid conflicts with normal authentication flows.</p>
     *
     * @param request the HTTP request to evaluate
     * @return true if the request is eligible for auto-login, false otherwise
     */
    private boolean isEligibleRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Exclude authentication endpoints to avoid conflicts with normal auth flow
        return !(path != null && (path.startsWith("/login") || path.startsWith("/logout")));
    }

    // Reserved for future use if we need to filter out Vaadin internal requests
}
