package org.apolenkov.application.config.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Servlet filter that populates MDC with request-scoped information for logging.
 * Provides requestId for request correlation and userId for user-specific logging.
 *
 * <p>This class is designed for extension. Subclasses should override {@link #doFilter}
 * to add custom MDC processing while calling the parent method to maintain
 * the base functionality.
 */
@Component
public final class MdcFilter implements Filter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final String USER_ID_MDC_KEY = "userId";

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Generate or extract request ID
            String requestId = extractOrGenerateRequestId(httpRequest);
            MDC.put(REQUEST_ID_MDC_KEY, requestId);

            // Set response header for client correlation
            httpResponse.setHeader(REQUEST_ID_HEADER, requestId);

            // Extract user ID from security context
            String userId = extractUserId();
            if (userId != null) {
                MDC.put(USER_ID_MDC_KEY, userId);
            }

            chain.doFilter(request, response);

        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.remove(REQUEST_ID_MDC_KEY);
            MDC.remove(USER_ID_MDC_KEY);
        }
    }

    private String extractOrGenerateRequestId(final HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        return requestId != null ? requestId : UUID.randomUUID().toString().substring(0, 8);
    }

    private String extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }
        return null;
    }
}
