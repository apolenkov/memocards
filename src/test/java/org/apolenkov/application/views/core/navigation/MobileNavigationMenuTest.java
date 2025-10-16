package org.apolenkov.application.views.core.navigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.vaadin.flow.component.AttachEvent;
import org.apolenkov.application.service.settings.PracticeSettingsService;
import org.apolenkov.application.views.practice.components.PracticeSettingsComponents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

/**
 * Smoke tests for MobileNavigationMenu component.
 * Verifies basic initialization and menu refresh functionality.
 */
class MobileNavigationMenuTest {

    private TopMenuAuthService authService;
    private TopMenuLogoutDialog logoutDialog;
    private PracticeSettingsService practiceSettingsService;
    private PracticeSettingsComponents settingsComponents;
    private MobileNavigationMenu menu;

    @BeforeEach
    void setUp() {
        authService = mock(TopMenuAuthService.class);
        logoutDialog = mock(TopMenuLogoutDialog.class);
        practiceSettingsService = mock(PracticeSettingsService.class);
        settingsComponents = mock(PracticeSettingsComponents.class);

        menu = new MobileNavigationMenu(authService, logoutDialog, practiceSettingsService, settingsComponents);
    }

    @Test
    void shouldCreateMenuInstance() {
        assertThat(menu).isNotNull();
    }

    @Test
    void shouldInitializeOnAttach() {
        // Given
        Authentication auth = mock(Authentication.class);
        when(authService.getCurrentAuthentication()).thenReturn(auth);
        when(authService.isAuthenticated(auth)).thenReturn(false);

        // When
        AttachEvent event = mock(AttachEvent.class);
        menu.onAttach(event);

        // Then
        assertThat(menu.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void shouldRefreshMenuForAnonymousUser() {
        // Given
        Authentication auth = mock(Authentication.class);
        when(authService.getCurrentAuthentication()).thenReturn(auth);
        when(authService.isAuthenticated(auth)).thenReturn(false);

        // When
        menu.refreshMenu();

        // Then
        assertThat(menu.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void shouldRefreshMenuForAuthenticatedUser() {
        // Given
        Authentication auth = mock(Authentication.class);
        when(authService.getCurrentAuthentication()).thenReturn(auth);
        when(authService.isAuthenticated(auth)).thenReturn(true);
        when(authService.getUserDisplayName(auth)).thenReturn("Test User");
        when(authService.hasUserRole(auth)).thenReturn(true);

        // When
        menu.refreshMenu();

        // Then
        assertThat(menu.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void shouldOnlyRefreshWhenAuthenticationStateChanges() {
        // Given
        Authentication auth = mock(Authentication.class);
        when(authService.getCurrentAuthentication()).thenReturn(auth);
        when(authService.isAuthenticated(auth)).thenReturn(false);

        // Initialize menu
        menu.refreshMenu();
        long initialComponentCount = menu.getChildren().count();

        // When - same auth state
        menu.refreshMenuIfNeeded();

        // Then - should not refresh (component count unchanged)
        assertThat(menu.getChildren().count()).isEqualTo(initialComponentCount);
    }
}
