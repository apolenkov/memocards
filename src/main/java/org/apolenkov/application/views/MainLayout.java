package org.apolenkov.application.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.components.PracticeSettingsDialog;
import org.apolenkov.application.views.components.StatsDialogComponent;

/** The main view is a top-level placeholder for other views. */
@Layout
public class MainLayout extends AppLayout {

    private final DeckUseCase deckUseCase;
    private final UserUseCase userUseCase;
    private final StatsService statsService;
    private final PracticeSettingsService practiceSettingsService;
    private H1 viewTitle;
    private final org.apolenkov.application.views.components.LanguageSwitcher languageSwitcher;
    private Span greetingSpan;

    public MainLayout(
            DeckUseCase deckUseCase,
            UserUseCase userUseCase,
            StatsService statsService,
            PracticeSettingsService practiceSettingsService,
            org.apolenkov.application.views.components.LanguageSwitcher languageSwitcher) {
        this.deckUseCase = deckUseCase;
        this.userUseCase = userUseCase;
        this.statsService = statsService;
        this.practiceSettingsService = practiceSettingsService;
        this.languageSwitcher = languageSwitcher;
        setPrimarySection(Section.NAVBAR);
        addHeaderContent();
    }

    private void addHeaderContent() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.addClassName("main-layout__navbar");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        HorizontalLayout menu = new HorizontalLayout();
        menu.addClassName("main-layout__menu");
        Button decksBtn = new Button(getTranslation("main.decks"), e -> getUI().ifPresent(ui -> ui.navigate("home")));
        decksBtn.getElement().setAttribute("data-testid", "nav-decks");
        Button statsBtn = new Button(getTranslation("main.stats"), e -> openStatsDialog());
        statsBtn.getElement().setAttribute("data-testid", "nav-stats");
        Button settingsBtn = new Button(getTranslation("main.settings"), e -> openSettingsDialog());
        settingsBtn.getElement().setAttribute("data-testid", "nav-settings");
        Button adminBtn =
                new Button(getTranslation("main.admin"), e -> getUI().ifPresent(ui -> ui.navigate("admin/users")));
        adminBtn.getElement().setAttribute("data-testid", "nav-admin");
        Button auditBtn = new Button(
                getTranslation("main.adminAudit"), e -> getUI().ifPresent(ui -> ui.navigate("admin/role-audit")));
        auditBtn.getElement().setAttribute("data-testid", "nav-audit");
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication();
            boolean hasUser =
                    auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_USER".equals(a.getAuthority()));
            boolean hasAdmin =
                    auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

            decksBtn.setVisible(hasUser);
            statsBtn.setVisible(hasUser);
            settingsBtn.setVisible(hasUser);
            adminBtn.setVisible(hasAdmin);
            auditBtn.setVisible(hasAdmin);
        } catch (Exception ignored) {
            decksBtn.setVisible(false);
            statsBtn.setVisible(false);
            settingsBtn.setVisible(false);
            adminBtn.setVisible(false);
            auditBtn.setVisible(false);
        }
        Button logoutBtn = new Button(getTranslation("main.logout"), e -> getUI().ifPresent(
                        ui -> ui.getPage().executeJs("location.assign('/logout-confirm')")));
        logoutBtn.getElement().setAttribute("data-testid", "nav-logout");
        menu.add(decksBtn, statsBtn, settingsBtn, adminBtn, auditBtn, logoutBtn);

        HorizontalLayout right = new HorizontalLayout();
        right.addClassName("main-layout__right");
        greetingSpan = new Span("");
        greetingSpan.addClassName("main-layout__greeting");
        updateGreeting();
        right.add(greetingSpan, languageSwitcher);

        bar.add(viewTitle, menu, right);
        addToNavbar(true, bar);
    }

    private void openStatsDialog() {
        StatsDialogComponent dialog = new StatsDialogComponent(deckUseCase, userUseCase, statsService);
        dialog.open();
    }

    private void openSettingsDialog() {
        PracticeSettingsDialog dialog = new PracticeSettingsDialog(practiceSettingsService);
        dialog.open();
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText("");
        updateGreeting();
        if (getContent() != null) {
            getContent()
                    .getElement()
                    .getStyle()
                    .set("max-width", "1040px")
                    .set("margin", "0 auto")
                    .set("padding-left", "var(--lumo-space-m)")
                    .set("padding-right", "var(--lumo-space-m)");
        }
    }

    private void updateGreeting() {
        try {
            String name = userUseCase.getCurrentUser().getName();
            greetingSpan.setText(getTranslation("main.greeting", name));
            greetingSpan.setVisible(true);
        } catch (Exception ex) {
            greetingSpan.setVisible(false);
        }
    }
}
