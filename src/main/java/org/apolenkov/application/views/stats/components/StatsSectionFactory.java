package org.apolenkov.application.views.stats.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.views.shared.utils.ButtonHelper;

/**
 * Factory for creating statistics section components.
 * Handles creation of collapsible sections with consistent styling and behavior.
 */
public final class StatsSectionFactory {

    private static final String SURFACE_PANEL_CLASS = "surface-panel";
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

    // Dependencies
    private StatsCardFactory cardFactory;
    private TranslationProvider translationProvider;

    /**
     * Sets the card factory for creating statistics cards.
     *
     * @param cardFactoryParam factory for creating cards
     */
    public void setCardFactory(final StatsCardFactory cardFactoryParam) {
        this.cardFactory = cardFactoryParam;
    }

    /**
     * Sets the translation provider for localized strings.
     *
     * @param translationProviderParam provider for translations
     */
    public void setTranslationProvider(final TranslationProvider translationProviderParam) {
        this.translationProvider = translationProviderParam;
    }

    /**
     * Interface for providing translations.
     * Used to decouple translation functionality from Vaadin components.
     */
    public interface TranslationProvider {
        /**
         * Gets a translated string for the given key with optional parameters.
         *
         * @param key the translation key
         * @param params optional parameters for string formatting
         * @return the translated string
         */
        String getTranslation(String key, Object... params);
    }

    /**
     * Creates the overall statistics section with collapsible content.
     *
     * @param agg aggregated statistics data for all decks
     * @return configured vertical layout for overall stats
     */
    public VerticalLayout createOverallStatsSection(final Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout section = createStatsSectionHeader(translationProvider.getTranslation("stats.overall"));

        HorizontalLayout statsGrid = new HorizontalLayout();
        statsGrid.setWidthFull();
        statsGrid.setSpacing(true);
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
                cardFactory.createStatCard(STATS_SESSIONS, totalSessions),
                cardFactory.createStatCard(STATS_VIEWED, totalViewed),
                cardFactory.createStatCard(STATS_CORRECT, totalCorrect),
                cardFactory.createStatCard(STATS_HARD, totalHard));

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
    public VerticalLayout createTodayStatsSection(final Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout section = createStatsSectionHeader(translationProvider.getTranslation("stats.today"));

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
                cardFactory.createStatCard(STATS_SESSIONS, todaySessions),
                cardFactory.createStatCard(STATS_VIEWED, todayViewed),
                cardFactory.createStatCard(STATS_CORRECT, todayCorrect),
                cardFactory.createStatCard(STATS_HARD, todayHard));

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
    public VerticalLayout createDeckStatsSection(
            final List<Deck> decks, final Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout section = createStatsSectionHeader(translationProvider.getTranslation("stats.byDeck"));
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
        VerticalLayout currentDeckContainer = new VerticalLayout();
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
        prevButton.getElement().setAttribute(TITLE_ATTRIBUTE, translationProvider.getTranslation("stats.previousDeck"));

        Button nextButton = ButtonHelper.createIconButton(
                VaadinIcon.CHEVRON_RIGHT, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        nextButton.getElement().setAttribute(TITLE_ATTRIBUTE, translationProvider.getTranslation("stats.nextDeck"));

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
            final VerticalLayout currentDeckContainer,
            final Span pageIndicator,
            final HorizontalLayout navigationLayout,
            final int totalDecks) {
        return index -> {
            if (totalDecks == 0) {
                currentDeckContainer.removeAll();
                currentDeckContainer.add(new Span(translationProvider.getTranslation("stats.noDecks")));
                pageIndicator.setText("");
                setNavigationButtonsEnabled(navigationLayout, false, false);
                return;
            }

            currentDeckContainer.removeAll();
            Deck currentDeck = decks.get(index);
            var stats =
                    agg.getOrDefault(currentDeck.getId(), new StatsRepository.DeckAggregate(0, 0, 0, 0, 0, 0, 0, 0));
            currentDeckContainer.add(cardFactory.createDeckStatCard(currentDeck, stats));

            // Update page indicator
            pageIndicator.setText(translationProvider.getTranslation("stats.deckPage", index + 1, totalDecks));

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

        H3 sectionTitle = new H3(translationProvider.getTranslation(titleKey));
        sectionTitle.addClassName(STATS_SECTION_TITLE_CLASS);

        Button toggleButton = ButtonHelper.createIconButton(VaadinIcon.CHEVRON_DOWN, ButtonVariant.LUMO_TERTIARY);
        toggleButton.getElement().setAttribute(TITLE_ATTRIBUTE, translationProvider.getTranslation(STATS_COLLAPSE_KEY));

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
                toggleButton
                        .getElement()
                        .setAttribute(TITLE_ATTRIBUTE, translationProvider.getTranslation(STATS_COLLAPSE_KEY));
            } else {
                toggleButton.setIcon(VaadinIcon.CHEVRON_RIGHT.create());
                toggleButton
                        .getElement()
                        .setAttribute(TITLE_ATTRIBUTE, translationProvider.getTranslation(STATS_EXPAND_KEY));
            }

            // Toggle functionality
            Runnable toggleAction = () -> {
                if (contentContainer.isVisible()) {
                    contentContainer.setVisible(false);
                    toggleButton.setIcon(VaadinIcon.CHEVRON_RIGHT.create());
                    toggleButton
                            .getElement()
                            .setAttribute(TITLE_ATTRIBUTE, translationProvider.getTranslation(STATS_EXPAND_KEY));
                } else {
                    contentContainer.setVisible(true);
                    toggleButton.setIcon(VaadinIcon.CHEVRON_DOWN.create());
                    toggleButton
                            .getElement()
                            .setAttribute(TITLE_ATTRIBUTE, translationProvider.getTranslation(STATS_COLLAPSE_KEY));
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
}
