package org.apolenkov.application.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClockConfig Tests")
class ClockConfigTest {

    private ClockConfig clockConfig;

    @BeforeEach
    void setUp() {
        clockConfig = new ClockConfig();
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {
        @Test
        @DisplayName("Should be annotated with Configuration")
        void shouldBeAnnotatedWithConfiguration() {
            // Given
            Class<ClockConfig> clazz = ClockConfig.class;

            // When & Then
            assertThat(clazz.isAnnotationPresent(org.springframework.context.annotation.Configuration.class))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("System Clock Bean Tests")
    class SystemClockBeanTests {
        @Test
        @DisplayName("SystemClock should return Clock instance")
        void systemClockShouldReturnClockInstance() {
            // When
            Clock result = clockConfig.systemClock();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(Clock.class);
        }

        @Test
        @DisplayName("SystemClock should return system default zone clock")
        void systemClockShouldReturnSystemDefaultZoneClock() {
            // When
            Clock result = clockConfig.systemClock();

            // Then
            assertThat(result.getZone()).isEqualTo(ZoneId.systemDefault());
        }

        @Test
        @DisplayName("SystemClock should be functional")
        void systemClockShouldBeFunctional() {
            // Given
            Clock clock = clockConfig.systemClock();
            Instant before = Instant.now();

            // When
            Instant clockInstant = clock.instant();
            Instant after = Instant.now();

            // Then
            assertThat(clockInstant).isBetween(before, after);
        }

        @Test
        @DisplayName("SystemClock should return different instances")
        void systemClockShouldReturnDifferentInstances() {
            // When
            Clock clock1 = clockConfig.systemClock();
            Clock clock2 = clockConfig.systemClock();

            // Then
            assertThat(clock1).isNotSameAs(clock2);
        }

        @Test
        @DisplayName("SystemClock should have correct zone")
        void systemClockShouldHaveCorrectZone() {
            // Given
            ZoneId expectedZone = ZoneId.systemDefault();

            // When
            Clock clock = clockConfig.systemClock();

            // Then
            assertThat(clock.getZone()).isEqualTo(expectedZone);
        }

        @Test
        @DisplayName("SystemClock should be able to get current time")
        void systemClockShouldBeAbleToGetCurrentTime() {
            // Given
            Clock clock = clockConfig.systemClock();
            long before = System.currentTimeMillis();

            // When
            long clockMillis = clock.millis();
            long after = System.currentTimeMillis();

            // Then
            assertThat(clockMillis).isBetween(before, after);
        }

        @Test
        @DisplayName("SystemClock should be able to get current instant")
        void systemClockShouldBeAbleToGetCurrentInstant() {
            // Given
            Clock clock = clockConfig.systemClock();
            Instant before = Instant.now();

            // When
            Instant clockInstant = clock.instant();
            Instant after = Instant.now();

            // Then
            assertThat(clockInstant).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        @Test
        @DisplayName("Should handle multiple clock calls")
        void shouldHandleMultipleClockCalls() {
            // Given
            Clock clock = clockConfig.systemClock();

            // When
            Instant instant1 = clock.instant();
            Instant instant2 = clock.instant();
            Instant instant3 = clock.instant();

            // Then
            assertThat(instant1).isBeforeOrEqualTo(instant2);
            assertThat(instant2).isBeforeOrEqualTo(instant3);
        }

        @Test
        @DisplayName("Should handle clock zone operations")
        void shouldHandleClockZoneOperations() {
            // Given
            Clock clock = clockConfig.systemClock();
            ZoneId systemZone = ZoneId.systemDefault();

            // When
            ZoneId clockZone = clock.getZone();
            Clock withZone = clock.withZone(systemZone);

            // Then
            assertThat(clockZone).isEqualTo(systemZone);
            assertThat(withZone.getZone()).isEqualTo(systemZone);
        }

        @Test
        @DisplayName("Should handle clock offset operations")
        void shouldHandleClockOffsetOperations() {
            // Given
            Clock clock = clockConfig.systemClock();

            // When
            Clock offsetClock = Clock.offset(clock, java.time.Duration.ZERO);

            // Then
            assertThat(offsetClock).isNotNull();
            assertThat(offsetClock.getZone()).isEqualTo(clock.getZone());
        }
    }
}
