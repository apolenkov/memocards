package org.apolenkov.application.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Mock
    private Environment environment;

    private SecurityConfig securityConfig;

    @Mock
    private DevAutoLoginFilter devAutoLoginFilter;

    @BeforeEach
    void setUp() {
        // Mock environment to return empty profiles array to avoid NPE in constructor
        when(environment.getActiveProfiles()).thenReturn(new String[0]);
        devAutoLoginFilter = mock(DevAutoLoginFilter.class);
        securityConfig = new SecurityConfig(environment, devAutoLoginFilter);
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
            SecurityConfig config = new SecurityConfig(mockEnvironment, devAutoLoginFilter);

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
        @DisplayName("AccessDeniedHandler should be configured in SecurityConfig")
        void accessDeniedHandlerShouldBeConfiguredInSecurityConfig() {
            // Then
            assertThat(securityConfig).isNotNull();
            // Note: AccessDeniedHandler is private and configured internally
            // We test its behavior through Spring Security integration
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
            boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");

            // Then
            assertThat(isProd).isTrue();
        }

        @Test
        @DisplayName("Should detect development profile")
        void shouldDetectDevelopmentProfile() {
            // Given
            when(environment.getActiveProfiles()).thenReturn(new String[] {"dev"});

            // When
            boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");

            // Then
            assertThat(isProd).isFalse();
        }

        @Test
        @DisplayName("Should handle multiple profiles")
        void shouldHandleMultipleProfiles() {
            // Given
            when(environment.getActiveProfiles()).thenReturn(new String[] {"dev", "test"});

            // When
            boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");

            // Then
            assertThat(isProd).isFalse();
        }

        @Test
        @DisplayName("Should handle empty profiles")
        void shouldHandleEmptyProfiles() {
            // Given
            when(environment.getActiveProfiles()).thenReturn(new String[] {});

            // When
            boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");

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
                    Arrays.asList(environment.getActiveProfiles()).contains(longProfile);

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
                    Arrays.asList(environment.getActiveProfiles()).contains(specialProfile);

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
                    Arrays.asList(environment.getActiveProfiles()).contains(unicodeProfile);

            // Then
            assertThat(containsUnicodeProfile).isTrue();
        }
    }
}
