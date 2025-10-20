package org.apolenkov.application.views.stats.pages;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;
import java.util.Map;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.domain.usecase.DeckUseCase;
import org.apolenkov.application.domain.usecase.UserUseCase;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.stats.StatsService;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.deck.cache.UserDecksCache;
import org.apolenkov.application.views.shared.base.BaseView;
import org.apolenkov.application.views.stats.components.CardVariant;
import org.apolenkov.application.views.stats.components.DeckStatCardCompact;
import org.apolenkov.application.views.stats.components.StatCard;
import org.apolenkov.application.views.stats.components.StatsCalculator;
import org.apolenkov.application.views.stats.constants.StatsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;

/**
 * Modern statistics view with Material Design principles.
 * Displays user learning progress with semantic color coding and single-page layout.
 */
@Route(value = RouteConstants.STATS_ROUTE, layout = PublicLayout.class)
@RolesAllowed({SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN})
public class StatsView extends BaseView implements AfterNavigationObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsView.class);

    // CSS class constants
    private static final String STATS_SECTION_CLASS = "stats-section";
    private static final String STATS_SECTION_TITLE_CLASS = "stats-section__title";

    // ==================== Fields ====================

    // Services
    private final transient DeckUseCase deckUseCase;
    private final transient UserUseCase userUseCase;
    private final transient StatsService statsService;
    private final transient UserDecksCache decksCache;

    // Data
    private transient List<Deck> decks;
    private transient Map<Long, StatsRepository.DeckAggregate> aggregates;

    // Helpers
    private transient StatsCalculator statsCalculator;

    // UI Components (created in @PostConstruct)
    private VerticalLayout pageSection;

    // ==================== Constructor ====================

    /**
     * Creates a new StatsView with required dependencies.
     *
     * @param deckUseCaseParam service for deck operations
     * @param userUseCaseParam service for user operations
     * @param statsServiceParam service for statistics and progress tracking
     * @param decksCacheParam UI-scoped cache for decks (lazy-loaded)
     */
    public StatsView(
            final DeckUseCase deckUseCaseParam,
            final UserUseCase userUseCaseParam,
            final StatsService statsServiceParam,
            @Lazy final UserDecksCache decksCacheParam) {
        this.deckUseCase = deckUseCaseParam;
        this.userUseCase = userUseCaseParam;
        this.statsService = statsServiceParam;
        this.decksCache = decksCacheParam;
    }

    /**
     * Initializes the UI structure (no data loading).
     * Data will be loaded in afterNavigation().
     */
    @PostConstruct
    @SuppressWarnings("unused")
    private void initializeUI() {
        setSpacing(true);
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        addClassName(StatsConstants.STATS_VIEW_CLASS);

        // Main container
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.setWidthFull();
        contentContainer.addClassName(StatsConstants.CONTAINER_MD_CLASS);
        contentContainer.setAlignItems(FlexComponent.Alignment.CENTER);

        // Page section (stored as field for afterNavigation access)
        pageSection = new VerticalLayout();
        pageSection.setSpacing(true);
        pageSection.setWidthFull();
        pageSection.addClassName(StatsConstants.STATS_PAGE_SECTION_CLASS);
        pageSection.addClassName(StatsConstants.SURFACE_PANEL_CLASS);

        contentContainer.add(pageSection);
        add(contentContainer);
    }

    /**
     * Called after navigation to this view is complete.
     * Loads statistics data and populates the view.
     * This method is called ONCE per navigation - no flag needed.
     *
     * @param event the after navigation event
     */
    @Override
    public void afterNavigation(final AfterNavigationEvent event) {
        loadAndDisplayStats();
    }

    /**
     * Loads statistics data and populates the view with Material Design layout.
     */
    private void loadAndDisplayStats() {
        // Add main title
        H2 mainTitle = new H2(getTranslation(StatsConstants.STATS_TITLE_KEY));
        mainTitle.addClassName(StatsConstants.STATS_VIEW_TITLE_CLASS);
        pageSection.add(mainTitle);

        // Load data and initialize builders
        loadStatsData();
        initializeBuilders();

        // Add sections with semantic variants
        pageSection.add(createTodayStatsSection());
        pageSection.add(createOverallStatsSection());
        pageSection.add(createDeckStatsSection());

        LOGGER.debug("Loaded {} decks with statistics", decks.size());
    }

    /**
     * Loads statistics data from services.
     * Uses UI-scoped cache to avoid repeated database queries.
     */
    private void loadStatsData() {
        long userId = userUseCase.getCurrentUser().getId();
        decks = decksCache.getDecks(userId, () -> deckUseCase.getDecksByUserId(userId));

        List<Long> deckIds = decks.stream().map(Deck::getId).toList();
        aggregates = statsService.getDeckAggregates(deckIds);
    }

    /**
     * Initializes all helper components.
     */
    private void initializeBuilders() {
        statsCalculator = new StatsCalculator(aggregates);
    }

    /**
     * Creates today's statistics section with semantic color coding.
     *
     * @return configured vertical layout with today's statistics
     */
    private Component createTodayStatsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.addClassName(STATS_SECTION_CLASS);
        section.addClassName("stats-section--today");

        // Section title
        H2 sectionTitle = new H2(getTranslation(StatsConstants.STATS_TODAY_KEY));
        sectionTitle.addClassName(STATS_SECTION_TITLE_CLASS);
        section.add(sectionTitle);

        // Today's stats grid with semantic variants
        HorizontalLayout statsGrid = createStatsGrid(false);
        statsGrid.addClassName("stats-today-grid");
        section.add(statsGrid);

        return section;
    }

    /**
     * Creates overall statistics section with semantic color coding.
     *
     * @return configured vertical layout with overall statistics
     */
    private Component createOverallStatsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.addClassName(STATS_SECTION_CLASS);
        section.addClassName("stats-section--overall");

        // Section title
        H2 sectionTitle = new H2(getTranslation(StatsConstants.STATS_OVERALL_KEY));
        sectionTitle.addClassName(STATS_SECTION_TITLE_CLASS);
        section.add(sectionTitle);

        // Overall stats grid with semantic variants
        HorizontalLayout statsGrid = createStatsGrid(true);
        statsGrid.addClassName("stats-overall-grid");
        section.add(statsGrid);

        return section;
    }

    /**
     * Creates deck-specific statistics section with vertical list layout.
     *
     * @return configured vertical layout with deck statistics
     */
    private Component createDeckStatsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.addClassName(STATS_SECTION_CLASS);
        section.addClassName("stats-section--decks");

        // Section title
        H2 sectionTitle = new H2(getTranslation(StatsConstants.STATS_BY_DECK_KEY));
        sectionTitle.addClassName(STATS_SECTION_TITLE_CLASS);
        section.add(sectionTitle);

        if (decks.isEmpty()) {
            Span noDecksMessage = new Span(getTranslation(StatsConstants.STATS_NO_DECKS_KEY));
            noDecksMessage.addClassName("stats-no-decks");
            section.add(noDecksMessage);
        } else {
            // Vertical list of compact deck cards
            VerticalLayout decksList = new VerticalLayout();
            decksList.setSpacing(false);
            decksList.addClassName("stats-decks-list");

            for (Deck deck : decks) {
                StatsRepository.DeckAggregate deckStats = aggregates.get(deck.getId());
                if (deckStats != null) {
                    decksList.add(new DeckStatCardCompact(deck, deckStats));
                }
            }

            section.add(decksList);
        }

        return section;
    }

    /**
     * Creates a statistics grid with semantic color-coded cards.
     *
     * @param useOverallStats whether to use overall stats (true) or today's stats (false)
     * @return configured horizontal layout with statistics cards
     */
    private HorizontalLayout createStatsGrid(final boolean useOverallStats) {
        HorizontalLayout statsGrid = new HorizontalLayout();
        statsGrid.setWidthFull();
        statsGrid.setSpacing(true);
        statsGrid.setJustifyContentMode(FlexComponent.JustifyContentMode.EVENLY);

        // Calculate aggregated statistics
        StatsCalculator.StatsValues stats = statsCalculator.calculateStats(useOverallStats);

        // Add statistics cards with semantic variants
        statsGrid.add(
                new StatCard(StatsConstants.STATS_SESSIONS_KEY, stats.sessions(), CardVariant.PRIMARY),
                new StatCard(StatsConstants.STATS_VIEWED_KEY, stats.viewed(), CardVariant.INFO),
                new StatCard(StatsConstants.STATS_CORRECT_KEY, stats.correct(), CardVariant.SUCCESS),
                new StatCard(StatsConstants.STATS_HARD_KEY, stats.hard(), CardVariant.WARNING));

        return statsGrid;
    }

    /**
     * Gets the page title for the stats view.
     *
     * @return the localized stats title
     */
    @Override
    public String getPageTitle() {
        return getTranslation(StatsConstants.STATS_TITLE_KEY);
    }
}
