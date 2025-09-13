package org.apolenkov.application.config.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.views.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

/**
 * Spring Security configuration for the application.
 * Uses VaadinWebSecurity with modern security practices for Vaadin 24.x.
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig extends VaadinWebSecurity {

    /**
     * Creates security configuration.
     */
    public SecurityConfig() {
        // VaadinWebSecurity handles all security configuration automatically
    }

    /**
     * Configures security filter chain with modern security practices for Vaadin 24.x.
     * Uses VaadinWebSecurity with enhanced CSRF protection and security headers.
     *
     * @param http the HttpSecurity builder
     * @throws Exception if configuration fails
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        super.configure(http);

        setLoginView(http, LoginView.class, "/");

        // Authentication and access control
        http.exceptionHandling(ex ->
                ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/" + RouteConstants.LOGIN_ROUTE)));

        // Logout configuration with session cleanup
        http.logout(logout -> logout.logoutUrl("/" + RouteConstants.LOGOUT_ROUTE)
                .logoutSuccessUrl("/" + RouteConstants.HOME_ROUTE)
                .deleteCookies("JSESSIONID", "remember-me")
                .invalidateHttpSession(true));

        // Vaadin automatically handles CSP and security headers
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
