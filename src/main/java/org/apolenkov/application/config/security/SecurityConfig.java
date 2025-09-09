package org.apolenkov.application.config.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.views.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Spring Security configuration for the application.
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig extends VaadinWebSecurity {
    private final boolean prodProfileActive;

    /**
     * Creates security configuration with environment.
     *
     * @param environment Spring environment for profile detection
     */
    public SecurityConfig(final Environment environment) {
        // Determine if production profile is active for security configuration
        String[] profiles = environment != null ? environment.getActiveProfiles() : null;
        this.prodProfileActive =
                profiles != null && java.util.Arrays.asList(profiles).contains("prod");
    }

    /**
     * Sets up HTTP security with authentication, CSRF protection, and CSP.
     */
    @Override
    @SuppressWarnings("java:S4502")
    protected void configure(final HttpSecurity http) throws Exception {
        super.configure(http);

        setLoginView(http, LoginView.class, "/");

        // CSRF protection via HttpOnly cookies for security
        CookieCsrfTokenRepository csrfRepo = new CookieCsrfTokenRepository();
        csrfRepo.setCookieCustomizer(cookie -> cookie.httpOnly(true));
        http.csrf(csrf -> csrf.csrfTokenRepository(csrfRepo).ignoringRequestMatchers("/" + RouteConstants.LOGIN_ROUTE));

        // Authentication and access control
        http.exceptionHandling(ex ->
                ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/" + RouteConstants.LOGIN_ROUTE)));

        // Logout configuration with session cleanup
        http.logout(logout -> logout.logoutUrl("/" + RouteConstants.LOGOUT_ROUTE)
                .logoutSuccessUrl("/" + RouteConstants.HOME_ROUTE)
                .deleteCookies("JSESSIONID", "remember-me")
                .invalidateHttpSession(true));

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
     * Creates password encoder for secure password hashing.
     *
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
