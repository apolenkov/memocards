package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
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
        setSpacing(true);
        setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.CENTER);
        addClassName("stats-view");

        // Create a container with consistent width
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.setWidthFull();
        contentContainer.addClassName("container-md");
        contentContainer.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        // Single shaded section holds title and all groups
        VerticalLayout pageSection = new VerticalLayout();
        pageSection.setSpacing(true);
        pageSection.setWidthFull();
        pageSection.addClassName("stats-page__section");
        pageSection.addClassName("surface-panel");

        H2 mainTitle = new H2(getTranslation("stats.title"));
        mainTitle.addClassName("stats-view__title");
        pageSection.add(mainTitle);

        List<Deck> decks =
                deckUseCase.getDecksByUserId(userUseCase.getCurrentUser().getId());
        Map<Long, org.apolenkov.application.domain.port.StatsRepository.DeckAggregate> agg =
                statsService.getDeckAggregates(decks.stream().map(Deck::getId).toList(), LocalDate.now());

        pageSection.add(createTodayStatsSection(agg));
        pageSection.add(createOverallStatsSection(agg));
        pageSection.add(createDeckStatsSection(decks, agg));

        contentContainer.add(pageSection);
        add(contentContainer);
    }

    private VerticalLayout createOverallStatsSection(Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setWidthFull();
        section.addClassName("stats-section");
        section.addClassName("surface-panel");

        // Create collapsible header
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H3 sectionTitle = TextHelper.createSectionTitle(getTranslation("stats.overall"));
        sectionTitle.addClassName("stats-section__title");

        Button toggleButton = new Button(VaadinIcon.CHEVRON_DOWN.create());
        toggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        toggleButton.getElement().setAttribute("title", getTranslation("stats.collapse"));

        headerLayout.add(sectionTitle, toggleButton);
        section.add(headerLayout);

        HorizontalLayout statsGrid = LayoutHelper.createStatsGrid();
        statsGrid.addClassName("stats-overall-grid");
        statsGrid.setJustifyContentMode(JustifyContentMode.EVENLY);

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

        // Create content container
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.add(statsGrid);

        // Hide section by default
        contentContainer.setVisible(false);
        toggleButton.setIcon(VaadinIcon.CHEVRON_RIGHT.create());
        toggleButton.getElement().setAttribute("title", getTranslation("stats.expand"));

        // Add toggle functionality
        toggleButton.addClickListener(e -> {
            if (contentContainer.isVisible()) {
                contentContainer.setVisible(false);
                toggleButton.setIcon(VaadinIcon.CHEVRON_RIGHT.create());
                toggleButton.getElement().setAttribute("title", getTranslation("stats.expand"));
            } else {
                contentContainer.setVisible(true);
                toggleButton.setIcon(VaadinIcon.CHEVRON_DOWN.create());
                toggleButton.getElement().setAttribute("title", getTranslation("stats.collapse"));
            }
        });

        section.add(contentContainer);
        return section;
    }

    private VerticalLayout createTodayStatsSection(Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setWidthFull();
        section.addClassName("stats-section");
        section.addClassName("surface-panel");

        // Create collapsible header
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H3 sectionTitle = new H3(getTranslation("stats.today"));
        sectionTitle.addClassName("stats-section__title");

        Button toggleButton = new Button(VaadinIcon.CHEVRON_DOWN.create());
        toggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        toggleButton.getElement().setAttribute("title", getTranslation("stats.collapse"));

        headerLayout.add(sectionTitle, toggleButton);
        section.add(headerLayout);

        HorizontalLayout statsGrid = new HorizontalLayout();
        statsGrid.setWidthFull();
        statsGrid.addClassName("stats-today-grid");
        statsGrid.setJustifyContentMode(JustifyContentMode.EVENLY);

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
                createStatCard(STATS_SESSIONS, todaySessions),
                createStatCard(STATS_VIEWED, todayViewed),
                createStatCard(STATS_CORRECT, todayCorrect),
                createStatCard(STATS_REPEAT, todayRepeat),
                createStatCard(STATS_HARD, todayHard));

        // Create content container
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.add(statsGrid);

        // Add toggle functionality
        toggleButton.addClickListener(e -> {
            if (contentContainer.isVisible()) {
                contentContainer.setVisible(false);
                toggleButton.setIcon(VaadinIcon.CHEVRON_RIGHT.create());
                toggleButton.getElement().setAttribute("title", getTranslation("stats.expand"));
            } else {
                contentContainer.setVisible(true);
                toggleButton.setIcon(VaadinIcon.CHEVRON_DOWN.create());
                toggleButton.getElement().setAttribute("title", getTranslation("stats.collapse"));
            }
        });

        section.add(contentContainer);
        return section;
    }

    private VerticalLayout createDeckStatsSection(List<Deck> decks, Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.setWidthFull();
        section.addClassName("stats-section");
        section.addClassName("surface-panel");

        // Create collapsible header
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H3 sectionTitle = new H3(getTranslation("stats.byDeck"));
        sectionTitle.addClassName("stats-section__title");

        Button toggleButton = new Button(VaadinIcon.CHEVRON_DOWN.create());
        toggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        toggleButton.getElement().setAttribute("title", getTranslation("stats.collapse"));

        headerLayout.add(sectionTitle, toggleButton);
        section.add(headerLayout);

        // Create paginated container for deck stats
        VerticalLayout paginatedContainer = new VerticalLayout();
        paginatedContainer.setSpacing(true);
        paginatedContainer.setAlignItems(Alignment.CENTER);

        // Navigation controls
        HorizontalLayout navigationLayout = new HorizontalLayout();
        navigationLayout.setSpacing(true);
        navigationLayout.setAlignItems(Alignment.CENTER);
        navigationLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        Button prevButton = new Button(VaadinIcon.CHEVRON_LEFT.create());
        prevButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        prevButton.getElement().setAttribute("title", getTranslation("stats.previousDeck"));

        Button nextButton = new Button(VaadinIcon.CHEVRON_RIGHT.create());
        nextButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        nextButton.getElement().setAttribute("title", getTranslation("stats.nextDeck"));

        // Page indicator
        Span pageIndicator = new Span();
        pageIndicator.addClassName("stats-pagination__indicator");

        navigationLayout.add(prevButton, pageIndicator, nextButton);

        // Current deck display
        Div currentDeckContainer = new Div();
        currentDeckContainer.setWidthFull();
        currentDeckContainer.addClassName("stats-current-deck__container");

        // Initialize pagination state
        final int[] currentIndex = {0};
        final int totalDecks = decks.size();

        // Update display function
        java.util.function.Consumer<Integer> updateDisplay = index -> {
            if (totalDecks == 0) {
                currentDeckContainer.removeAll();
                currentDeckContainer.add(new Span(getTranslation("stats.noDecks")));
                pageIndicator.setText("");
                prevButton.setEnabled(false);
                nextButton.setEnabled(false);
                return;
            }

            currentDeckContainer.removeAll();
            Deck currentDeck = decks.get(index);
            var stats = agg.getOrDefault(
                    currentDeck.getId(), new StatsRepository.DeckAggregate(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            currentDeckContainer.add(createDeckStatCard(currentDeck, stats));

            // Update page indicator
            pageIndicator.setText(getTranslation("stats.deckPage", index + 1, totalDecks));

            // Update button states
            prevButton.setEnabled(index > 0);
            nextButton.setEnabled(index < totalDecks - 1);
        };

        // Button click handlers
        prevButton.addClickListener(e -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                updateDisplay.accept(currentIndex[0]);
            }
        });

        nextButton.addClickListener(e -> {
            if (currentIndex[0] < totalDecks - 1) {
                currentIndex[0]++;
                updateDisplay.accept(currentIndex[0]);
            }
        });

        // Initial display
        updateDisplay.accept(0);

        paginatedContainer.add(navigationLayout, currentDeckContainer);

        // Create content container
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setPadding(false);
        contentContainer.setSpacing(true);
        contentContainer.add(paginatedContainer);

        // Hide section by default
        contentContainer.setVisible(false);
        toggleButton.setIcon(VaadinIcon.CHEVRON_RIGHT.create());
        toggleButton.getElement().setAttribute("title", getTranslation("stats.expand"));

        // Add toggle functionality
        toggleButton.addClickListener(e -> {
            if (contentContainer.isVisible()) {
                contentContainer.setVisible(false);
                toggleButton.setIcon(VaadinIcon.CHEVRON_RIGHT.create());
                toggleButton.getElement().setAttribute("title", getTranslation("stats.expand"));
            } else {
                contentContainer.setVisible(true);
                toggleButton.setIcon(VaadinIcon.CHEVRON_DOWN.create());
                toggleButton.getElement().setAttribute("title", getTranslation("stats.collapse"));
            }
        });

        section.add(contentContainer);
        return section;
    }

    private Div createStatCard(String labelKey, int value) {
        return createStatCard(labelKey, value, "");
    }

    private Div createStatCard(String labelKey, int value, String modifier) {
        Div card = new Div();
        card.addClassName("stats-card");
        card.addClassName("surface-card");
        if (!modifier.isEmpty()) {
            card.addClassName("stats-card--" + modifier);
        }

        Div valueDiv = new Div();
        valueDiv.addClassName("stats-card__value");
        valueDiv.setText(String.valueOf(value));

        Div labelDiv = new Div();
        labelDiv.addClassName("stats-card__label");
        labelDiv.setText(getTranslation(labelKey));

        card.add(valueDiv, labelDiv);
        return card;
    }

    private Div createDeckStatCard(Deck deck, StatsRepository.DeckAggregate stats) {
        Div card = new Div();
        card.addClassName("deck-stats-card");
        card.addClassName("surface-card");

        Div header = new Div();
        header.addClassName("deck-stats-card__header");

        H3 deckTitle = new H3(deck.getTitle());
        deckTitle.addClassName("deck-stats-card__title");

        header.add(deckTitle);

        HorizontalLayout deckStatsGrid = new HorizontalLayout();
        deckStatsGrid.setWidthFull();
        deckStatsGrid.setSpacing(true);
        deckStatsGrid.addClassName("stats-deck-grid");
        deckStatsGrid.setJustifyContentMode(JustifyContentMode.EVENLY);

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
        item.addClassName("stats-deck-item");

        Div totalDiv = new Div();
        totalDiv.addClassName("stats-deck-item__total");
        totalDiv.setText(String.valueOf(total));

        Div todayDiv = new Div();
        todayDiv.addClassName("stats-deck-item__today");
        todayDiv.setText("+" + today);

        Div labelDiv = new Div();
        labelDiv.addClassName("stats-deck-item__label");
        labelDiv.setText(getTranslation(labelKey));

        item.add(totalDiv, todayDiv, labelDiv);
        return item;
    }

    @Override
    public String getPageTitle() {
        return getTranslation("stats.title");
    }
}
