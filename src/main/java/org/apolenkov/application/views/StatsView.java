package org.apolenkov.application.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apolenkov.application.config.SecurityConstants;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.utils.LayoutHelper;
import org.apolenkov.application.views.utils.TextHelper;

@Route(value = "stats", layout = PublicLayout.class)
@RolesAllowed({SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN})
public class StatsView extends VerticalLayout implements HasDynamicTitle {

    // CSS class constants
    private static final String CSS_SECTION = "stats-view__section";
    private static final String CSS_SECTION_TITLE = "stats-view__section-title";

    // Translation key constants
    private static final String STATS_SESSIONS = "stats.sessions";
    private static final String STATS_VIEWED = "stats.viewed";
    private static final String STATS_CORRECT = "stats.correct";
    private static final String STATS_REPEAT = "stats.repeat";
    private static final String STATS_HARD = "stats.hard";

    // Modifier constants
    private static final String MODIFIER_TODAY = "today";

    public StatsView(DeckUseCase deckUseCase, UserUseCase userUseCase, StatsService statsService) {
        setPadding(true);
        setSpacing(true);
        addClassName("stats-view");

        H2 mainTitle = new H2(getTranslation("stats.title"));
        mainTitle.addClassName("stats-view__main-title");
        add(mainTitle);

        List<Deck> decks =
                deckUseCase.getDecksByUserId(userUseCase.getCurrentUser().getId());
        Map<Long, org.apolenkov.application.domain.port.StatsRepository.DeckAggregate> agg =
                statsService.getDeckAggregates(decks.stream().map(Deck::getId).toList(), LocalDate.now());

        add(createOverallStatsSection(agg));

        add(createTodayStatsSection(agg));

        add(createDeckStatsSection(decks, agg));
    }

    private VerticalLayout createOverallStatsSection(Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout section = new VerticalLayout();
        section.addClassName(CSS_SECTION);

        H3 sectionTitle = TextHelper.createSectionTitle(getTranslation("stats.overall"));
        section.add(sectionTitle);

        HorizontalLayout statsGrid = LayoutHelper.createStatsGrid();

        int totalSessions = agg.values().stream()
                .mapToInt(StatsRepository.DeckAggregate::sessionsAll)
                .sum();
        int totalViewed = agg.values().stream()
                .mapToInt(StatsRepository.DeckAggregate::viewedAll)
                .sum();
        int totalCorrect = agg.values().stream()
                .mapToInt(StatsRepository.DeckAggregate::correctAll)
                .sum();
        int totalRepeat = agg.values().stream()
                .mapToInt(StatsRepository.DeckAggregate::repeatAll)
                .sum();
        int totalHard = agg.values().stream()
                .mapToInt(StatsRepository.DeckAggregate::hardAll)
                .sum();

        statsGrid.add(
                createStatCard(STATS_SESSIONS, totalSessions),
                createStatCard(STATS_VIEWED, totalViewed),
                createStatCard(STATS_CORRECT, totalCorrect),
                createStatCard(STATS_REPEAT, totalRepeat),
                createStatCard(STATS_HARD, totalHard));

        section.add(statsGrid);
        return section;
    }

    private VerticalLayout createTodayStatsSection(Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout section = new VerticalLayout();
        section.addClassName(CSS_SECTION);

        H3 sectionTitle = new H3(getTranslation("stats.today"));
        sectionTitle.addClassName(CSS_SECTION_TITLE);
        section.add(sectionTitle);

        HorizontalLayout statsGrid = new HorizontalLayout();
        statsGrid.addClassName("stats-view__stats-grid");
        statsGrid.setWidthFull();

        int todaySessions = agg.values().stream()
                .mapToInt(StatsRepository.DeckAggregate::sessionsToday)
                .sum();
        int todayViewed = agg.values().stream()
                .mapToInt(StatsRepository.DeckAggregate::viewedToday)
                .sum();
        int todayCorrect = agg.values().stream()
                .mapToInt(StatsRepository.DeckAggregate::correctToday)
                .sum();
        int todayRepeat = agg.values().stream()
                .mapToInt(StatsRepository.DeckAggregate::repeatToday)
                .sum();
        int todayHard = agg.values().stream()
                .mapToInt(StatsRepository.DeckAggregate::hardToday)
                .sum();

        statsGrid.add(
                createStatCard(STATS_SESSIONS, todaySessions, MODIFIER_TODAY),
                createStatCard(STATS_VIEWED, todayViewed, MODIFIER_TODAY),
                createStatCard(STATS_CORRECT, todayCorrect, MODIFIER_TODAY),
                createStatCard(STATS_REPEAT, todayRepeat, MODIFIER_TODAY),
                createStatCard(STATS_HARD, todayHard, MODIFIER_TODAY));

        section.add(statsGrid);
        return section;
    }

    private VerticalLayout createDeckStatsSection(List<Deck> decks, Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout section = new VerticalLayout();
        section.addClassName(CSS_SECTION);

        H3 sectionTitle = new H3(getTranslation("stats.byDeck"));
        sectionTitle.addClassName(CSS_SECTION_TITLE);
        section.add(sectionTitle);

        VerticalLayout deckStats = new VerticalLayout();
        deckStats.addClassName("stats-view__deck-stats");
        deckStats.setSpacing(true);

        for (Deck deck : decks) {
            var stats = agg.getOrDefault(deck.getId(), new StatsRepository.DeckAggregate(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));

            deckStats.add(createDeckStatCard(deck, stats));
        }

        section.add(deckStats);
        return section;
    }

    private Div createStatCard(String labelKey, int value) {
        return createStatCard(labelKey, value, "");
    }

    private Div createStatCard(String labelKey, int value, String modifier) {
        Div card = new Div();
        card.addClassName("stats-view__stat-card");
        if (!modifier.isEmpty()) {
            card.addClassName("stats-view__stat-card--" + modifier);
        }

        Div valueDiv = new Div();
        valueDiv.addClassName("stats-view__stat-value");
        valueDiv.setText(String.valueOf(value));

        Div labelDiv = new Div();
        labelDiv.addClassName("stats-view__stat-label");
        labelDiv.setText(getTranslation(labelKey));

        card.add(valueDiv, labelDiv);
        return card;
    }

    private Div createDeckStatCard(Deck deck, StatsRepository.DeckAggregate stats) {
        Div card = new Div();
        card.addClassName("stats-view__deck-card");

        Div header = new Div();
        header.addClassName("stats-view__deck-header");

        H3 deckTitle = new H3(deck.getTitle());
        deckTitle.addClassName("stats-view__deck-title");
        header.add(deckTitle);

        HorizontalLayout deckStatsGrid = new HorizontalLayout();
        deckStatsGrid.addClassName("stats-view__deck-stats-grid");
        deckStatsGrid.setWidthFull();

        deckStatsGrid.add(
                createDeckStatItem(STATS_SESSIONS, stats.sessionsAll(), stats.sessionsToday()),
                createDeckStatItem(STATS_VIEWED, stats.viewedAll(), stats.viewedToday()),
                createDeckStatItem(STATS_CORRECT, stats.correctAll(), stats.correctToday()),
                createDeckStatItem(STATS_REPEAT, stats.repeatAll(), stats.repeatToday()),
                createDeckStatItem(STATS_HARD, stats.hardAll(), stats.hardToday()));

        card.add(header, deckStatsGrid);
        return card;
    }

    private Div createDeckStatItem(String labelKey, int total, int today) {
        Div item = new Div();
        item.addClassName("stats-view__deck-stat-item");

        Div totalDiv = new Div();
        totalDiv.addClassName("stats-view__deck-stat-total");
        totalDiv.setText(String.valueOf(total));

        Div todayDiv = new Div();
        todayDiv.addClassName("stats-view__deck-stat-today");
        todayDiv.setText("+" + today);

        Div labelDiv = new Div();
        labelDiv.addClassName("stats-view__deck-stat-label");
        labelDiv.setText(getTranslation(labelKey));

        item.add(totalDiv, todayDiv, labelDiv);
        return item;
    }

    @Override
    public String getPageTitle() {
        return getTranslation("stats.title");
    }
}
