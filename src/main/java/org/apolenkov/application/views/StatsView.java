package org.apolenkov.application.views;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apolenkov.application.config.SecurityConstants;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;

@Route(value = "stats", layout = PublicLayout.class)
@RolesAllowed({SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN})
public class StatsView extends VerticalLayout implements HasDynamicTitle {

    public StatsView(DeckUseCase deckUseCase, UserUseCase userUseCase, StatsService statsService) {
        setPadding(true);
        setSpacing(true);
        addClassName("stats-view");

        add(new H3(getTranslation("stats.title")));

        List<Deck> decks =
                deckUseCase.getDecksByUserId(userUseCase.getCurrentUser().getId());
        Map<Long, org.apolenkov.application.domain.port.StatsRepository.DeckAggregate> agg =
                statsService.getDeckAggregates(decks.stream().map(Deck::getId).toList(), LocalDate.now());

        int totalSessionsAll =
                agg.values().stream().mapToInt(a -> a.sessionsAll()).sum();
        int totalViewedAll = agg.values().stream().mapToInt(a -> a.viewedAll()).sum();
        int totalCorrectAll =
                agg.values().stream().mapToInt(a -> a.correctAll()).sum();
        int totalRepeatAll = agg.values().stream().mapToInt(a -> a.repeatAll()).sum();
        int totalHardAll = agg.values().stream().mapToInt(a -> a.hardAll()).sum();
        int totalSessionsToday =
                agg.values().stream().mapToInt(a -> a.sessionsToday()).sum();
        int totalViewedToday =
                agg.values().stream().mapToInt(a -> a.viewedToday()).sum();
        int totalCorrectToday =
                agg.values().stream().mapToInt(a -> a.correctToday()).sum();
        int totalRepeatToday =
                agg.values().stream().mapToInt(a -> a.repeatToday()).sum();
        int totalHardToday = agg.values().stream().mapToInt(a -> a.hardToday()).sum();

        add(new Span(getTranslation(
                "stats.total", totalSessionsAll, totalViewedAll, totalCorrectAll, totalRepeatAll, totalHardAll)));
        add(new Span(getTranslation(
                "stats.today",
                totalSessionsToday,
                totalViewedToday,
                totalCorrectToday,
                totalRepeatToday,
                totalHardToday)));

        add(new H3(getTranslation("stats.byDeck")));
        for (Deck deck : decks) {
            var a = agg.getOrDefault(
                    deck.getId(),
                    new org.apolenkov.application.domain.port.StatsRepository.DeckAggregate(
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            add(new Span(getTranslation(
                    "stats.deckLine",
                    deck.getTitle(),
                    a.sessionsAll(),
                    a.sessionsToday(),
                    a.viewedAll(),
                    a.viewedToday(),
                    a.correctAll(),
                    a.correctToday(),
                    a.repeatAll(),
                    a.repeatToday(),
                    a.hardAll(),
                    a.hardToday())));
        }
    }

    @Override
    public String getPageTitle() {
        return getTranslation("stats.title");
    }
}
