package org.apolenkov.application.views.admin.pages;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.config.ui.UIConfig;
import org.apolenkov.application.model.News;
import org.apolenkov.application.service.news.NewsService;
import org.apolenkov.application.views.admin.components.AdminNewsDeleteDialog;
import org.apolenkov.application.views.admin.components.AdminNewsDialog;
import org.apolenkov.application.views.admin.constants.AdminConstants;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.shared.base.BaseView;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.LayoutHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;

/**
 * Administrative interface for managing news articles.
 */
@Route(value = RouteConstants.ADMIN_NEWS_ROUTE, layout = PublicLayout.class)
@RouteAlias(value = RouteConstants.ADMIN_CONTENT_ROUTE, layout = PublicLayout.class)
@RolesAllowed(SecurityConstants.ROLE_ADMIN)
public class AdminNewsView extends BaseView implements AfterNavigationObserver {

    // ==================== Fields ====================

    private final transient NewsService newsService;
    private final transient UIConfig uiConfig;
    private VerticalLayout newsList;

    // Event Registrations
    private Registration searchListenerRegistration;

    // ==================== Constructor ====================

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

    // ==================== Lifecycle & Initialization ====================

    /**
     * Initializes the view components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        setPadding(false);
        setSpacing(false);
        addClassName(AdminConstants.ADMIN_CONTENT_VIEW_CLASS);

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(Alignment.CENTER);
        content.addClassName(AdminConstants.ADMIN_CONTENT_VIEW_CONTENT_CLASS);

        H2 title = new H2(getTranslation(AdminConstants.ADMIN_CONTENT_PAGE_TITLE_KEY));
        title.addClassName(AdminConstants.ADMIN_CONTENT_VIEW_TITLE_CLASS);

        TextField search = new TextField();
        search.setPlaceholder(getTranslation(AdminConstants.ADMIN_CONTENT_SEARCH_PLACEHOLDER_KEY));
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.TIMEOUT);
        search.setValueChangeTimeout(uiConfig.search().debounceMs());
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.addClassName(AdminConstants.ADMIN_CONTENT_TOOLBAR_SEARCH_CLASS);
        searchListenerRegistration = search.addValueChangeListener(e -> refreshNews(e.getValue()));

        Button addNewsBtn = ButtonHelper.createButton(
                getTranslation(AdminConstants.ADMIN_NEWS_ADD_KEY),
                VaadinIcon.PLUS,
                e -> showNewsDialog(null),
                ButtonVariant.LUMO_PRIMARY);
        addNewsBtn.addClassName(AdminConstants.ADMIN_CONTENT_TOOLBAR_ADD_BUTTON_CLASS);
        // Add tooltip for mobile users
        addNewsBtn.getElement().setAttribute("title", getTranslation(AdminConstants.ADMIN_NEWS_ADD_KEY));

        HorizontalLayout toolbar = LayoutHelper.createSearchRow(search, addNewsBtn);
        toolbar.addClassName(AdminConstants.ADMIN_CONTENT_TOOLBAR_CLASS);

        newsList = new VerticalLayout();
        newsList.setPadding(false);
        newsList.setSpacing(true);
        newsList.setWidthFull();
        newsList.setAlignItems(Alignment.CENTER);

        VerticalLayout newsContainer = new VerticalLayout();
        newsContainer.setSpacing(true);
        newsContainer.setAlignItems(Alignment.CENTER);
        newsContainer.setWidthFull();
        newsContainer.addClassName(AdminConstants.CONTAINER_MD_CLASS);
        newsContainer.addClassName(AdminConstants.ADMIN_CONTENT_SECTION_CLASS);
        newsContainer.addClassName(AdminConstants.SURFACE_PANEL_CLASS);

        newsContainer.add(title, toolbar, newsList);

        content.add(newsContainer);
        add(content);
    }

    /**
     * Called after navigation to this view is complete.
     * Loads news data and displays it.
     * This method is called ONCE per navigation - no flag needed.
     *
     * @param event the after navigation event
     */
    @Override
    public void afterNavigation(final AfterNavigationEvent event) {
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
     * Refreshes news data based on search query.
     * This method updates the news list by filtering news based on the
     * provided search query. It handles empty results gracefully by displaying
     * an appropriate message when no news match the search criteria.
     *
     * @param query the search query to filter news by title or content
     */
    private void refreshNews(final String query) {
        newsList.removeAll();
        List<News> allNews = newsService.getAllNews();
        List<News> filteredNews = filterNews(allNews, query);

        if (filteredNews.isEmpty()) {
            displayEmptyState();
            return;
        }

        displayNewsCards(filteredNews);
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
     * Displays empty state when no news found.
     */
    private void displayEmptyState() {
        Span empty = new Span(getTranslation(AdminConstants.ADMIN_CONTENT_SEARCH_NO_RESULTS_KEY));
        empty.addClassName(AdminConstants.ADMIN_CONTENT_EMPTY_MESSAGE_CLASS);
        newsList.add(empty);
    }

    /**
     * Displays news cards for filtered news.
     *
     * @param filteredNews the news to display
     */
    private void displayNewsCards(final List<News> filteredNews) {
        filteredNews.forEach(news -> newsList.add(createNewsCard(news)));
    }

    /**
     * Creates a news card component similar to deck cards.
     * This method creates a card-like component for displaying news information
     * with title, content preview, author, and action buttons.
     *
     * @param news the news item to display
     * @return a vertical layout containing the news card
     */
    private VerticalLayout createNewsCard(final News news) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(true);
        card.setWidthFull();
        card.addClassName(AdminConstants.NEWS_CARD_CLASS);

        // Title
        H3 title = new H3(news.getTitle());
        title.addClassName(AdminConstants.NEWS_CARD_TITLE_CLASS);

        // Content preview (first 150 characters)
        String contentPreview = news.getContent();
        if (contentPreview.length() > AdminConstants.CONTENT_PREVIEW_LENGTH) {
            contentPreview = contentPreview.substring(0, AdminConstants.CONTENT_PREVIEW_LENGTH)
                    + AdminConstants.CONTENT_PREVIEW_SUFFIX;
        }
        Span content = new Span(contentPreview);
        content.addClassName(AdminConstants.NEWS_CARD_CONTENT_CLASS);
        content.addClassName(AdminConstants.TEXT_CONTENT_CLASS);

        // Author and date
        HorizontalLayout metaInfo = new HorizontalLayout();
        metaInfo.setSpacing(true);
        metaInfo.setAlignItems(Alignment.CENTER);

        Span author = new Span(getTranslation(AdminConstants.ADMIN_NEWS_AUTHOR_KEY)
                + CoreConstants.SEPARATOR_COLON_SPACE
                + news.getAuthor());
        author.addClassName(AdminConstants.NEWS_CARD_AUTHOR_CLASS);
        author.addClassName(AdminConstants.TEXT_MUTED_CLASS);

        Span createdAt =
                new Span(news.getCreatedAt().format(DateTimeFormatter.ofPattern(AdminConstants.DATE_TIME_PATTERN)));
        createdAt.addClassName(AdminConstants.NEWS_CARD_DATE_CLASS);
        createdAt.addClassName(AdminConstants.TEXT_MUTED_SMALL_CLASS);

        metaInfo.add(author, createdAt);

        // Action buttons
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setAlignItems(Alignment.CENTER);
        actions.addClassName(AdminConstants.NEWS_CARD_ACTIONS_CLASS);

        Button editBtn = ButtonHelper.createButton(
                getTranslation(AdminConstants.COMMON_EDIT_KEY),
                VaadinIcon.EDIT,
                e -> showNewsDialog(news),
                ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_SMALL);

        Button deleteBtn = ButtonHelper.createButton(
                getTranslation(AdminConstants.COMMON_DELETE_KEY),
                VaadinIcon.TRASH,
                e -> deleteNews(news),
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_SMALL);

        actions.add(editBtn, deleteBtn);

        card.add(title, content, metaInfo, actions);
        return card;
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
        if (searchListenerRegistration != null) {
            searchListenerRegistration.remove();
            searchListenerRegistration = null;
        }
        super.onDetach(detachEvent);
    }
}
