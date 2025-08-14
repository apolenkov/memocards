package org.apolenkov.application.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.time.LocalDate;
import java.util.List;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;

/** Stats dialog extracted from MainLayout. */
public class StatsDialogComponent extends Dialog {

    public StatsDialogComponent(DeckUseCase deckUseCase, UserUseCase userUseCase, StatsService statsService) {
        setWidth("760px");

        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("stats-dialog");
        layout.setPadding(true);
        layout.setSpacing(true);
        H3 title = new H3(getTranslation("stats.title"));
        title.addClassName("stats-dialog__title");
        layout.add(title);

        List<Deck> decks =
                deckUseCase.getDecksByUserId(userUseCase.getCurrentUser().getId());
        LocalDate today = LocalDate.now();

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
                totalSessionsAll += ds.sessions();
                totalViewedAll += ds.viewed();
                totalCorrectAll += ds.correct();
                totalHardAll += ds.hard();
                if (today.equals(ds.date())) {
                    totalSessionsToday += ds.sessions();
                    totalViewedToday += ds.viewed();
                    totalCorrectToday += ds.correct();
                    totalHardToday += ds.hard();
                }
            }
        }

        Span total = new Span(
                getTranslation("stats.total", totalSessionsAll, totalViewedAll, totalCorrectAll, totalHardAll));
        total.addClassName("stats-dialog__summary");
        Span todayLine = new Span(
                getTranslation("stats.today", totalSessionsToday, totalViewedToday, totalCorrectToday, totalHardToday));
        todayLine.addClassName("stats-dialog__summary--today");
        layout.add(total, todayLine);

        H3 byDeck = new H3(getTranslation("stats.byDeck"));
        byDeck.addClassName("stats-dialog__subtitle");
        layout.add(byDeck);
        for (Deck deck : decks) {
            int sessionsAll = 0, sessionsToday = 0;
            int viewedAll = 0, viewedToday = 0;
            int correctAll = 0, correctToday = 0;
            int hardAll = 0, hardToday = 0;
            var daily = statsService.getDailyStatsForDeck(deck.getId());
            for (var ds : daily) {
                sessionsAll += ds.sessions();
                viewedAll += ds.viewed();
                correctAll += ds.correct();
                hardAll += ds.hard();
                if (today.equals(ds.date())) {
                    sessionsToday += ds.sessions();
                    viewedToday += ds.viewed();
                    correctToday += ds.correct();
                    hardToday += ds.hard();
                }
            }
            Span line = new Span(getTranslation(
                    "stats.deckLine",
                    deck.getTitle(),
                    sessionsAll,
                    sessionsToday,
                    viewedAll,
                    viewedToday,
                    correctAll,
                    correctToday,
                    hardAll,
                    hardToday));
            layout.add(line);
        }

        Button close = new Button(getTranslation("stats.close"), e -> close());
        layout.add(close);
        add(layout);
    }
}
