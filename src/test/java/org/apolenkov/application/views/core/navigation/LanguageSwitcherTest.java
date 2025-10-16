package org.apolenkov.application.views.core.navigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.vaadin.flow.component.AttachEvent;
import org.apolenkov.application.domain.usecase.UserUseCase;
import org.apolenkov.application.service.settings.UserSettingsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Smoke tests for LanguageSwitcher (desktop) component.
 * Verifies basic initialization functionality.
 */
class LanguageSwitcherTest {

    private LanguageSwitcher switcher;
    private VaadinTestContext vaadinContext;

    @BeforeEach
    void setUp() {
        vaadinContext = new VaadinTestContext();

        UserUseCase userUseCase = mock(UserUseCase.class);
        UserSettingsService userSettingsService = mock(UserSettingsService.class);
        switcher = new LanguageSwitcher(userUseCase, userSettingsService);
    }

    @AfterEach
    void tearDown() {
        if (vaadinContext != null) {
            try {
                vaadinContext.close();
            } catch (Exception e) {
                // Ignore cleanup errors in tests
            }
        }
    }

    @Test
    void shouldCreateSwitcherInstance() {
        assertThat(switcher).isNotNull();
    }

    @Test
    void shouldInitializeOnAttach() {
        // When
        AttachEvent event = mock(AttachEvent.class);
        switcher.onAttach(event);

        // Then
        assertThat(switcher.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void shouldNotReinitializeOnMultipleAttach() {
        // When
        AttachEvent event = mock(AttachEvent.class);
        switcher.onAttach(event);
        long initialCount = switcher.getChildren().count();

        // Second attach
        switcher.onAttach(event);

        // Then - component count should remain same (no double init)
        assertThat(switcher.getChildren().count()).isEqualTo(initialCount);
    }
}
