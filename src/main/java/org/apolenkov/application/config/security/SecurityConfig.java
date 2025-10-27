package org.apolenkov.application.config.security;

import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.logging.MdcFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the application.
 * Provides authentication, authorization, and security headers.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
public class SecurityConfig {

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
     * Configures HTTP security including CSRF protection and security headers.
     *
     * @param http the HttpSecurity builder
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        // Configure public resources and actuator endpoints
        http.authorizeHttpRequests(auth -> auth
                // Public static resources (icons, images, etc.)
                .requestMatchers("/icons/**")
                .permitAll()
                // Actuator endpoints: health, info, metrics, env are public
                .requestMatchers(
                        RouteConstants.ACTUATOR_HEALTH,
                        RouteConstants.ACTUATOR_INFO,
                        RouteConstants.ACTUATOR_METRICS,
                        RouteConstants.ACTUATOR_ENV)
                .permitAll()
                .requestMatchers(RouteConstants.ACTUATOR_BASE_PATH)
                .authenticated());

        // Configure Vaadin's security using VaadinSecurityConfigurer
        http.with(
                VaadinSecurityConfigurer.vaadin(),
                configurer -> configurer.loginView(
                        RouteConstants.ROOT_PATH + RouteConstants.LOGIN_ROUTE, RouteConstants.ROOT_PATH));

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

        return http.build();
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
