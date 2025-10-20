package org.apolenkov.application.views.admin.pages;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;
import java.util.Locale;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.config.ui.UIConfig;
import org.apolenkov.application.model.News;
import org.apolenkov.application.service.news.NewsService;
import org.apolenkov.application.views.admin.components.AdminNewsDeleteDialog;
import org.apolenkov.application.views.admin.components.AdminNewsDialog;
import org.apolenkov.application.views.admin.components.NewsContainer;
import org.apolenkov.application.views.admin.constants.AdminConstants;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.shared.base.BaseView;
import org.apolenkov.application.views.shared.utils.NotificationHelper;

/**
 * Administrative interface for managing news articles.
 * This view provides functionality for listing all news articles,
 * searching through them, and creating/editing/deleting news.
 */
@Route(value = RouteConstants.ADMIN_NEWS_ROUTE, layout = PublicLayout.class)
@RouteAlias(value = RouteConstants.ADMIN_CONTENT_ROUTE, layout = PublicLayout.class)
@RolesAllowed(SecurityConstants.ROLE_ADMIN)
public class AdminNewsView extends BaseView implements AfterNavigationObserver {

    // Dependencies
    private final transient NewsService newsService;
    private final transient UIConfig uiConfig;

    // UI Components
    private NewsContainer newsContainer;

    // Event Registrations
    private Registration searchListenerRegistration;
    private Registration addClickListenerRegistration;

    /**
     * Creates news management interface.
     *
     * @param service the service for news operations
     * @param uiConfigParam UI configuration settings
     * @throws IllegalArgumentException if service is null
     */
    public AdminNewsView(final NewsService service, final UIConfig uiConfigParam) {
        if (service == null) {
            throw new IllegalArgumentException("NewsService cannot be null");
        }

        this.newsService = service;
        this.uiConfig = uiConfigParam;
    }

    /**
     * Initializes the view components after dependency injection is complete.
     * Sets up layout, creates components, configures event listeners, and loads data.
     */
    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        // Configure main view layout
        setPadding(false);
        setSpacing(false);
        addClassName(AdminConstants.ADMIN_CONTENT_VIEW_CLASS);

        // Create content layout
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.addClassName(AdminConstants.ADMIN_CONTENT_VIEW_CONTENT_CLASS);
        add(content);

        // Create and add news container
        newsContainer = new NewsContainer(uiConfig.search().debounceMs());

        // Set up callbacks for news actions
        newsContainer.getNewsList().setEditCallback(this::showNewsDialog);
        newsContainer.getNewsList().setDeleteCallback(this::deleteNews);

        content.add(newsContainer);
    }

    /**
     * Called after navigation to this view is complete.
     * Sets up event listeners and loads initial data.
     * This method is called ONCE per navigation - no flag needed.
     *
     * @param event the after navigation event
     */
    @Override
    public void afterNavigation(final AfterNavigationEvent event) {
        if (searchListenerRegistration == null) {
            searchListenerRegistration = newsContainer.getToolbar().addSearchListener(this::refreshNews);
        }
        if (addClickListenerRegistration == null) {
            addClickListenerRegistration = newsContainer.getToolbar().addAddClickListener(e -> showNewsDialog(null));
        }
        refreshNews("");
    }

    /**
     * Shows dialog for creating or editing news articles.
     *
     * @param news the news item to edit, or null for creating new news
     */
    private void showNewsDialog(final News news) {
        AdminNewsDialog dialog = new AdminNewsDialog(news, formData -> {
            if (formData.id() == null) {
                createNews(formData.title(), formData.content(), formData.author());
            } else {
                updateNews(formData.id(), formData.title(), formData.content(), formData.author());
            }
            refreshNews("");
        });
        dialog.open();
    }

    /**
     * Creates new news article.
     *
     * @param title the title of the news article
     * @param content the content of the news article
     * @param author the author of the news article
     */
    private void createNews(final String title, final String content, final String author) {
        try {
            newsService.createNews(title, content, author);
            NotificationHelper.showSuccess(getTranslation(AdminConstants.ADMIN_NEWS_CREATED_KEY));
        } catch (Exception e) {
            NotificationHelper.showError(getTranslation(AdminConstants.ADMIN_NEWS_ERROR_CREATE_KEY, e.getMessage()));
        }
    }

    /**
     * Updates existing news article.
     *
     * @param id the identifier of the news article to update
     * @param title the new title for the news article
     * @param content the new content for the news article
     * @param author the new author for the news article
     */
    private void updateNews(final long id, final String title, final String content, final String author) {
        try {
            newsService.updateNews(id, title, content, author);
            NotificationHelper.showSuccess(getTranslation(AdminConstants.ADMIN_NEWS_UPDATED_KEY));
        } catch (Exception e) {
            NotificationHelper.showError(getTranslation(AdminConstants.ADMIN_NEWS_ERROR_UPDATE_KEY, e.getMessage()));
        }
    }

    /**
     * Deletes news article after user confirmation.
     *
     * @param news the news article to delete
     */
    private void deleteNews(final News news) {
        AdminNewsDeleteDialog dialog = new AdminNewsDeleteDialog(news, () -> {
            try {
                newsService.deleteNews(news.getId());
                refreshNews("");
                NotificationHelper.showSuccess(getTranslation(AdminConstants.ADMIN_NEWS_DELETED_KEY));
            } catch (Exception ex) {
                NotificationHelper.showError(
                        getTranslation(AdminConstants.ADMIN_NEWS_ERROR_DELETE_KEY, ex.getMessage()));
            }
        });
        dialog.open();
    }

    /**
     * Refreshes the news list display based on the search query.
     * This method updates the news list by filtering news based on the
     * provided search query. It handles empty results gracefully by displaying
     * an appropriate message when no news match the search criteria.
     *
     * @param query the search query to filter news by title, content, or author
     */
    private void refreshNews(final String query) {
        final List<News> allNews = newsService.getAllNews();
        final List<News> filteredNews = filterNews(allNews, query);
        newsContainer.refreshNews(filteredNews);
    }

    /**
     * Filters news based on search query.
     *
     * @param allNews all available news
     * @param query search query
     * @return filtered news list
     */
    private List<News> filterNews(final List<News> allNews, final String query) {
        if (query == null || query.isEmpty()) {
            return allNews;
        }

        String lowerQuery = query.toLowerCase(Locale.ROOT);
        return allNews.stream().filter(news -> matchesQuery(news, lowerQuery)).toList();
    }

    /**
     * Checks if news matches search query.
     *
     * @param news the news to check
     * @param lowerQuery lowercase search query
     * @return true if matches
     */
    private boolean matchesQuery(final News news, final String lowerQuery) {
        return news.getTitle().toLowerCase(Locale.ROOT).contains(lowerQuery)
                || news.getContent().toLowerCase(Locale.ROOT).contains(lowerQuery)
                || news.getAuthor().toLowerCase(Locale.ROOT).contains(lowerQuery);
    }

    /**
     * Returns localized page title.
     *
     * @return the localized page title
     */
    @Override
    public String getPageTitle() {
        return getTranslation(AdminConstants.ADMIN_CONTENT_PAGE_TITLE_KEY);
    }

    /**
     * Cleans up event listeners when the component is detached.
     * Prevents memory leaks by removing event listener registrations.
     *
     * @param detachEvent the detach event
     */
    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        if (searchListenerRegistration != null) {
            searchListenerRegistration.remove();
            searchListenerRegistration = null;
        }

        if (addClickListenerRegistration != null) {
            addClickListenerRegistration.remove();
            addClickListenerRegistration = null;
        }
    }
}
