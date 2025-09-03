package org.apolenkov.application.config.vaadin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.apolenkov.application.config.error.ErrorHandlingConfiguration;
import org.apolenkov.application.config.locale.LocaleConfiguration;
import org.apolenkov.application.config.theme.ThemeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Configures Vaadin service with UI initialization listeners.
 *
 * <p>This component is responsible for setting up Vaadin service
 * and delegating UI configuration to specialized components.
 */
@Component
public class VaadinServiceInitializer implements VaadinServiceInitListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(VaadinServiceInitializer.class);

    private final transient ThemeConfiguration themeConfiguration;
    private final transient ErrorHandlingConfiguration errorHandlingConfiguration;
    private final transient LocaleConfiguration localeConfiguration;

    /**
     * Constructs a new {@code VaadinServiceInitializer} with the specified configurations.
     *
     * @param themeConfig the theme configuration to apply
     * @param errorConfig the error handling configuration to apply
     * @param localeConfig the locale configuration to apply
     */
    public VaadinServiceInitializer(
            final ThemeConfiguration themeConfig,
            final ErrorHandlingConfiguration errorConfig,
            final LocaleConfiguration localeConfig) {
        this.themeConfiguration = themeConfig;
        this.errorHandlingConfiguration = errorConfig;
        this.localeConfiguration = localeConfig;
    }

    /**
     * Initializes Vaadin service with UI configuration.
     *
     * @param event the service initialization event (non-null)
     * @throws IllegalArgumentException if event is null
     */
    @Override
    public void serviceInit(final ServiceInitEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("ServiceInitEvent cannot be null");
        }
        LOGGER.debug("Initializing Vaadin service...");
        event.getSource().addUIInitListener(uiEvent -> configureUi(uiEvent.getUI()));
    }

    /**
     * Configures UI with theme, error handling and locale settings.
     *
     * @param ui the UI instance to configure (non-null)
     * @throws IllegalArgumentException if ui is null
     */
    private void configureUi(final UI ui) {
        if (ui == null) {
            throw new IllegalArgumentException("UI cannot be null");
        }

        LOGGER.debug("UI initialized, applying configuration [uiId={}]", ui.getUIId());

        themeConfiguration.applyTheme(ui);
        errorHandlingConfiguration.installErrorHandler(ui);
        localeConfiguration.applyPreferredLocale(ui);

        LOGGER.debug("UI setup completed [uiId={}]", ui.getUIId());
    }
}
