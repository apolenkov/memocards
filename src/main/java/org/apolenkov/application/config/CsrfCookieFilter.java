package org.apolenkov.application.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that ensures CSRF tokens are generated and available for Vaadin applications.
 *
 * <p>This filter is essential for Vaadin applications to ensure CSRF tokens are always
 * available in the browser. It works by touching the CSRF token on GET requests, which
 * triggers Spring Security to generate and set the XSRF cookie.</p>
 *
 * <p>The filter selectively processes requests, excluding Vaadin internal endpoints
 * and static resources to avoid unnecessary processing and potential conflicts.</p>
 *
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CsrfCookieFilter.class);

    /**
     * Processes each request to ensure CSRF tokens are generated when needed.
     *
     * <p>This method checks if the request is a GET request and if a CSRF token
     * is present in the request attributes. By accessing the token, it ensures
     * that Spring Security generates the token and sets the appropriate cookie.</p>
     *
     * <p>The method logs debug information about token generation success or failure
     * to aid in troubleshooting CSRF-related issues.</p>
     *
     * @param request the HTTP request being processed
     * @param response the HTTP response
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
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
     * Determines if a request should be filtered by this CSRF cookie filter.
     *
     * <p>This method excludes Vaadin internal endpoints, static resources, and
     * other requests that don't require CSRF token generation. This prevents
     * unnecessary processing and potential conflicts with Vaadin's internal
     * request handling.</p>
     *
     * @param request the HTTP request to evaluate
     * @return true if the request should not be filtered, false otherwise
     * @throws ServletException if a servlet error occurs
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
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
