package org.apolenkov.application.config.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.logging.MdcFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the application.
 * Uses VaadinWebSecurity with modern security practices for Vaadin 24.x.
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig extends VaadinWebSecurity {

    private final MdcFilter mdcFilter;

    /**
     * Creates security configuration.
     *
     * @param mdcFilterParam the MDC filter for request logging
     */
    public SecurityConfig(final MdcFilter mdcFilterParam) {
        this.mdcFilter = mdcFilterParam;
    }

    /**
     * Uses VaadinWebSecurity with enhanced CSRF protection and security headers.
     *
     * @param http the HttpSecurity builder
     * @throws Exception if configuration fails
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        setLoginView(http, RouteConstants.ROOT_PATH + RouteConstants.LOGIN_ROUTE);

        // Require authentication for all actuator endpoints
        // This must be configured BEFORE calling super.configure()
        http.authorizeHttpRequests(
                auth -> auth.requestMatchers(RouteConstants.ACTUATOR_BASE_PATH).authenticated());

        // Call parent configuration which will add .anyRequest().authenticated()
        super.configure(http);

        // Authentication and access control
        http.exceptionHandling(ex -> ex.authenticationEntryPoint(
                new LoginUrlAuthenticationEntryPoint(RouteConstants.ROOT_PATH + RouteConstants.LOGIN_ROUTE)));

        // Logout configuration with session cleanup
        http.logout(logout -> logout.logoutUrl(RouteConstants.ROOT_PATH + RouteConstants.LOGOUT_ROUTE)
                .logoutSuccessUrl(RouteConstants.ROOT_PATH + RouteConstants.HOME_ROUTE)
                .deleteCookies("JSESSIONID", "remember-me")
                .invalidateHttpSession(true));

        // Enhanced security headers for protection against common attacks
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .contentTypeOptions(contentTypeOptions -> {}) // MIME sniffing protection
                .httpStrictTransportSecurity(
                        hstsConfig -> hstsConfig.maxAgeInSeconds(31536000))); // Enforce HTTPS for 1 year

        // Add filters for request processing
        http.addFilterBefore(mdcFilter, UsernamePasswordAuthenticationFilter.class);
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
