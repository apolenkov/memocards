package org.apolenkov.application.views.stats.pages;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.shared.base.BaseView;
import org.apolenkov.application.views.stats.components.StatsCardFactory;
import org.apolenkov.application.views.stats.components.StatsConstants;
import org.apolenkov.application.views.stats.components.StatsDataLoader;
import org.apolenkov.application.views.stats.components.StatsSectionFactory;

/**
 * View for displaying user statistics and analytics.
 * This view provides comprehensive statistics about user's learning progress,
 * including today's stats, overall statistics, and detailed deck-specific metrics.
 * The statistics are presented in collapsible sections for better organization.
 */
@Route(value = RouteConstants.STATS_ROUTE, layout = PublicLayout.class)
@RolesAllowed({SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN})
public class StatsView extends BaseView {

    private final transient StatsDataLoader dataLoader;
    private final transient StatsCardFactory cardFactory;
    private final transient StatsSectionFactory sectionFactory;

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
        this.dataLoader = new StatsDataLoader(deckUseCaseParam, userUseCaseParam, statsServiceParam);
        this.cardFactory = new StatsCardFactory();
        this.sectionFactory = new StatsSectionFactory();
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
        pageSection.addClassName(StatsConstants.SURFACE_PANEL_CLASS);

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
        // Set up translation provider for components
        dataLoader.setTranslationProvider(this::getTranslation);
        cardFactory.setTranslationProvider(this::getTranslation);
        sectionFactory.setTranslationProvider(this::getTranslation);
        sectionFactory.setCardFactory(cardFactory);

        // Load data using the data loader
        StatsDataLoader.StatsData statsData = dataLoader.loadStatsData(pageSection);

        // Add sections using loaded data
        pageSection.add(sectionFactory.createTodayStatsSection(statsData.aggregates()));
        pageSection.add(sectionFactory.createOverallStatsSection(statsData.aggregates()));
        pageSection.add(sectionFactory.createDeckStatsSection(statsData.decks(), statsData.aggregates()));
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
