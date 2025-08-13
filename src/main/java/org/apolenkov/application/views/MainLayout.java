package org.apolenkov.application.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.apolenkov.application.application.usecase.DeckUseCase;
import org.apolenkov.application.application.usecase.UserUseCase;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.views.components.PracticeSettingsDialog;
import org.apolenkov.application.views.components.LanguageSwitcher;
import org.apolenkov.application.views.components.StatsDialogComponent;

import java.time.LocalDate;
import java.util.List;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private final DeckUseCase deckUseCase;
    private final UserUseCase userUseCase;
    private final StatsService statsService;
    private final PracticeSettingsService practiceSettingsService;
    private H1 viewTitle;

    public MainLayout(DeckUseCase deckUseCase, UserUseCase userUseCase, StatsService statsService, PracticeSettingsService practiceSettingsService) {
        this.deckUseCase = deckUseCase;
        this.userUseCase = userUseCase;
        this.statsService = statsService;
        this.practiceSettingsService = practiceSettingsService;
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
        Button decksBtn = new Button(getTranslation("main.decks"), e -> getUI().ifPresent(ui -> ui.navigate("")));
        Button statsBtn = new Button(getTranslation("main.stats"), e -> openStatsDialog());
        Button settingsBtn = new Button(getTranslation("main.settings"), e -> openSettingsDialog());
        LanguageSwitcher lang = new LanguageSwitcher();
        menu.add(decksBtn, statsBtn, settingsBtn, lang);

        bar.add(viewTitle, menu);
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
        if (getContent() != null) {
            getContent().getElement().getStyle()
                .set("max-width", "1040px")
                .set("margin", "0 auto")
                .set("padding-left", "var(--lumo-space-m)")
                .set("padding-right", "var(--lumo-space-m)");
        }
    }
}
