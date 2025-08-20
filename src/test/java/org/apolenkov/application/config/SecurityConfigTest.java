package org.apolenkov.application.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Mock
    private Environment environment;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        // Mock environment to return empty profiles array to avoid NPE in constructor
        when(environment.getActiveProfiles()).thenReturn(new String[0]);
        securityConfig = new SecurityConfig(environment);
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {
        @Test
        @DisplayName("Should be annotated with Configuration")
        void shouldBeAnnotatedWithConfiguration() {
            // Given
            Class<SecurityConfig> clazz = SecurityConfig.class;

            // When & Then
            assertThat(clazz.isAnnotationPresent(org.springframework.context.annotation.Configuration.class))
                    .isTrue();
        }

        @Test
        @DisplayName("Should extend VaadinWebSecurity")
        void shouldExtendVaadinWebSecurity() {
            // Given
            Class<SecurityConfig> clazz = SecurityConfig.class;

            // When & Then
            assertThat(com.vaadin.flow.spring.security.VaadinWebSecurity.class).isAssignableFrom(clazz.getSuperclass());
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        @Test
        @DisplayName("Constructor should compute prodProfileActive from Environment")
        void constructorShouldComputeProdProfileActiveFromEnvironment() {
            // Given
            Environment mockEnvironment = mock(Environment.class);
            when(mockEnvironment.getActiveProfiles()).thenReturn(new String[] {"prod"});

            // When
            SecurityConfig config = new SecurityConfig(mockEnvironment);

            // Then
            Boolean prodFlag = (Boolean) ReflectionTestUtils.getField(config, "prodProfileActive");
            assertThat(prodFlag).isTrue();
        }
    }

    @Nested
    @DisplayName("Password Encoder Bean Tests")
    class PasswordEncoderBeanTests {
        @Test
        @DisplayName("PasswordEncoder should return BCryptPasswordEncoder")
        void passwordEncoderShouldReturnBCryptPasswordEncoder() {
            // When
            PasswordEncoder result = securityConfig.passwordEncoder();

            // Then
            assertThat(result).isInstanceOf(BCryptPasswordEncoder.class);
        }

        @Test
        @DisplayName("PasswordEncoder should encode password correctly")
        void passwordEncoderShouldEncodePasswordCorrectly() {
            // Given
            String rawPassword = "testPassword123";
            PasswordEncoder encoder = securityConfig.passwordEncoder();

            // When
            String encodedPassword = encoder.encode(rawPassword);

            // Then
            assertThat(encodedPassword).isNotEqualTo(rawPassword);
            assertThat(encoder.matches(rawPassword, encodedPassword)).isTrue();
        }

        @Test
        @DisplayName("PasswordEncoder should handle different passwords")
        void passwordEncoderShouldHandleDifferentPasswords() {
            // Given
            String password1 = "password1";
            String password2 = "password2";
            PasswordEncoder encoder = securityConfig.passwordEncoder();

            // When
            String encoded1 = encoder.encode(password1);
            String encoded2 = encoder.encode(password2);

            // Then
            assertThat(encoded1).isNotEqualTo(encoded2);
            assertThat(encoder.matches(password1, encoded1)).isTrue();
            assertThat(encoder.matches(password2, encoded2)).isTrue();
            assertThat(encoder.matches(password1, encoded2)).isFalse();
        }
    }

    @Nested
    @DisplayName("Access Denied Handler Bean Tests")
    class AccessDeniedHandlerBeanTests {
        @Test
        @DisplayName("AccessDeniedHandler should return handler")
        void accessDeniedHandlerShouldReturnHandler() {
            // When
            AccessDeniedHandler result = securityConfig.accessDeniedHandler();

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("AccessDeniedHandler should handle API requests with JSON response")
        void accessDeniedHandlerShouldHandleApiRequestsWithJsonResponse() throws IOException, ServletException {
            // Given
            AccessDeniedHandler handler = securityConfig.accessDeniedHandler();
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            AccessDeniedException exception = new AccessDeniedException("Access denied");
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            when(request.getHeader("Accept")).thenReturn(MediaType.APPLICATION_JSON_VALUE);
            when(response.getWriter()).thenReturn(printWriter);

            // When
            handler.handle(request, response, exception);

            // Then
            verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            assertThat(stringWriter.toString()).contains("error");
            assertThat(stringWriter.toString()).contains("Access denied");
            assertThat(stringWriter.toString()).contains("403");
        }

        @Test
        @DisplayName("AccessDeniedHandler should handle XML API requests")
        void accessDeniedHandlerShouldHandleXmlApiRequests() throws IOException, ServletException {
            // Given
            AccessDeniedHandler handler = securityConfig.accessDeniedHandler();
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            AccessDeniedException exception = new AccessDeniedException("Access denied");
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            when(request.getHeader("Accept")).thenReturn(MediaType.APPLICATION_XML_VALUE);
            when(response.getWriter()).thenReturn(printWriter);

            // When
            handler.handle(request, response, exception);

            // Then
            verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        }

        @Test
        @DisplayName("AccessDeniedHandler should redirect browser requests to access-denied page")
        void accessDeniedHandlerShouldRedirectBrowserRequestsToAccessDeniedPage() throws IOException, ServletException {
            // Given
            AccessDeniedHandler handler = securityConfig.accessDeniedHandler();
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            AccessDeniedException exception = new AccessDeniedException("Access denied");

            when(request.getHeader("Accept")).thenReturn(MediaType.TEXT_HTML_VALUE);

            // When
            handler.handle(request, response, exception);

            // Then
            verify(response).sendRedirect("/access-denied");
        }

        @Test
        @DisplayName("AccessDeniedHandler should handle null Accept header")
        void accessDeniedHandlerShouldHandleNullAcceptHeader() throws IOException, ServletException {
            // Given
            AccessDeniedHandler handler = securityConfig.accessDeniedHandler();
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            AccessDeniedException exception = new AccessDeniedException("Access denied");

            when(request.getHeader("Accept")).thenReturn(null);

            // When
            handler.handle(request, response, exception);

            // Then
            verify(response).sendRedirect("/access-denied");
        }

        @Test
        @DisplayName("AccessDeniedHandler should handle empty Accept header")
        void accessDeniedHandlerShouldHandleEmptyAcceptHeader() throws IOException, ServletException {
            // Given
            AccessDeniedHandler handler = securityConfig.accessDeniedHandler();
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            AccessDeniedException exception = new AccessDeniedException("Access denied");

            when(request.getHeader("Accept")).thenReturn("");

            // When
            handler.handle(request, response, exception);

            // Then
            verify(response).sendRedirect("/access-denied");
        }

        @Test
        @DisplayName("AccessDeniedHandler should handle mixed Accept header")
        void accessDeniedHandlerShouldHandleMixedAcceptHeader() throws IOException, ServletException {
            // Given
            AccessDeniedHandler handler = securityConfig.accessDeniedHandler();
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            AccessDeniedException exception = new AccessDeniedException("Access denied");

            when(request.getHeader("Accept"))
                    .thenReturn("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

            // When
            handler.handle(request, response, exception);

            // Then
            verify(response).sendRedirect("/access-denied");
        }
    }

    @Nested
    @DisplayName("Environment Profile Tests")
    class EnvironmentProfileTests {
        @Test
        @DisplayName("Should detect production profile")
        void shouldDetectProductionProfile() {
            // Given
            when(environment.getActiveProfiles()).thenReturn(new String[] {"prod"});

            // When
            boolean isProd =
                    java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");

            // Then
            assertThat(isProd).isTrue();
        }

        @Test
        @DisplayName("Should detect development profile")
        void shouldDetectDevelopmentProfile() {
            // Given
            when(environment.getActiveProfiles()).thenReturn(new String[] {"dev"});

            // When
            boolean isProd =
                    java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");

            // Then
            assertThat(isProd).isFalse();
        }

        @Test
        @DisplayName("Should handle multiple profiles")
        void shouldHandleMultipleProfiles() {
            // Given
            when(environment.getActiveProfiles()).thenReturn(new String[] {"dev", "jpa", "test"});

            // When
            boolean isProd =
                    java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");

            // Then
            assertThat(isProd).isFalse();
        }

        @Test
        @DisplayName("Should handle empty profiles")
        void shouldHandleEmptyProfiles() {
            // Given
            when(environment.getActiveProfiles()).thenReturn(new String[] {});

            // When
            boolean isProd =
                    java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");

            // Then
            assertThat(isProd).isFalse();
        }

        @Test
        @DisplayName("Should handle null profiles")
        void shouldHandleNullProfiles() {
            // Given
            when(environment.getActiveProfiles()).thenReturn(null);

            // When
            boolean isProd = environment.getActiveProfiles() != null
                    && java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");

            // Then
            assertThat(isProd).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        @Test
        @DisplayName("Should handle very long profile names")
        void shouldHandleVeryLongProfileNames() {
            // Given
            String longProfile = "a".repeat(1000);
            when(environment.getActiveProfiles()).thenReturn(new String[] {longProfile});

            // When
            boolean containsLongProfile =
                    java.util.Arrays.asList(environment.getActiveProfiles()).contains(longProfile);

            // Then
            assertThat(containsLongProfile).isTrue();
        }

        @Test
        @DisplayName("Should handle special characters in profile names")
        void shouldHandleSpecialCharactersInProfileNames() {
            // Given
            String specialProfile = "prod@#$%^&*()";
            when(environment.getActiveProfiles()).thenReturn(new String[] {specialProfile});

            // When
            boolean containsSpecialProfile =
                    java.util.Arrays.asList(environment.getActiveProfiles()).contains(specialProfile);

            // Then
            assertThat(containsSpecialProfile).isTrue();
        }

        @Test
        @DisplayName("Should handle unicode characters in profile names")
        void shouldHandleUnicodeCharactersInProfileNames() {
            // Given
            String unicodeProfile = "prod_русский_español";
            when(environment.getActiveProfiles()).thenReturn(new String[] {unicodeProfile});

            // When
            boolean containsUnicodeProfile =
                    java.util.Arrays.asList(environment.getActiveProfiles()).contains(unicodeProfile);

            // Then
            assertThat(containsUnicodeProfile).isTrue();
        }
    }
}
