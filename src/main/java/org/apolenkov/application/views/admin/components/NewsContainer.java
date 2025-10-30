package org.apolenkov.application.views.admin.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import org.apolenkov.application.model.News;
import org.apolenkov.application.views.admin.constants.AdminConstants;

/**
 * Reusable container component for news management views.
 * Provides consistent layout, styling, and structure for news-related content
 * including title, toolbar, and news list components.
 */
public final class NewsContainer extends Composite<VerticalLayout> {

    // UI Components
    private final H2 title;
    private final NewsToolbar toolbar;
    private final NewsList newsList;

    /**
     * Creates a new NewsContainer.
     *
     * @param searchDebounceMs debouncing timeout for search field
     */
    public NewsContainer(final int searchDebounceMs) {
        this.title = new H2();
        this.toolbar = new NewsToolbar(searchDebounceMs);
        this.newsList = new NewsList();
    }

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout container = new VerticalLayout();
        container.setSpacing(true);
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        container.setWidthFull();
        container.addClassName(AdminConstants.CONTAINER_MD_CLASS);
        container.addClassName(AdminConstants.ADMIN_CONTENT_SECTION_CLASS);
        container.addClassName(AdminConstants.SURFACE_PANEL_CLASS);

        // Initialize title content
        title.setText(getTranslation(AdminConstants.ADMIN_CONTENT_PAGE_TITLE_KEY));
        title.addClassName(AdminConstants.ADMIN_CONTENT_VIEW_TITLE_CLASS);

        container.add(title, toolbar, newsList);
        return container;
    }

    /**
     * Gets the toolbar component.
     *
     * @return the NewsToolbar component
     */
    public NewsToolbar getToolbar() {
        return toolbar;
    }

    /**
     * Gets the news list component.
     *
     * @return the NewsList component
     */
    public NewsList getNewsList() {
        return newsList;
    }

    /**
     * Refreshes the news list with new data.
     *
     * @param news the list of news to display
     */
    public void refreshNews(final List<News> news) {
        newsList.refreshNews(news);
    }
}
