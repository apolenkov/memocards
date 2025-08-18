package org.apolenkov.application.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.ArrayList;
import java.util.List;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.DialogHelper;
import org.apolenkov.application.views.utils.NavigationHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

@Component
@UIScope
public class TopMenu extends HorizontalLayout {

    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String LOGOUT_ROUTE = "/logout";
    private static final String DATA_TESTID_ATTRIBUTE = "data-testid";

    private final List<MenuButton> menuButtons = new ArrayList<>();
    private final Anchor title;

    private final transient UserUseCase userUseCase;
    private final transient PracticeSettingsService practiceSettingsService;

    public TopMenu(UserUseCase userUseCase, PracticeSettingsService practiceSettingsService) {
        this.userUseCase = userUseCase;
        this.practiceSettingsService = practiceSettingsService;
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        addClassName("top-menu");

        title = new Anchor("/", "");
        title.addClassName("top-menu__title");

        Image navIcon = new Image(
                new StreamResource("flashcards-logo.svg", () -> getClass()
                        .getResourceAsStream("/META-INF/resources/icons/flashcards-logo.svg")),
                getTranslation("app.title"));
        navIcon.addClassName("top-menu__logo");
        title.add(navIcon);

        initializeMenuButtons();
        refreshMenu();
    }

    private void initializeMenuButtons() {
        menuButtons.add(new MenuButton(getTranslation("main.decks"), "/decks", "nav-decks", false, ROLE_USER));
        menuButtons.add(new MenuButton(getTranslation("main.stats"), "/stats", "nav-stats", false, ROLE_USER));
        menuButtons.add(new MenuButton(getTranslation("main.settings"), "/settings", "nav-settings", false, ROLE_USER));
        menuButtons.add(new MenuButton(
                getTranslation("admin.users.page.title"), "/admin/users", "nav-admin-users", false, ROLE_ADMIN));
        menuButtons.add(new MenuButton(
                getTranslation("admin.content.page.title"), "/admin/content", "nav-admin-content", false, ROLE_ADMIN));
        menuButtons.add(new MenuButton(
                getTranslation("admin.audit.page.title"), "/admin/audit", "nav-admin-audit", false, ROLE_ADMIN));
        menuButtons.add(new MenuButton(getTranslation("main.logout"), LOGOUT_ROUTE, "nav-logout", false));
    }

    private HorizontalLayout createMenuButtonsLayout() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(Alignment.CENTER);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && !(auth instanceof AnonymousAuthenticationToken);

        for (MenuButton menuButton : menuButtons) {
            if (shouldShowButton(menuButton, auth, isAuthenticated)) {
                Button button = createButton(menuButton);
                buttonsLayout.add(button);
            }
        }

        return buttonsLayout;
    }

    public void refreshMenu() {
        removeAll();

        HorizontalLayout left = new HorizontalLayout();
        left.setAlignItems(Alignment.CENTER);
        left.setSpacing(true);
        left.add(title);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && !(auth instanceof AnonymousAuthenticationToken);
        if (isAuthenticated) {
            String displayName;
            String authName = auth.getName();
            try {
                displayName = userUseCase.getCurrentUser().getName();
                if (displayName == null || displayName.isBlank()) {
                    displayName = authName;
                }
            } catch (Exception e) {
                displayName = authName;
            }
            Div greeting = new Div();
            greeting.setText(getTranslation("main.greeting", displayName));
            greeting.getElement().getClassList().add("top-menu__greeting");
            left.add(greeting);
        }

        add(left);
        add(createMenuButtonsLayout());
    }

    private boolean shouldShowButton(MenuButton menuButton, Authentication auth, boolean isAuthenticated) {
        if (menuButton.isAlwaysVisible()) {
            return true;
        }

        if (menuButton.getRoute().equals(LOGOUT_ROUTE)) {
            return isAuthenticated;
        }

        if (menuButton.getRequiredRoles() != null
                && !menuButton.getRequiredRoles().isEmpty()) {
            if (!isAuthenticated) {
                return false;
            }

            for (String requiredRole : menuButton.getRequiredRoles()) {
                if (auth != null
                        && auth.getAuthorities().stream().anyMatch(a -> requiredRole.equals(a.getAuthority()))) {
                    return true;
                }
            }
            return false;
        }

        return false;
    }

    private Button createButton(MenuButton menuButton) {
        Button button;

        if (menuButton.getRoute().equals(LOGOUT_ROUTE)) {
            button = ButtonHelper.createTertiaryButton(menuButton.getText(), e -> openLogoutDialog());
            button.getElement().setAttribute(DATA_TESTID_ATTRIBUTE, menuButton.getTestId());
        } else if (menuButton.getRoute().equals("/settings")) {
            button = ButtonHelper.createTertiaryButton(menuButton.getText(), e -> {
                PracticeSettingsDialog dialog = new PracticeSettingsDialog(practiceSettingsService);
                dialog.open();
            });
            button.getElement().setAttribute(DATA_TESTID_ATTRIBUTE, menuButton.getTestId());
        } else {
            button = ButtonHelper.createTertiaryButton(
                    menuButton.getText(), e -> NavigationHelper.navigateTo(menuButton.getRoute()));
            button.getElement().setAttribute(DATA_TESTID_ATTRIBUTE, menuButton.getTestId());
        }

        return button;
    }

    private void openLogoutDialog() {
        Dialog dialog = DialogHelper.createConfirmationDialog(
                getTranslation("auth.logout.confirm"),
                getTranslation("auth.logout.confirm"),
                () -> {
                    try {
                        var req = VaadinServletRequest.getCurrent().getHttpServletRequest();
                        new SecurityContextLogoutHandler().logout(req, null, null);
                        getUI().ifPresent(ui ->
                                ui.getPage().setLocation("/")); // Keep setLocation for logout (server redirect)
                    } catch (Exception ignored) {
                        getUI().ifPresent(ui ->
                                ui.navigate("error", com.vaadin.flow.router.QueryParameters.of("from", "home")));
                    }
                },
                null);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.open();
    }

    private static class MenuButton {
        private final String text;
        private final String route;
        private final String testId;
        private final boolean alwaysVisible;
        private final List<String> requiredRoles;

        public MenuButton(String text, String route, String testId, boolean alwaysVisible) {
            this(text, route, testId, alwaysVisible, new String[0]);
        }

        public MenuButton(String text, String route, String testId, boolean alwaysVisible, String... requiredRoles) {
            this.text = text;
            this.route = route;
            this.testId = testId;
            this.alwaysVisible = alwaysVisible;
            this.requiredRoles = requiredRoles != null ? List.of(requiredRoles) : new ArrayList<>();
        }

        public String getText() {
            return text;
        }

        public String getRoute() {
            return route;
        }

        public String getTestId() {
            return testId;
        }

        public boolean isAlwaysVisible() {
            return alwaysVisible;
        }

        public List<String> getRequiredRoles() {
            return requiredRoles;
        }
    }
}
