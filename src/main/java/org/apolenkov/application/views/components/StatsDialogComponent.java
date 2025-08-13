package org.apolenkov.application.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apolenkov.application.application.usecase.DeckUseCase;
import org.apolenkov.application.application.usecase.UserUseCase;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.StatsService;

import java.time.LocalDate;
import java.util.List;

/**
 * Stats dialog extracted from MainLayout.
 */
public class StatsDialogComponent extends Dialog {

    public StatsDialogComponent(DeckUseCase deckUseCase, UserUseCase userUseCase, StatsService statsService) {
        setWidth("760px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.add(new H3(getTranslation("stats.title")));

        List<Deck> decks = deckUseCase.getDecksByUserId(userUseCase.getCurrentUser().getId());
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

        layout.add(new Span(getTranslation("stats.total", null, totalSessionsAll, totalViewedAll, totalCorrectAll, totalHardAll)));
        layout.add(new Span(getTranslation("stats.today", null, totalSessionsToday, totalViewedToday, totalCorrectToday, totalHardToday)));

        layout.add(new H3(getTranslation("stats.byDeck")));
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
            Span line = new Span(getTranslation(
                    "stats.deckLine",
                    null,
                    deck.getTitle(),
                    sessionsAll, sessionsToday,
                    viewedAll, viewedToday,
                    correctAll, correctToday,
                    hardAll, hardToday
            ));
            layout.add(line);
        }

        Button close = new Button(getTranslation("stats.close"), e -> close());
        layout.add(close);
        add(layout);
    }
}


