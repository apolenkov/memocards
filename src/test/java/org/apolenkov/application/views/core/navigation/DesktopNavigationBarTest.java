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
 * Smoke tests for DesktopNavigationBar component.
 * Verifies basic initialization and menu refresh functionality.
 */
class DesktopNavigationBarTest {

    private TopMenuAuthService authService;
    private TopMenuLogoutDialog logoutDialog;
    private PracticeSettingsService practiceSettingsService;
    private PracticeSettingsComponents settingsComponents;
    private DesktopNavigationBar navbar;

    @BeforeEach
    void setUp() {
        authService = mock(TopMenuAuthService.class);
        logoutDialog = mock(TopMenuLogoutDialog.class);
        practiceSettingsService = mock(PracticeSettingsService.class);
        settingsComponents = mock(PracticeSettingsComponents.class);

        navbar = new DesktopNavigationBar(authService, logoutDialog, practiceSettingsService, settingsComponents);
    }

    @Test
    void shouldCreateNavbarInstance() {
        assertThat(navbar).isNotNull();
    }

    @Test
    void shouldInitializeOnAttach() {
        // Given
        Authentication auth = mock(Authentication.class);
        when(authService.getCurrentAuthentication()).thenReturn(auth);
        when(authService.isAuthenticated(auth)).thenReturn(false);

        // When
        AttachEvent event = mock(AttachEvent.class);
        navbar.onAttach(event);

        // Then
        assertThat(navbar.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void shouldRefreshMenuForAnonymousUser() {
        // Given
        Authentication auth = mock(Authentication.class);
        when(authService.getCurrentAuthentication()).thenReturn(auth);
        when(authService.isAuthenticated(auth)).thenReturn(false);

        // When
        navbar.refreshMenu();

        // Then
        assertThat(navbar.getChildren().count()).isGreaterThan(0);
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
        navbar.refreshMenu();

        // Then
        assertThat(navbar.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void shouldOnlyRefreshWhenAuthenticationStateChanges() {
        // Given
        Authentication auth = mock(Authentication.class);
        when(authService.getCurrentAuthentication()).thenReturn(auth);
        when(authService.isAuthenticated(auth)).thenReturn(false);

        // Initialize navbar
        navbar.refreshMenu();
        long initialComponentCount = navbar.getChildren().count();

        // When - same auth state
        navbar.refreshMenuIfNeeded();

        // Then - should not refresh (component count unchanged)
        assertThat(navbar.getChildren().count()).isEqualTo(initialComponentCount);
    }
}
