package org.apolenkov.application.views.core.navigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.vaadin.flow.component.AttachEvent;
import org.apolenkov.application.domain.usecase.UserUseCase;
import org.apolenkov.application.service.settings.UserSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Smoke tests for LanguageSwitcher (desktop) component.
 * Verifies basic initialization functionality.
 */
class LanguageSwitcherTest {

    private UserUseCase userUseCase;
    private UserSettingsService userSettingsService;
    private LanguageSwitcher switcher;

    @BeforeEach
    void setUp() {
        userUseCase = mock(UserUseCase.class);
        userSettingsService = mock(UserSettingsService.class);
        switcher = new LanguageSwitcher(userUseCase, userSettingsService);
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
