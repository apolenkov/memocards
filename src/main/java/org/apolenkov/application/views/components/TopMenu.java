package org.apolenkov.application.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.ArrayList;
import java.util.List;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@UIScope
public class TopMenu extends HorizontalLayout {

    private final List<MenuButton> menuButtons = new ArrayList<>();
    private final Anchor title;

    private final DeckUseCase deckUseCase;
    private final UserUseCase userUseCase;
    private final StatsService statsService;
    private final PracticeSettingsService practiceSettingsService;

    public TopMenu(
            DeckUseCase deckUseCase,
            UserUseCase userUseCase,
            StatsService statsService,
            PracticeSettingsService practiceSettingsService) {
        this.deckUseCase = deckUseCase;
        this.userUseCase = userUseCase;
        this.statsService = statsService;
        this.practiceSettingsService = practiceSettingsService;
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        addClassName("top-menu");

        // Заголовок-прямая ссылка на главную
        title = new Anchor("/", getTranslation("main.home"));
        title.addClassName("top-menu__title");

        // Инициализируем кнопки меню
        initializeMenuButtons();

        // Добавляем заголовок и кнопки
        add(title);
        add(createMenuButtonsLayout());
    }

    private void initializeMenuButtons() {
        // Кнопка "Главная" - всегда доступна
        menuButtons.add(new MenuButton(getTranslation("main.home"), "", "nav-home", true));

        // Кнопки для роли USER
        menuButtons.add(new MenuButton(getTranslation("main.decks"), "/decks", "nav-decks", false, "ROLE_USER"));
        // Оставляем пути для тест-ид, но клики перехватим и откроем модальные окна
        menuButtons.add(new MenuButton(getTranslation("main.stats"), "#stats", "nav-stats", false, "ROLE_USER"));
        menuButtons.add(
                new MenuButton(getTranslation("main.settings"), "#settings", "nav-settings", false, "ROLE_USER"));

        // Кнопки для роли ADMIN
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

        // Кнопка "Выход" - только для авторизованных
        menuButtons.add(new MenuButton(getTranslation("main.logout"), "/logout", "nav-logout", false));
    }

    private HorizontalLayout createMenuButtonsLayout() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(Alignment.CENTER);

        // Получаем информацию об аутентификации
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

    private boolean shouldShowButton(MenuButton menuButton, Authentication auth, boolean isAuthenticated) {
        // Кнопка "Главная" всегда показывается
        if (menuButton.isAlwaysVisible()) {
            return true;
        }

        // Кнопка "Выход" показывается только авторизованным
        if (menuButton.getRoute().equals("/logout")) {
            return isAuthenticated;
        }

        // Остальные кнопки проверяются по ролям
        if (menuButton.getRequiredRoles() != null
                && !menuButton.getRequiredRoles().isEmpty()) {
            if (!isAuthenticated) {
                return false;
            }

            for (String requiredRole : menuButton.getRequiredRoles()) {
                if (auth.getAuthorities().stream().anyMatch(a -> requiredRole.equals(a.getAuthority()))) {
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
            // Кнопка выхода
            button = new Button(menuButton.getText());
            button.getElement().setAttribute("data-testid", menuButton.getTestId());
            button.addClickListener(e -> {
                // Перенаправляем на logout
                getUI().ifPresent(ui -> ui.getPage().executeJs("location.assign('/logout-confirm')"));
            });
        } else {
            // Обычная навигационная кнопка
            button = new Button(menuButton.getText());
            button.getElement().setAttribute("data-testid", menuButton.getTestId());
            button.addClickListener(e -> {
                String route = menuButton.getRoute();
                if ("#stats".equals(route)) {
                    if (deckUseCase != null && userUseCase != null && statsService != null) {
                        new StatsDialogComponent(deckUseCase, userUseCase, statsService).open();
                    }
                    return;
                }
                if ("#settings".equals(route)) {
                    if (practiceSettingsService != null) {
                        new PracticeSettingsDialog(practiceSettingsService).open();
                    }
                    return;
                }
                getUI().ifPresent(ui -> ui.navigate(route));
            });
        }

        return button;
    }

    // Внутренний класс для хранения информации о кнопке меню
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
