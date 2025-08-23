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
 * Automatic authentication filter for development environment.
 *
 * <p>Automatically authenticates a predefined user to streamline development workflow.
 * Only operates in non-production profiles and skips authentication endpoints.</p>
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
     * Creates auto-login filter with configuration and dependencies.
     *
     * @param autoLoginEnabled whether automatic login is enabled (default: false)
     * @param autoLoginUser email of user to automatically authenticate (default: user@example.com)
     * @param userDetailsService service for loading user details
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
     * Applies automatic authentication when conditions are met.
     *
     * <p>Checks if auto-login is enabled and user needs authentication.
     * Loads predefined user and sets up authentication context early in request chain.</p>
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
     * Checks if request is eligible for automatic authentication.
     *
     * <p>Excludes authentication endpoints to avoid conflicts with normal auth flow.</p>
     *
     * @param request the HTTP request to evaluate
     * @return true if request is eligible for auto-login
     */
    private boolean isEligibleRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Exclude authentication endpoints to avoid conflicts with normal auth flow
        return !(path != null && (path.startsWith("/login") || path.startsWith("/logout")));
    }

    // Reserved for future use if we need to filter out Vaadin internal requests
}
