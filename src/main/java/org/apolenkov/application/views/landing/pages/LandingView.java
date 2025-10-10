package org.apolenkov.application.views.landing.pages;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.service.NewsService;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.landing.components.HeroSection;
import org.apolenkov.application.views.landing.components.NewsSection;
import org.apolenkov.application.views.landing.constants.LandingConstants;
import org.apolenkov.application.views.shared.base.BaseView;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Landing page view with hero section, news updates, and dynamic navigation based on authentication status.
 */
@Route(value = RouteConstants.HOME_ROUTE, layout = PublicLayout.class)
@AnonymousAllowed
public class LandingView extends BaseView {

    // Dependencies
    private final transient NewsService newsService;

    /**
     * Creates a new LandingView with news service dependency.
     *
     * @param service service for retrieving and displaying news content
     */
    public LandingView(final NewsService service) {
        this.newsService = service;
    }

    /**
     * Initializes the view components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        setupLayout();
        createAndAddSections();
    }

    /**
     * Sets up the main layout properties.
     */
    private void setupLayout() {
        setSpacing(true);
        setPadding(true);
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    /**
     * Creates and adds the hero and news sections to the layout.
     */
    private void createAndAddSections() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        HeroSection heroSection = new HeroSection(auth);
        NewsSection newsSection = new NewsSection(newsService);

        add(heroSection, newsSection);
    }

    /**
     * Returns the page title for the landing view.
     * Provides a localized page title that appears in the browser tab
     * and navigation history.
     *
     * @return the localized page title
     */
    @Override
    public String getPageTitle() {
        return getTranslation(LandingConstants.APP_TITLE_KEY);
    }
}
