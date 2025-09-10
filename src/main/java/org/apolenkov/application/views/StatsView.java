package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.LayoutHelper;

/**
 * View for displaying user statistics and analytics.
 * This view provides comprehensive statistics about user's learning progress,
 * including today's stats, overall statistics, and detailed deck-specific metrics.
 * The statistics are presented in collapsible sections for better organization.
 */
@Route(value = "stats", layout = PublicLayout.class)
@RolesAllowed({SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN})
public class StatsView extends VerticalLayout implements HasDynamicTitle {

    private static final String SURFACE_PANEL_CLASS = "surface-panel";
    private static final String SURFACE_CARD_CLASS = "surface-card";
    private static final String STATS_SECTION_CLASS = "stats-section";
    private static final String STATS_SECTION_TITLE_CLASS = "stats-section__title";
    private static final String TITLE_ATTRIBUTE = "title";
    private static final String STATS_COLLAPSE_KEY = "stats.collapse";
    private static final String STATS_EXPAND_KEY = "stats.expand";

    // Translation key constants
    private static final String STATS_SESSIONS = "stats.sessions";
    private static final String STATS_VIEWED = "stats.viewed";
    private static final String STATS_CORRECT = "stats.correct";
    private static final String STATS_HARD = "stats.hard";

    private final transient DeckUseCase deckUseCase;
    private final transient UserUseCase userUseCase;
    private final transient StatsService statsService;

    /**
     * Creates a new StatsView with required dependencies.
     *
     * @param deckUseCaseParam service for deck operations
     * @param userUseCaseParam service for user operations
     * @param statsServiceParam service for statistics and progress tracking
     */
    public StatsView(
            final DeckUseCase deckUseCaseParam,
            final UserUseCase userUseCaseParam,
            final StatsService statsServiceParam) {
        this.deckUseCase = deckUseCaseParam;
        this.userUseCase = userUseCaseParam;
        this.statsService = statsServiceParam;
    }

    /**
     * Initializes the UI components and loads data after construction.
     */
    @PostConstruct
    private void initializeUI() {
        setSpacing(true);
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        addClassName("stats-view");

        // Create a container with consistent width
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.setWidthFull();
        contentContainer.addClassName("container-md");
        contentContainer.setAlignItems(FlexComponent.Alignment.CENTER);

        // Single shaded section holds title and all groups
        VerticalLayout pageSection = new VerticalLayout();
        pageSection.setSpacing(true);
        pageSection.setWidthFull();
        pageSection.addClassName("stats-page__section");
        pageSection.addClassName(SURFACE_PANEL_CLASS);

        contentContainer.add(pageSection);
        add(contentContainer);

        // Load data after UI initialization
        loadStatsData(pageSection);
    }

    /**
     * Loads statistics data and populates the view.
     *
     * @param pageSection the container to add stats sections to
     */
    private void loadStatsData(final VerticalLayout pageSection) {
        H2 mainTitle = new H2(getTranslation("stats.title"));
        mainTitle.addClassName("stats-view__title");
        pageSection.add(mainTitle);

        List<Deck> decks =
                deckUseCase.getDecksByUserId(userUseCase.getCurrentUser().getId());
        Map<Long, StatsRepository.DeckAggregate> agg =
                statsService.getDeckAggregates(decks.stream().map(Deck::getId).toList());

        pageSection.add(createTodayStatsSection(agg));
        pageSection.add(createOverallStatsSection(agg));
        pageSection.add(createDeckStatsSection(decks, agg));
    }

    /**
     * Creates the overall statistics section with collapsible content.
     *
     * @param agg aggregated statistics data for all decks
     * @return configured vertical layout for overall stats
     */
    private VerticalLayout createOverallStatsSection(final Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout section = createStatsSectionHeader("stats.overall");

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
        int totalHard = agg.values().stream()
                .mapToInt(StatsRepository.DeckAggregate::hardAll)
                .sum();

        statsGrid.add(
                createStatCard(STATS_SESSIONS, totalSessions),
                createStatCard(STATS_VIEWED, totalViewed),
                createStatCard(STATS_CORRECT, totalCorrect),
                createStatCard(STATS_HARD, totalHard));

        // Create content container
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.add(statsGrid);

        setupCollapsibleSection(section, contentContainer);
        return section;
    }

    /**
     * Creates the today's statistics section with collapsible content.
     *
     * @param agg aggregated statistics data for all decks
     * @return configured vertical layout for today's stats
     */
    private VerticalLayout createTodayStatsSection(final Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout section = createStatsSectionHeader("stats.today");

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
        int todayHard = agg.values().stream()
                .mapToInt(StatsRepository.DeckAggregate::hardToday)
                .sum();

        statsGrid.add(
                createStatCard(STATS_SESSIONS, todaySessions),
                createStatCard(STATS_VIEWED, todayViewed),
                createStatCard(STATS_CORRECT, todayCorrect),
                createStatCard(STATS_HARD, todayHard));

        // Create content container
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.add(statsGrid);

        setupCollapsibleSection(section, contentContainer, true); // Today section is open by default
        return section;
    }

    /**
     * Creates the deck-specific statistics section with collapsible content.
     *
     * @param decks list of user's decks
     * @param agg aggregated statistics data for all decks
     * @return configured vertical layout for deck stats
     */
    private VerticalLayout createDeckStatsSection(
            final List<Deck> decks, final Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout section = createStatsSectionHeader("stats.byDeck");
        VerticalLayout contentContainer = createDeckStatsContent(decks, agg);
        setupCollapsibleSection(section, contentContainer);
        return section;
    }

    /**
     * Creates the content container for deck statistics with pagination.
     *
     * @param decks list of user's decks
     * @param agg aggregated statistics data for all decks
     * @return configured vertical layout with paginated deck stats
     */
    private VerticalLayout createDeckStatsContent(
            final List<Deck> decks, final Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setPadding(false);
        contentContainer.setSpacing(true);

        VerticalLayout paginatedContainer = createPaginatedDeckStats(decks, agg);
        contentContainer.add(paginatedContainer);

        return contentContainer;
    }

    /**
     * Creates paginated container for deck statistics with navigation controls.
     *
     * @param decks list of user's decks
     * @param agg aggregated statistics data for all decks
     * @return configured vertical layout with pagination
     */
    private VerticalLayout createPaginatedDeckStats(
            final List<Deck> decks, final Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout paginatedContainer = new VerticalLayout();
        paginatedContainer.setSpacing(true);
        paginatedContainer.setAlignItems(Alignment.CENTER);

        // Initialize pagination state
        final int[] currentIndex = {0};
        final int totalDecks = decks.size();

        // Create navigation controls
        HorizontalLayout navigationLayout = createDeckNavigationControls();
        Span pageIndicator = (Span) navigationLayout
                .getChildren()
                .filter(Span.class::isInstance)
                .findFirst()
                .orElse(null);

        // Current deck display
        Div currentDeckContainer = new Div();
        currentDeckContainer.setWidthFull();
        currentDeckContainer.addClassName("stats-current-deck__container");

        // Create update display function
        IntConsumer updateDisplay = createDeckUpdateDisplayFunction(
                decks, agg, currentDeckContainer, pageIndicator, navigationLayout, totalDecks);

        // Setup button click handlers
        setupDeckNavigationHandlers(navigationLayout, currentIndex, updateDisplay, totalDecks);

        // Initial display
        updateDisplay.accept(0);

        paginatedContainer.add(navigationLayout, currentDeckContainer);
        return paginatedContainer;
    }

    /**
     * Creates navigation controls for deck statistics pagination.
     *
     * @return configured horizontal layout with navigation buttons
     */
    private HorizontalLayout createDeckNavigationControls() {
        HorizontalLayout navigationLayout = new HorizontalLayout();
        navigationLayout.setSpacing(true);
        navigationLayout.setAlignItems(Alignment.CENTER);
        navigationLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        Button prevButton = ButtonHelper.createIconButton(
                VaadinIcon.CHEVRON_LEFT, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        prevButton.getElement().setAttribute(TITLE_ATTRIBUTE, getTranslation("stats.previousDeck"));

        Button nextButton = ButtonHelper.createIconButton(
                VaadinIcon.CHEVRON_RIGHT, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        nextButton.getElement().setAttribute(TITLE_ATTRIBUTE, getTranslation("stats.nextDeck"));

        // Page indicator
        Span pageIndicator = new Span();
        pageIndicator.addClassName("stats-pagination__indicator");

        navigationLayout.add(prevButton, pageIndicator, nextButton);
        return navigationLayout;
    }

    /**
     * Creates the update display function for deck statistics.
     *
     * @param decks list of user's decks
     * @param agg aggregated statistics data for all decks
     * @param currentDeckContainer container for current deck display
     * @param pageIndicator span for page indicator
     * @param navigationLayout layout containing navigation buttons
     * @param totalDecks total number of decks
     * @return function to update display based on current index
     */
    private IntConsumer createDeckUpdateDisplayFunction(
            final List<Deck> decks,
            final Map<Long, StatsRepository.DeckAggregate> agg,
            final Div currentDeckContainer,
            final Span pageIndicator,
            final HorizontalLayout navigationLayout,
            final int totalDecks) {
        return index -> {
            if (totalDecks == 0) {
                currentDeckContainer.removeAll();
                currentDeckContainer.add(new Span(getTranslation("stats.noDecks")));
                pageIndicator.setText("");
                setNavigationButtonsEnabled(navigationLayout, false, false);
                return;
            }

            currentDeckContainer.removeAll();
            Deck currentDeck = decks.get(index);
            var stats =
                    agg.getOrDefault(currentDeck.getId(), new StatsRepository.DeckAggregate(0, 0, 0, 0, 0, 0, 0, 0));
            currentDeckContainer.add(createDeckStatCard(currentDeck, stats));

            // Update page indicator
            pageIndicator.setText(getTranslation("stats.deckPage", index + 1, totalDecks));

            // Update button states
            setNavigationButtonsEnabled(navigationLayout, index > 0, index < totalDecks - 1);
        };
    }

    /**
     * Sets up click handlers for deck navigation buttons.
     *
     * @param navigationLayout layout containing navigation buttons
     * @param currentIndex current page index (mutable array)
     * @param updateDisplay function to update display
     * @param totalDecks total number of decks
     */
    private void setupDeckNavigationHandlers(
            final HorizontalLayout navigationLayout,
            final int[] currentIndex,
            final IntConsumer updateDisplay,
            final int totalDecks) {
        Button prevButton = (Button) navigationLayout
                .getChildren()
                .filter(Button.class::isInstance)
                .findFirst()
                .orElse(null);
        Button nextButton = (Button) navigationLayout
                .getChildren()
                .filter(Button.class::isInstance)
                .skip(1)
                .findFirst()
                .orElse(null);

        if (prevButton != null) {
            prevButton.addClickListener(e -> {
                if (currentIndex[0] > 0) {
                    currentIndex[0]--;
                    updateDisplay.accept(currentIndex[0]);
                }
            });
        }

        if (nextButton != null) {
            nextButton.addClickListener(e -> {
                if (currentIndex[0] < totalDecks - 1) {
                    currentIndex[0]++;
                    updateDisplay.accept(currentIndex[0]);
                }
            });
        }
    }

    /**
     * Sets the enabled state of navigation buttons.
     *
     * @param navigationLayout layout containing navigation buttons
     * @param prevEnabled whether previous button should be enabled
     * @param nextEnabled whether next button should be enabled
     */
    private void setNavigationButtonsEnabled(
            final HorizontalLayout navigationLayout, final boolean prevEnabled, final boolean nextEnabled) {
        Button prevButton = (Button) navigationLayout
                .getChildren()
                .filter(Button.class::isInstance)
                .findFirst()
                .orElse(null);
        Button nextButton = (Button) navigationLayout
                .getChildren()
                .filter(Button.class::isInstance)
                .skip(1)
                .findFirst()
                .orElse(null);

        if (prevButton != null) {
            prevButton.setEnabled(prevEnabled);
        }
        if (nextButton != null) {
            nextButton.setEnabled(nextEnabled);
        }
    }

    /**
     * Creates a statistics section header with title and toggle button.
     *
     * @param titleKey translation key for the section title
     * @return configured vertical layout with header
     */
    private VerticalLayout createStatsSectionHeader(final String titleKey) {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.setWidthFull();
        section.addClassName(STATS_SECTION_CLASS);
        section.addClassName(SURFACE_PANEL_CLASS);

        // Create collapsible header
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.addClassName("stats-section__header");

        H3 sectionTitle = new H3(getTranslation(titleKey));
        sectionTitle.addClassName(STATS_SECTION_TITLE_CLASS);

        Button toggleButton = ButtonHelper.createIconButton(VaadinIcon.CHEVRON_DOWN, ButtonVariant.LUMO_TERTIARY);
        toggleButton.getElement().setAttribute(TITLE_ATTRIBUTE, getTranslation(STATS_COLLAPSE_KEY));

        headerLayout.add(sectionTitle, toggleButton);
        section.add(headerLayout);

        return section;
    }

    /**
     * Sets up collapsible functionality for a statistics section.
     *
     * @param section the main section container
     * @param contentContainer the content to be collapsed/expanded
     * @param openByDefault whether the section should be open by default
     */
    private void setupCollapsibleSection(
            final VerticalLayout section, final VerticalLayout contentContainer, final boolean openByDefault) {
        // Set initial visibility
        contentContainer.setVisible(openByDefault);

        // Find header layout and components
        HorizontalLayout headerLayout = (HorizontalLayout) section.getChildren()
                .filter(HorizontalLayout.class::isInstance)
                .findFirst()
                .orElse(null);

        if (headerLayout == null) {
            section.add(contentContainer);
            return;
        }

        // Find title and toggle button
        H3 sectionTitle = (H3) headerLayout
                .getChildren()
                .filter(H3.class::isInstance)
                .findFirst()
                .orElse(null);

        Button toggleButton = (Button) headerLayout
                .getChildren()
                .filter(Button.class::isInstance)
                .findFirst()
                .orElse(null);

        if (toggleButton != null) {
            // Set initial icon and tooltip
            if (openByDefault) {
                toggleButton.setIcon(VaadinIcon.CHEVRON_DOWN.create());
                toggleButton.getElement().setAttribute(TITLE_ATTRIBUTE, getTranslation(STATS_COLLAPSE_KEY));
            } else {
                toggleButton.setIcon(VaadinIcon.CHEVRON_RIGHT.create());
                toggleButton.getElement().setAttribute(TITLE_ATTRIBUTE, getTranslation(STATS_EXPAND_KEY));
            }

            // Toggle functionality
            Runnable toggleAction = () -> {
                if (contentContainer.isVisible()) {
                    contentContainer.setVisible(false);
                    toggleButton.setIcon(VaadinIcon.CHEVRON_RIGHT.create());
                    toggleButton.getElement().setAttribute(TITLE_ATTRIBUTE, getTranslation(STATS_EXPAND_KEY));
                } else {
                    contentContainer.setVisible(true);
                    toggleButton.setIcon(VaadinIcon.CHEVRON_DOWN.create());
                    toggleButton.getElement().setAttribute(TITLE_ATTRIBUTE, getTranslation(STATS_COLLAPSE_KEY));
                }
            };

            // Add click listener to toggle button
            toggleButton.addClickListener(e -> toggleAction.run());

            // Make entire header clickable for better UX
            headerLayout.getStyle().set("cursor", "pointer");
            headerLayout.addClickListener(e -> toggleAction.run());

            // Style section title for better visual feedback
            if (sectionTitle != null) {
                sectionTitle.getStyle().set("cursor", "pointer");
            }
        }

        section.add(contentContainer);
    }

    /**
     * Sets up collapsible functionality for a statistics section (closed by default).
     *
     * @param section the main section container
     * @param contentContainer the content to be collapsed/expanded
     */
    private void setupCollapsibleSection(final VerticalLayout section, final VerticalLayout contentContainer) {
        setupCollapsibleSection(section, contentContainer, false);
    }

    /**
     * Creates a statistics card with label, value and CSS modifier.
     *
     * @param labelKey translation key for the label
     * @param value numeric value to display
     * @return configured statistics card component
     */
    private Div createStatCard(final String labelKey, final int value) {
        Div card = new Div();
        card.addClassName("stats-card");
        card.addClassName(SURFACE_CARD_CLASS);

        Div valueDiv = new Div();
        valueDiv.addClassName("stats-card__value");
        valueDiv.setText(String.valueOf(value));

        Div labelDiv = new Div();
        labelDiv.addClassName("stats-card__label");
        labelDiv.setText(getTranslation(labelKey));

        card.add(valueDiv, labelDiv);
        return card;
    }

    /**
     * Creates a deck statistics card with comprehensive stats display.
     *
     * @param deck the deck to display statistics for
     * @param stats aggregated statistics for the deck
     * @return configured deck statistics card component
     */
    private Div createDeckStatCard(final Deck deck, final StatsRepository.DeckAggregate stats) {
        Div card = new Div();
        card.addClassName("deck-stats-card");
        card.addClassName(SURFACE_CARD_CLASS);

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
                createDeckStatItem(STATS_HARD, stats.hardAll(), stats.hardToday()));

        card.add(header, deckStatsGrid);
        return card;
    }

    /**
     * Creates a deck statistics item showing total and today's values.
     *
     * @param labelKey translation key for the label
     * @param total total value for all time
     * @param today today's value
     * @return configured deck statistics item component
     */
    private Div createDeckStatItem(final String labelKey, final int total, final int today) {
        Div item = new Div();
        item.addClassName("stats-deck-item");
        item.addClassName(SURFACE_CARD_CLASS);

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

    /**
     * Gets the page title for the stats view.
     *
     * @return the localized stats title
     */
    @Override
    public String getPageTitle() {
        return getTranslation("stats.title");
    }
}
