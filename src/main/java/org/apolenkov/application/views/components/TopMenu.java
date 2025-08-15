package org.apolenkov.application.views.components;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.ArrayList;
import java.util.List;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.UserUseCase;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

@Component
@UIScope
public class TopMenu extends HorizontalLayout {

    private final List<MenuButton> menuButtons = new ArrayList<>();
    private final Anchor title;

    private final UserUseCase userUseCase;

    public TopMenu(
            UserUseCase userUseCase, StatsService statsService, PracticeSettingsService practiceSettingsService) {
        this.userUseCase = userUseCase;
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        addClassName("top-menu");

        title = new Anchor("/", getTranslation("app.title"));
        title.addClassName("top-menu__title");

        initializeMenuButtons();
        refreshMenu();
    }

    private void initializeMenuButtons() {
        menuButtons.add(new MenuButton(getTranslation("main.decks"), "/decks", "nav-decks", false, "ROLE_USER"));
        menuButtons.add(new MenuButton(getTranslation("main.stats"), "/stats", "nav-stats", false, "ROLE_USER"));
        menuButtons.add(
                new MenuButton(getTranslation("main.settings"), "/settings", "nav-settings", false, "ROLE_USER"));
        menuButtons.add(new MenuButton(
                getTranslation("admin.users.page.title"), "/admin/users", "nav-admin-users", false, "ROLE_ADMIN"));
        menuButtons.add(new MenuButton(
                getTranslation("admin.content.page.title"),
                "/admin/content",
                "nav-admin-content",
                false,
                "ROLE_ADMIN"));
        menuButtons.add(new MenuButton(
                getTranslation("admin.audit.page.title"), "/admin/audit", "nav-admin-audit", false, "ROLE_ADMIN"));
        menuButtons.add(new MenuButton(getTranslation("main.logout"), "/logout", "nav-logout", false));
    }

    private HorizontalLayout createMenuButtonsLayout() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(Alignment.CENTER);

        Authentication auth = SecurityContextHolder.getContext() != null
                ? SecurityContextHolder.getContext().getAuthentication()
                : null;
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

        Authentication auth = SecurityContextHolder.getContext() != null
                ? SecurityContextHolder.getContext().getAuthentication()
                : null;
        boolean isAuthenticated = auth != null && !(auth instanceof AnonymousAuthenticationToken);
        if (isAuthenticated) {
            String displayName;
            try {
                displayName = userUseCase.getCurrentUser().getName();
                if (displayName == null || displayName.isBlank()) {
                    displayName = auth != null ? auth.getName() : "";
                }
            } catch (Exception e) {
                displayName = auth != null ? auth.getName() : "";
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

        if (menuButton.getRoute().equals("/logout")) {
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

        if (menuButton.getRoute().equals("/logout")) {
            button = new Button(menuButton.getText());
            button.getElement().setAttribute("data-testid", menuButton.getTestId());
            button.addClickListener(e -> {
                openLogoutDialog();
            });
        } else {
            button = new Button(menuButton.getText());
            button.getElement().setAttribute("data-testid", menuButton.getTestId());
            button.addClickListener(e -> {
                String route = menuButton.getRoute();
                getUI().ifPresent(ui -> ui.navigate(route));
            });
        }

        return button;
    }

    private void openLogoutDialog() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(Alignment.STRETCH);
        content.getStyle().set("minWidth", "360px");

        Div question = new Div();
        question.setText(getTranslation("auth.logout.confirm"));
        question.getElement().getClassList().add("logout-dialog__title");
        content.add(question);

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setAlignItems(Alignment.CENTER);
        actions.setJustifyContentMode(JustifyContentMode.END);

        Button cancel = new Button(getTranslation("common.cancel"));
        cancel.addClickListener(e -> dialog.close());
        Button submit = new Button(getTranslation("auth.logout.submit"));
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        submit.addClickShortcut(Key.ENTER);
        submit.addClickListener(e -> {
            try {
                var req = VaadinServletRequest.getCurrent().getHttpServletRequest();
                new SecurityContextLogoutHandler().logout(req, null, null);
                getUI().ifPresent(ui -> ui.getPage().setLocation("/"));
                dialog.close();
            } catch (Exception ignored) {
                getUI().ifPresent(ui -> ui.getPage().setLocation("/error"));
                dialog.close();
            }
        });

        actions.add(cancel, submit);
        content.add(actions);

        dialog.add(content);
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
