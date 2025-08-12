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
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.FlashcardService;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.service.StatsService;

import java.time.LocalDate;
import java.util.List;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private final FlashcardService flashcardService;
    private final StatsService statsService;
    private final PracticeSettingsService practiceSettingsService;
    private H1 viewTitle;

    public MainLayout(FlashcardService flashcardService, StatsService statsService, PracticeSettingsService practiceSettingsService) {
        this.flashcardService = flashcardService;
        this.statsService = statsService;
        this.practiceSettingsService = practiceSettingsService;
        setPrimarySection(Section.NAVBAR);
        addHeaderContent();
    }

    private void addHeaderContent() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidth("100%");
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        HorizontalLayout menu = new HorizontalLayout();
        menu.setSpacing(true);
        Button decksBtn = new Button("Колоды", e -> getUI().ifPresent(ui -> ui.navigate("")));
        Button statsBtn = new Button("Статистика", e -> openStatsDialog());
        Button settingsBtn = new Button("Настройки", e -> openSettingsDialog());
        menu.add(decksBtn, statsBtn, settingsBtn);

        bar.add(viewTitle, menu);
        addToNavbar(true, bar);
    }

    private void openStatsDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("760px");
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.add(new H3("Статистика практик"));

        List<Deck> decks = flashcardService.getDecksByUserId(flashcardService.getCurrentUser().getId());
        LocalDate today = LocalDate.now();

        // Глобальные итоги по всем колодам
        int totalSessionsAll = 0;
        int totalSessionsToday = 0;
        int totalViewedAll = 0;
        int totalViewedToday = 0;
        int totalCorrectAll = 0;
        int totalCorrectToday = 0;
        int totalHardAll = 0;
        int totalHardToday = 0;

        for (Deck deck : decks) {
            var daily = statsService.getDailyStatsForDeck(deck.getId());
            for (var ds : daily) {
                totalSessionsAll += ds.sessions;
                totalViewedAll += ds.viewed;
                totalCorrectAll += ds.correct;
                totalHardAll += ds.hard;
                if (today.equals(ds.date)) {
                    totalSessionsToday += ds.sessions;
                    totalViewedToday += ds.viewed;
                    totalCorrectToday += ds.correct;
                    totalHardToday += ds.hard;
                }
            }
        }

        layout.add(new Span(String.format(
                "Всего: сессий %d, просмотрено %d, правильных %d, сложно %d",
                totalSessionsAll, totalViewedAll, totalCorrectAll, totalHardAll
        )));
        layout.add(new Span(String.format(
                "За сегодня: сессий %d, просмотрено %d, правильных %d, сложно %d",
                totalSessionsToday, totalViewedToday, totalCorrectToday, totalHardToday
        )));

        // Разбивка по колодам
        layout.add(new H3("По колодам"));
        for (Deck deck : decks) {
            int sessionsAll = 0, sessionsToday = 0;
            int viewedAll = 0, viewedToday = 0;
            int correctAll = 0, correctToday = 0;
            int hardAll = 0, hardToday = 0;
            var daily = statsService.getDailyStatsForDeck(deck.getId());
            for (var ds : daily) {
                sessionsAll += ds.sessions;
                viewedAll += ds.viewed;
                correctAll += ds.correct;
                hardAll += ds.hard;
                if (today.equals(ds.date)) {
                    sessionsToday += ds.sessions;
                    viewedToday += ds.viewed;
                    correctToday += ds.correct;
                    hardToday += ds.hard;
                }
            }
            Span line = new Span(String.format(
                    "%s — сессий: %d (сегодня %d), просмотрено: %d (сегодня %d), правильных: %d (сегодня %d), сложно: %d (сегодня %d)",
                    deck.getTitle(), sessionsAll, sessionsToday, viewedAll, viewedToday, correctAll, correctToday, hardAll, hardToday
            ));
            layout.add(line);
        }

        Button close = new Button("Закрыть", e -> dialog.close());
        layout.add(close);
        dialog.add(layout);
        dialog.open();
    }

    private void openSettingsDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("520px");
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.add(new H3("Настройки практики (по умолчанию)"));

        Select<Integer> countSelect = new Select<>();
        countSelect.setLabel("Количество карточек");
        countSelect.setItems(5, 10, 15, 20, 25, 30);
        countSelect.setValue(practiceSettingsService.getDefaultCount());

        RadioButtonGroup<String> modeGroup = new RadioButtonGroup<>();
        modeGroup.setLabel("Режим показа");
        modeGroup.setItems("Случайный порядок", "По порядку");
        modeGroup.setValue(practiceSettingsService.isDefaultRandomOrder() ? "Случайный порядок" : "По порядку");

        RadioButtonGroup<String> dirGroup = new RadioButtonGroup<>();
        dirGroup.setLabel("Направление");
        dirGroup.setItems("Лицевая → Обратная", "Обратная → Лицевая");
        dirGroup.setValue("front_to_back".equals(practiceSettingsService.getDefaultDirection()) ? "Лицевая → Обратная" : "Обратная → Лицевая");

        HorizontalLayout buttons = new HorizontalLayout();
        Button save = new Button("Сохранить", e -> {
            practiceSettingsService.setDefaultCount(countSelect.getValue());
            practiceSettingsService.setDefaultRandomOrder("Случайный порядок".equals(modeGroup.getValue()));
            practiceSettingsService.setDefaultDirection("Лицевая → Обратная".equals(dirGroup.getValue()) ? "front_to_back" : "back_to_front");
            dialog.close();
        });
        Button cancel = new Button("Отмена", e -> dialog.close());
        buttons.add(save, cancel);

        layout.add(countSelect, modeGroup, dirGroup, buttons);
        dialog.add(layout);
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
