package org.apolenkov.application.views.presentation.pages;

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
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.model.News;
import org.apolenkov.application.service.NewsService;
import org.apolenkov.application.views.presentation.layouts.PublicLayout;
import org.apolenkov.application.views.shared.base.BaseView;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.LayoutHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Administrative interface for managing news articles.
 */
@Route(value = RouteConstants.ADMIN_NEWS_ROUTE, layout = PublicLayout.class)
@RouteAlias(value = RouteConstants.ADMIN_CONTENT_ROUTE, layout = PublicLayout.class)
@RolesAllowed(SecurityConstants.ROLE_ADMIN)
public class AdminNewsView extends BaseView {

    private static final String COLOR_STYLE = "color";
    private static final String FONT_SIZE_STYLE = "font-size";
    private static final String LUMO_FONT_SIZE_S = "var(--lumo-font-size-s)";

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
    private void init() {
        setPadding(false);
        setSpacing(false);
        addClassName("admin-content-view");

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(Alignment.CENTER);
        content.addClassName("admin-content-view__content");

        H2 title = new H2(getTranslation("admin.content.page.title"));
        title.addClassName("admin-content-view__title");

        TextField search = new TextField();
        search.setPlaceholder(getTranslation("admin.content.search.placeholder"));
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.EAGER);
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.addValueChangeListener(e -> refreshNews(e.getValue()));

        Button addNewsBtn = ButtonHelper.createButton(
                getTranslation("common.add"), VaadinIcon.PLUS, e -> showNewsDialog(null), ButtonVariant.LUMO_PRIMARY);
        addNewsBtn.setText(getTranslation("admin.news.add"));

        HorizontalLayout toolbar = LayoutHelper.createSearchRow(search, addNewsBtn);
        toolbar.addClassName("admin-content-toolbar");

        newsList = new VerticalLayout();
        newsList.setPadding(false);
        newsList.setSpacing(true);
        newsList.setWidthFull();
        newsList.setAlignItems(Alignment.CENTER);

        VerticalLayout newsContainer = new VerticalLayout();
        newsContainer.setSpacing(true);
        newsContainer.setAlignItems(Alignment.CENTER);
        newsContainer.setWidthFull();
        newsContainer.addClassName("container-md");
        newsContainer.addClassName("admin-content-section");
        newsContainer.addClassName("surface-panel");

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
        dialog.addClassName("dialog-md");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);

        H3 dialogTitle = new H3(news == null ? getTranslation("admin.news.add") : getTranslation("dialog.edit"));
        content.add(dialogTitle);

        TextField titleField = new TextField(getTranslation("admin.news.title"));
        titleField.setWidthFull();
        if (news != null) {
            titleField.setValue(news.getTitle());
        }

        TextArea contentField = new TextArea(getTranslation("admin.news.content"));
        contentField.setWidthFull();
        contentField.addClassName("admin-news__content-area");
        if (news != null) {
            contentField.setValue(news.getContent());
        }

        TextField authorField = new TextField(getTranslation("admin.news.author"));
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
        } catch (Exception e) {
            NotificationHelper.showError(getTranslation("admin.news.error.create", e.getMessage()));
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
        } catch (Exception e) {
            NotificationHelper.showError(getTranslation("admin.news.error.update", e.getMessage()));
        }
    }

    /**
     * Deletes news article after user confirmation.
     *
     * @param news the news article to delete
     */
    private void deleteNews(final News news) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.addClassName("dialog-sm");

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3(getTranslation("admin.news.confirm.delete.title")));

        // Create message using native Vaadin DSL components
        Div messageContainer = new Div();
        messageContainer.addClassName("text-center");
        messageContainer.setWidthFull();

        // Use native Vaadin components instead of HTML manipulation
        Span prefixSpan = new Span(getTranslation("admin.news.confirm.delete.prefix") + " ");
        Span titleSpan = new Span(news.getTitle());
        titleSpan.getElement().getStyle().set("font-weight", "bold");
        Span suffixSpan = new Span(getTranslation("admin.news.confirm.delete.suffix"));

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
                NotificationHelper.showSuccess(getTranslation("admin.news.deleted"));
            } catch (Exception ex) {
                NotificationHelper.showError(getTranslation("admin.news.error.delete", ex.getMessage()));
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
            Span empty = new Span(getTranslation("admin.content.search.noResults"));
            empty.addClassName("admin-content-empty-message");
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
        card.setMaxWidth("700px");
        card.addClassName("news-card");

        // Title
        H3 title = new H3(news.getTitle());
        title.addClassName("news-card__title");
        title.getStyle().set("margin", "0");

        // Content preview (first 150 characters)
        String contentPreview = news.getContent();
        if (contentPreview.length() > 150) {
            contentPreview = contentPreview.substring(0, 150) + "...";
        }
        Span content = new Span(contentPreview);
        content.addClassName("news-card__content");
        content.getStyle().set(COLOR_STYLE, "var(--lumo-secondary-text-color)");
        content.getStyle().set(FONT_SIZE_STYLE, LUMO_FONT_SIZE_S);
        content.getStyle().set("line-height", "1.4");

        // Author and date
        HorizontalLayout metaInfo = new HorizontalLayout();
        metaInfo.setSpacing(true);
        metaInfo.setAlignItems(Alignment.CENTER);

        Span author = new Span(getTranslation("admin.news.author") + ": " + news.getAuthor());
        author.addClassName("news-card__author");
        author.getStyle().set(COLOR_STYLE, "var(--lumo-secondary-text-color)");
        author.getStyle().set(FONT_SIZE_STYLE, LUMO_FONT_SIZE_S);

        Span createdAt = new Span(news.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        createdAt.addClassName("news-card__date");
        createdAt.getStyle().set(COLOR_STYLE, "var(--lumo-tertiary-text-color)");
        createdAt.getStyle().set(FONT_SIZE_STYLE, LUMO_FONT_SIZE_S);

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
        return getTranslation("admin.content.page.title");
    }
}
