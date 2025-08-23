package org.apolenkov.application.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that ensures CSRF tokens are generated and available for Vaadin applications.
 * Touches CSRF token on GET requests to trigger Spring Security token generation.
 * Selectively processes requests, excluding Vaadin internal endpoints and static resources.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CsrfCookieFilter.class);

    /**
     * Ensures CSRF tokens are generated for GET requests.
     * Accesses CSRF token attribute to trigger Spring Security token generation
     * and sets appropriate cookie.
     *
     * @param request the HTTP request being processed
     * @param response the HTTP response
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Only process GET requests (CSRF tokens are typically needed for forms)
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            // Access CSRF token attribute to trigger Spring Security token generation
            Object attr = request.getAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName());
            if (attr instanceof org.springframework.security.web.csrf.CsrfToken token) {
                String tokenValue = token.getToken();
                // Log token generation success/failure for debugging
                LOGGER.debug(
                        "CSRF token generated for request: {} -> {}",
                        request.getRequestURI(),
                        tokenValue != null ? "SUCCESS" : "FAILED");
            } else {
                LOGGER.debug("No CSRF token found in request attributes for: {}", request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Excludes Vaadin internal endpoints and static resources from CSRF processing.
     *
     * @param request the HTTP request to evaluate
     * @return true if request should not be filtered
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }
        // Exclude Vaadin internal endpoints and static resources from CSRF processing
        return uri.startsWith("/VAADIN/") // Vaadin internal communication
                || uri.startsWith("/HEARTBEAT/") // Vaadin heartbeat/ping
                || uri.startsWith("/UIDL/") // Vaadin UIDL (UI definition)
                || uri.startsWith("/css/") // CSS stylesheets
                || uri.startsWith("/images/") // Image files
                || uri.startsWith("/icons/") // Icon files
                || uri.startsWith("/js/") // JavaScript files
                || uri.startsWith("/webjars/") // WebJar resources
                || uri.startsWith("/frontend/") // Frontend build files
                || uri.equals("/favicon.ico"); // Browser favicon
    }
}
