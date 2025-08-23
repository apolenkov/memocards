package org.apolenkov.application.config;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import jakarta.servlet.http.HttpServletResponse;
import org.apolenkov.application.views.LoginView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Spring Security configuration for the application.
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig extends VaadinWebSecurity {
    private final boolean prodProfileActive;
    private final DevAutoLoginFilter devAutoLoginFilter;

    /**
     * Creates security configuration with environment and auto-login filter.
     */
    @Autowired
    public SecurityConfig(Environment environment, DevAutoLoginFilter devAutoLoginFilter) {
        // Determine if production profile is active for security configuration
        String[] profiles = environment != null ? environment.getActiveProfiles() : null;
        this.prodProfileActive =
                profiles != null && java.util.Arrays.asList(profiles).contains("prod");
        this.devAutoLoginFilter = devAutoLoginFilter;
    }

    /**
     * Sets up HTTP security with authentication, CSRF protection, and CSP.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);

        setLoginView(http, LoginView.class, "/");

        // CSRF protection via HttpOnly cookies for security
        CookieCsrfTokenRepository csrfRepo = new CookieCsrfTokenRepository();
        csrfRepo.setCookieCustomizer(cookie -> cookie.httpOnly(true));
        http.csrf(csrf -> csrf.csrfTokenRepository(csrfRepo).ignoringRequestMatchers("/login"));

        // Authentication and access control
        http.exceptionHandling(ex -> ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                .accessDeniedHandler(accessDeniedHandler()));

        // Logout configuration with session cleanup
        http.logout(logout -> logout.logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID", "remember-me")
                .invalidateHttpSession(true));

        // Dev-only auto-login for development convenience
        if (!prodProfileActive) {
            http.addFilterBefore(devAutoLoginFilter, UsernamePasswordAuthenticationFilter.class);
        }

        // Content Security Policy configuration based on profile
        if (prodProfileActive) {
            // Strict CSP for production: minimal permissions, security-focused
            http.headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; "
                    + "base-uri 'self'; form-action 'self'; object-src 'none'; frame-ancestors 'none'; "
                    + "img-src 'self' data:; style-src 'self' 'unsafe-inline'; "
                    + "script-src 'self'; connect-src 'self'; font-src 'self' data:; worker-src 'self'")));
        } else {
            // Relaxed CSP for development: allows Vite HMR, WebSockets, blob URLs
            http.headers(
                    headers -> headers.contentSecurityPolicy(
                            csp -> csp.policyDirectives(
                                    "default-src 'self'; img-src 'self' data: blob:; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline' 'unsafe-eval' blob:; connect-src 'self' ws: wss:; font-src 'self' data:; frame-src 'self' blob:; worker-src 'self' blob:")));
        }
    }

    /**
     * Creates access denied handler for unauthorized requests.
     *
     * @return handler that returns JSON for API requests or redirects for UI requests
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            String requestURI = request.getRequestURI();
            // Determine if this is an API request or UI request
            boolean isApiRequest = requestURI != null && requestURI.startsWith("/api/");

            if (isApiRequest) {
                // Return RFC 7807 Problem Details JSON for API requests
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/problem+json");
                response.getWriter()
                        .write(String.format(
                                "{\"type\":\"https://httpstatuses.io/403\",\"title\":\"Access Denied\",\"status\":403,\"detail\":\"Access denied for resource\",\"path\":\"%s\",\"timestamp\":\"%s\"}",
                                requestURI, java.time.Instant.now()));
            } else {
                // Redirect to access denied page for UI requests
                response.sendRedirect("/access-denied");
            }
        };
    }

    /**
     * Creates password encoder for secure password hashing.
     *
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
