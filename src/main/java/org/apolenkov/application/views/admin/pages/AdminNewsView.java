package org.apolenkov.application.views.admin.pages;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.model.News;
import org.apolenkov.application.service.NewsService;
import org.apolenkov.application.views.admin.constants.AdminConstants;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.shared.base.BaseView;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.LayoutHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

/**
 * Administrative interface for managing news articles.
 */
@Route(value = RouteConstants.ADMIN_NEWS_ROUTE, layout = PublicLayout.class)
@RouteAlias(value = RouteConstants.ADMIN_CONTENT_ROUTE, layout = PublicLayout.class)
@RolesAllowed(SecurityConstants.ROLE_ADMIN)
public class AdminNewsView extends BaseView {

    private final transient NewsService newsService;
    private VerticalLayout newsList;

    /**
     * Creates news management interface.
     *
     * @param service the service for news operations
     * @throws IllegalArgumentException if service is null
     */
    public AdminNewsView(final NewsService service) {
        if (service == null) {
            throw new IllegalArgumentException("NewsService cannot be null");
        }

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
        search.setValueChangeMode(ValueChangeMode.EAGER);
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.addValueChangeListener(e -> refreshNews(e.getValue()));

        Button addNewsBtn = ButtonHelper.createButton(
                getTranslation("common.add"), VaadinIcon.PLUS, e -> showNewsDialog(null), ButtonVariant.LUMO_PRIMARY);
        addNewsBtn.setText(getTranslation(AdminConstants.ADMIN_NEWS_ADD_KEY));

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

        refreshNews("");
    }

    /**
     * Shows dialog for creating or editing news articles.
     *
     * @param news the news item to edit, or null for creating new news
     */
    private void showNewsDialog(final News news) {
        Dialog dialog = new Dialog();
        dialog.addClassName(AdminConstants.DIALOG_MD_CLASS);

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);

        H3 dialogTitle = new H3(
                news == null ? getTranslation(AdminConstants.ADMIN_NEWS_ADD_KEY) : getTranslation("dialog.edit"));
        content.add(dialogTitle);

        TextField titleField = new TextField(getTranslation(AdminConstants.ADMIN_NEWS_TITLE_KEY));
        titleField.setWidthFull();
        if (news != null) {
            titleField.setValue(news.getTitle());
        }

        TextArea contentField = new TextArea(getTranslation(AdminConstants.ADMIN_NEWS_CONTENT_KEY));
        contentField.setWidthFull();
        contentField.addClassName(AdminConstants.ADMIN_NEWS_CONTENT_AREA_CLASS);
        if (news != null) {
            contentField.setValue(news.getContent());
        }

        TextField authorField = new TextField(getTranslation(AdminConstants.ADMIN_NEWS_AUTHOR_KEY));
        authorField.setWidthFull();
        if (news != null) {
            authorField.setValue(news.getAuthor());
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                authorField.setValue(auth.getName());
            }
        }

        content.add(titleField, contentField, authorField);

        Button saveBtn = ButtonHelper.createPrimaryButton(getTranslation("dialog.save"), e -> {
            String t = safeTrim(titleField.getValue());
            String c = safeTrim(contentField.getValue());
            if (validateRequired(titleField, t, "admin.news.validation.titleRequired")) {
                return;
            }
            if (validateRequired(contentField, c, "admin.news.validation.contentRequired")) {
                return;
            }

            if (news == null) {
                createNews(t, c, authorField.getValue());
            } else {
                updateNews(news.getId(), t, c, authorField.getValue());
            }
            dialog.close();
            refreshNews("");
        });

        Button cancelBtn = ButtonHelper.createTertiaryButton(getTranslation("common.cancel"), e -> dialog.close());
        HorizontalLayout buttonRow = new HorizontalLayout();
        buttonRow.setSpacing(true);
        buttonRow.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonRow.add(saveBtn, cancelBtn);
        content.add(buttonRow);

        dialog.add(content);
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
        Dialog confirmDialog = new Dialog();
        confirmDialog.addClassName(AdminConstants.DIALOG_SM_CLASS);

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3(getTranslation(AdminConstants.ADMIN_NEWS_CONFIRM_DELETE_TITLE_KEY)));

        // Create message using native Vaadin DSL components
        Div messageContainer = new Div();
        messageContainer.addClassName(AdminConstants.TEXT_CENTER_CLASS);
        messageContainer.setWidthFull();

        // Use native Vaadin components instead of HTML manipulation
        Span prefixSpan = new Span(getTranslation(AdminConstants.ADMIN_NEWS_CONFIRM_DELETE_PREFIX_KEY) + " ");
        Span titleSpan = new Span(news.getTitle());
        titleSpan.addClassName(AdminConstants.ADMIN_DIALOG_CONFIRM_TITLE_CLASS);
        Span suffixSpan = new Span(getTranslation(AdminConstants.ADMIN_NEWS_CONFIRM_DELETE_SUFFIX_KEY));

        messageContainer.add(prefixSpan, titleSpan, suffixSpan);
        layout.add(messageContainer);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        Button confirmButton = ButtonHelper.createConfirmButton(getTranslation("dialog.confirm"), e -> {
            try {
                newsService.deleteNews(news.getId());
                refreshNews("");
                NotificationHelper.showSuccess(getTranslation(AdminConstants.ADMIN_NEWS_DELETED_KEY));
            } catch (Exception ex) {
                NotificationHelper.showError(
                        getTranslation(AdminConstants.ADMIN_NEWS_ERROR_DELETE_KEY, ex.getMessage()));
            }
            confirmDialog.close();
        });

        Button cancelButton =
                ButtonHelper.createCancelButton(getTranslation("dialog.cancel"), e -> confirmDialog.close());

        buttons.add(confirmButton, cancelButton);
        layout.add(buttons);
        confirmDialog.add(layout);
        confirmDialog.open();
    }

    private static String safeTrim(final String value) {
        return value == null ? "" : value.trim();
    }

    private boolean validateRequired(final HasValidation field, final String value, final String i18nKey) {
        if (value == null || value.isBlank()) {
            field.setErrorMessage(getTranslation(i18nKey));
            field.setInvalid(true);
            return true;
        }
        return false;
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

        List<News> filteredNews = allNews.stream()
                .filter(news -> query == null
                        || query.isEmpty()
                        || news.getTitle().toLowerCase().contains(query.toLowerCase())
                        || news.getContent().toLowerCase().contains(query.toLowerCase())
                        || news.getAuthor().toLowerCase().contains(query.toLowerCase()))
                .toList();

        if (filteredNews.isEmpty()) {
            Span empty = new Span(getTranslation(AdminConstants.ADMIN_CONTENT_SEARCH_NO_RESULTS_KEY));
            empty.addClassName(AdminConstants.ADMIN_CONTENT_EMPTY_MESSAGE_CLASS);
            newsList.add(empty);
            return;
        }

        // Create news cards similar to deck cards
        filteredNews.forEach(news -> {
            VerticalLayout newsCard = createNewsCard(news);
            newsList.add(newsCard);
        });
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

        Span author = new Span(getTranslation(AdminConstants.ADMIN_NEWS_AUTHOR_KEY) + ": " + news.getAuthor());
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

        Button editBtn = ButtonHelper.createButton(
                getTranslation("common.edit"),
                VaadinIcon.EDIT,
                e -> showNewsDialog(news),
                ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_SMALL);

        Button deleteBtn = ButtonHelper.createButton(
                getTranslation("common.delete"),
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
}
