package org.apolenkov.application.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Configuration class for internationalization (i18n) support.
 *
 * <p>This configuration class sets up Spring's MessageSource infrastructure
 * for internationalization support throughout the application. It configures
 * a ResourceBundleMessageSource that reads from properties files and provides
 * fallback mechanisms for missing translations.</p>
 *
 * <p>The configuration supports UTF-8 encoding for proper handling of
 * international characters and uses the code as default message when
 * translations are not available.</p>
 *
 */
@Configuration
public class I18NConfig {

    /**
     * Creates a MessageSource bean for internationalization support.
     *
     * <p>This bean provides the core internationalization functionality
     * for the application. It reads message bundles from the "i18n/messages"
     * base name and configures UTF-8 encoding for proper character handling.</p>
     *
     * <p>The message source is configured to use the message code as the
     * default message when a translation is not found, providing graceful
     * fallback behavior.</p>
     *
     * @return a configured MessageSource for internationalization
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("i18n/messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        return ms;
    }
}
