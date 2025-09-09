package org.apolenkov.application.views;

import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.model.News;
import org.apolenkov.application.service.NewsService;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.DialogHelper;
import org.apolenkov.application.views.utils.GridHelper;
import org.apolenkov.application.views.utils.LayoutHelper;
import org.apolenkov.application.views.utils.NotificationHelper;
import org.apolenkov.application.views.utils.TextHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Administrative interface for managing news articles.
 */
@Route(value = "admin/news", layout = PublicLayout.class)
@RouteAlias(value = "admin/content", layout = PublicLayout.class)
@RolesAllowed(SecurityConstants.ROLE_ADMIN)
public class AdminNewsView extends VerticalLayout implements HasDynamicTitle {

    private static final String COL_CREATED_AT = "createdAt";
    private static final String COL_UPDATED_AT = "updatedAt";

    private final transient NewsService newsService;
    private ListDataProvider<News> dataProvider;
    private final transient List<News> newsList;

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
        this.newsList = new ArrayList<>();
    }

    /**
     * Initializes the view components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    private void init() {
        setPadding(true);
        setSpacing(true);
        addClassName("admin-view");

        H2 title = TextHelper.createPageTitle(getTranslation("admin.content.page.title"));
        add(title);

        Button addNewsBtn = ButtonHelper.createPlusButton(e -> showNewsDialog(null));
        addNewsBtn.setText(getTranslation("admin.news.add"));
        addNewsBtn.addClassName("admin-view__add-button");
        add(addNewsBtn);

        Grid<News> newsGrid = GridHelper.createBasicGrid(News.class);
        newsGrid.setColumns("title", "content", "author", COL_CREATED_AT, COL_UPDATED_AT);
        GridHelper.addTextColumn(newsGrid, getTranslation("admin.news.title"), News::getTitle, 2);
        GridHelper.addTextColumn(newsGrid, getTranslation("admin.news.content"), News::getContent, 3);
        GridHelper.addTextColumn(newsGrid, getTranslation("admin.news.author"), News::getAuthor, 2);
        newsGrid.getColumnByKey(COL_CREATED_AT).setHeader(getTranslation("admin.news.createdAt"));
        newsGrid.getColumnByKey(COL_UPDATED_AT).setHeader(getTranslation("admin.news.updatedAt"));
        newsGrid.getColumnByKey(COL_CREATED_AT)
                .setRenderer(new com.vaadin.flow.data.renderer.LocalDateTimeRenderer<>(
                        News::getCreatedAt, "dd.MM.yyyy HH:mm"));
        newsGrid.getColumnByKey(COL_UPDATED_AT)
                .setRenderer(new com.vaadin.flow.data.renderer.ComponentRenderer<>(news -> {
                    if (news.getUpdatedAt() != null) {
                        return new com.vaadin.flow.component.html.Span(
                                news.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                    }
                    return new com.vaadin.flow.component.html.Span(getTranslation("common.emDash"));
                }));

        // widths controlled via theme classes, allow auto sizing
        newsGrid.addComponentColumn(news -> LayoutHelper.createButtonRow(
                        ButtonHelper.createEditButton(e -> showNewsDialog(news)),
                        ButtonHelper.createDeleteButton(e -> deleteNews(news))))
                .setHeader(getTranslation("admin.users.actions"))
                .setFlexGrow(0);

        dataProvider = new ListDataProvider<>(newsList);
        newsGrid.setDataProvider(dataProvider);

        add(newsGrid);
        newsGrid.setSizeFull();

        refreshNews();
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

        H3 dialogTitle = TextHelper.createSectionTitle(
                news == null ? getTranslation("admin.news.add") : getTranslation("dialog.edit"));
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
            refreshNews();
        });

        Button cancelBtn = ButtonHelper.createTertiaryButton(getTranslation("common.cancel"), e -> dialog.close());
        HorizontalLayout buttonRow = LayoutHelper.createButtonRow(saveBtn, cancelBtn);
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
        String message = getTranslation("admin.news.confirm.delete.prefix")
                + " <b>" + org.apache.commons.text.StringEscapeUtils.escapeHtml4(news.getTitle())
                + "</b>"
                + getTranslation("admin.news.confirm.delete.suffix");

        Dialog confirmDialog = DialogHelper.createConfirmationDialog(
                getTranslation("admin.news.confirm.delete.title"),
                message,
                () -> {
                    try {
                        newsService.deleteNews(news.getId());
                        refreshNews();
                        NotificationHelper.showSuccess(getTranslation("admin.news.deleted"));
                    } catch (Exception ex) {
                        NotificationHelper.showError(getTranslation("admin.news.error.delete", ex.getMessage()));
                    }
                },
                null);
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
     * Refreshes news data in the grid.
     */
    private void refreshNews() {
        newsList.clear();
        newsList.addAll(newsService.getAllNews());
        dataProvider.refreshAll();
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
