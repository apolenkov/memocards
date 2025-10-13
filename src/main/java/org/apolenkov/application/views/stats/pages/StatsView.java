package org.apolenkov.application.views.stats.pages;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
import org.apolenkov.application.views.stats.components.CollapsibleSectionBuilder;
import org.apolenkov.application.views.stats.components.DeckPaginationBuilder;
import org.apolenkov.application.views.stats.components.StatCard;
import org.apolenkov.application.views.stats.components.StatsCalculator;
import org.apolenkov.application.views.stats.constants.StatsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;

/**
 * Simplified statistics view with integrated components.
 * Displays user learning progress with collapsible sections for better organization.
 */
@Route(value = RouteConstants.STATS_ROUTE, layout = PublicLayout.class)
@RolesAllowed({SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN})
public class StatsView extends BaseView {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsView.class);

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
     * Initializes the UI components and loads data.
     */
    @PostConstruct
    @SuppressWarnings("unused")
    private void initializeUI() {
        LOGGER.debug("Initializing stats view UI");

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

        // Page section
        VerticalLayout pageSection = new VerticalLayout();
        pageSection.setSpacing(true);
        pageSection.setWidthFull();
        pageSection.addClassName(StatsConstants.STATS_PAGE_SECTION_CLASS);
        pageSection.addClassName(StatsConstants.SURFACE_PANEL_CLASS);

        contentContainer.add(pageSection);
        add(contentContainer);

        // Load and display data
        loadAndDisplayStats(pageSection);
    }

    /**
     * Loads statistics data and populates the view.
     *
     * @param pageSection the container to add stats sections to
     */
    private void loadAndDisplayStats(final VerticalLayout pageSection) {
        LOGGER.debug("Loading statistics data for current user");

        // Add main title
        H2 mainTitle = new H2(getTranslation(StatsConstants.STATS_TITLE_KEY));
        mainTitle.addClassName(StatsConstants.STATS_VIEW_TITLE_CLASS);
        pageSection.add(mainTitle);

        // Load data and initialize builders
        loadStatsData();
        initializeBuilders();

        // Add sections
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
     * Creates today's statistics section.
     *
     * @return configured collapsible section
     */
    private Component createTodayStatsSection() {
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.add(createStatsGrid(false)); // today's stats

        return new CollapsibleSectionBuilder(StatsConstants.STATS_TODAY_KEY, contentContainer, true); // open by default
    }

    /**
     * Creates overall statistics section.
     *
     * @return configured collapsible section
     */
    private Component createOverallStatsSection() {
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.add(createStatsGrid(true)); // overall stats

        return new CollapsibleSectionBuilder(
                StatsConstants.STATS_OVERALL_KEY, contentContainer, false); // closed by default
    }

    /**
     * Creates deck-specific statistics section with pagination.
     *
     * @return configured collapsible section
     */
    private Component createDeckStatsSection() {
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.setAlignItems(FlexComponent.Alignment.CENTER);

        if (decks.isEmpty()) {
            contentContainer.add(new Span(getTranslation(StatsConstants.STATS_NO_DECKS_KEY)));
        } else {
            DeckPaginationBuilder paginationBuilder = new DeckPaginationBuilder(decks, aggregates);
            contentContainer.add(paginationBuilder);
        }

        return new CollapsibleSectionBuilder(StatsConstants.STATS_BY_DECK_KEY, contentContainer, false);
    }

    /**
     * Creates a statistics grid with cards.
     *
     * @param useOverallStats whether to use overall stats (true) or today's stats (false)
     * @return configured horizontal layout with statistics cards
     */
    private HorizontalLayout createStatsGrid(final boolean useOverallStats) {
        HorizontalLayout statsGrid = new HorizontalLayout();
        statsGrid.setWidthFull();
        statsGrid.setSpacing(true);
        statsGrid.addClassName(
                useOverallStats ? StatsConstants.STATS_OVERALL_GRID_CLASS : StatsConstants.STATS_TODAY_GRID_CLASS);
        statsGrid.setJustifyContentMode(FlexComponent.JustifyContentMode.EVENLY);

        // Calculate aggregated statistics
        StatsCalculator.StatsValues stats = statsCalculator.calculateStats(useOverallStats);

        // Add statistics cards
        statsGrid.add(
                new StatCard(StatsConstants.STATS_SESSIONS_KEY, stats.sessions()),
                new StatCard(StatsConstants.STATS_VIEWED_KEY, stats.viewed()),
                new StatCard(StatsConstants.STATS_CORRECT_KEY, stats.correct()),
                new StatCard(StatsConstants.STATS_HARD_KEY, stats.hard()));

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
