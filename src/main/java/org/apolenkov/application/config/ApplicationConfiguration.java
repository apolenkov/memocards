package org.apolenkov.application.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application-wide configuration for common beans and utilities.
 * Enables AOP for performance monitoring and scheduling for metrics logging.
 */
@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
public class ApplicationConfiguration {

    /**
     * Provides system UTC clock for time operations.
     *
     * @return system UTC clock instance
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
