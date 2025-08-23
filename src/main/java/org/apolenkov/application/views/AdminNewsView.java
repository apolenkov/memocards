package org.apolenkov.application.views;

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
import jakarta.annotation.security.RolesAllowed;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apolenkov.application.model.News;
import org.apolenkov.application.service.NewsService;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.DialogHelper;
import org.apolenkov.application.views.utils.GridHelper;
import org.apolenkov.application.views.utils.LayoutHelper;
import org.apolenkov.application.views.utils.TextHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Administrative view for managing news content in the application.
 *
 * <p>Provides CRUD interface for news articles with validation and security.</p>
 */
@Route(value = "admin/news", layout = PublicLayout.class)
@RouteAlias(value = "admin/content", layout = PublicLayout.class)
@RolesAllowed("ROLE_ADMIN")
public class AdminNewsView extends VerticalLayout implements HasDynamicTitle {

    private final transient NewsService newsService;
    private final transient ListDataProvider<News> dataProvider;
    private final transient List<News> newsList;

    /**
     * Constructs a new AdminNewsView with the required service dependency.
     *
     * <p>This constructor initializes the administrative interface for news management.
     * It sets up the layout, creates the news grid, and configures all interactive
     * elements including buttons, forms, and data display.</p>
     *
     * <p>The initialization process includes:</p>
     * <ul>
     *   <li><strong>Layout Setup:</strong> Configures padding, spacing, and CSS classes</li>
     *   <li><strong>Page Title:</strong> Creates localized page heading</li>
     *   <li><strong>Add Button:</strong> Button for creating new news articles</li>
     *   <li><strong>News Grid:</strong> Data grid with sortable columns and actions</li>
     *   <li><strong>Data Provider:</strong> List-based data provider for the grid</li>
     * </ul>
     *
     * <p><strong>Grid Configuration:</strong></p>
     * <ul>
     *   <li>Title, content, author, creation date, and update date columns</li>
     *   <li>Custom renderers for date formatting</li>
     *   <li>Action buttons for edit and delete operations</li>
     *   <li>Responsive column sizing and layout</li>
     * </ul>
     *
     * @param newsService the service for news operations
     * @throws IllegalArgumentException if newsService is null
     */
    public AdminNewsView(NewsService newsService) {
        if (newsService == null) {
            throw new IllegalArgumentException("NewsService cannot be null");
        }

        this.newsService = newsService;
        this.newsList = new ArrayList<>();

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
        newsGrid.setColumns("title", "content", "author", "createdAt", "updatedAt");
        GridHelper.addTextColumn(newsGrid, getTranslation("admin.news.title"), News::getTitle, 2);
        GridHelper.addTextColumn(newsGrid, getTranslation("admin.news.content"), News::getContent, 3);
        GridHelper.addTextColumn(newsGrid, getTranslation("admin.news.author"), News::getAuthor, 2);
        newsGrid.getColumnByKey("createdAt").setHeader(getTranslation("admin.news.createdAt"));
        newsGrid.getColumnByKey("updatedAt").setHeader(getTranslation("admin.news.updatedAt"));
        newsGrid.getColumnByKey("createdAt")
                .setRenderer(new com.vaadin.flow.data.renderer.LocalDateTimeRenderer<>(
                        News::getCreatedAt, "dd.MM.yyyy HH:mm"));
        newsGrid.getColumnByKey("updatedAt").setRenderer(new com.vaadin.flow.data.renderer.ComponentRenderer<>(news -> {
            if (news.getUpdatedAt() != null) {
                return new com.vaadin.flow.component.html.Span(
                        news.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            }
            return new com.vaadin.flow.component.html.Span(getTranslation("common.emDash"));
        }));

        // widths controlled via theme classes, allow auto sizing
        newsGrid.addComponentColumn(news -> {
                    HorizontalLayout actions = LayoutHelper.createButtonRow(
                            ButtonHelper.createEditButton(e -> showNewsDialog(news)),
                            ButtonHelper.createDeleteButton(e -> deleteNews(news)));
                    return actions;
                })
                .setHeader(getTranslation("admin.users.actions"))
                .setFlexGrow(0);

        dataProvider = new ListDataProvider<>(newsList);
        newsGrid.setDataProvider(dataProvider);

        add(newsGrid);
        newsGrid.setSizeFull();

        refreshNews();
    }

    /**
     * Displays a dialog for creating or editing news articles.
     *
     * <p>This method creates a modal dialog that allows administrators to input
     * or modify news article details. The dialog adapts its behavior based on
     * whether it's being used for creation (null news) or editing (existing news).</p>
     *
     * <p>The dialog includes:</p>
     * <ul>
     *   <li><strong>Dynamic Title:</strong> Changes based on create/edit mode</li>
     *   <li><strong>Form Fields:</strong> Title, content, and author input fields</li>
     *   <li><strong>Auto-population:</strong> Author field filled with current user</li>
     *   <li><strong>Validation:</strong> Client-side validation for required fields</li>
     *   <li><strong>Action Buttons:</strong> Save and cancel buttons with proper styling</li>
     * </ul>
     *
     * <p><strong>Form Behavior:</strong></p>
     * <ul>
     *   <li>For new news: Empty form with current user as author</li>
     *   <li>For existing news: Pre-populated with current values</li>
     *   <li>Validation prevents saving with empty title or content</li>
     *   <li>Successful save closes dialog and refreshes data</li>
     * </ul>
     *
     * @param news the news item to edit, or null for creating new news
     * @see Dialog
     * @see TextField
     * @see TextArea
     * @see ButtonHelper
     */
    private void showNewsDialog(News news) {
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

        HorizontalLayout buttons = new HorizontalLayout();
        Button saveBtn = ButtonHelper.createPrimaryButton(getTranslation("dialog.save"), e -> {
            String t =
                    titleField.getValue() == null ? "" : titleField.getValue().trim();
            String c = contentField.getValue() == null
                    ? ""
                    : contentField.getValue().trim();
            if (t.isEmpty()) {
                titleField.setErrorMessage(getTranslation("admin.news.validation.titleRequired"));
                titleField.setInvalid(true);
                return;
            }
            if (c.isEmpty()) {
                contentField.setErrorMessage(getTranslation("admin.news.validation.contentRequired"));
                contentField.setInvalid(true);
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
     * Creates a new news article.
     *
     * <p>This method delegates the creation of a new news article to the news service.
     * It handles any exceptions that occur during the creation process and displays
     * appropriate error messages to the user.</p>
     *
     * <p><strong>Error Handling:</strong></p>
     * <ul>
     *   <li>Catches and logs any exceptions from the service layer</li>
     *   <li>Displays user-friendly error messages</li>
     *   <li>Uses localized error message keys</li>
     *   <li>Includes exception details for debugging</li>
     * </ul>
     *
     * @param title the title of the news article
     * @param content the content/body of the news article
     * @param author the author of the news article
     * @throws RuntimeException if the news service encounters an error
     * @see NewsService#createNews(String, String, String)
     * @see NotificationHelper#showError(String)
     */
    private void createNews(String title, String content, String author) {
        try {
            newsService.createNews(title, content, author);
        } catch (Exception e) {
            org.apolenkov.application.views.utils.NotificationHelper.showError(
                    getTranslation("admin.news.error.create", e.getMessage()));
        }
    }

    /**
     * Updates an existing news article.
     *
     * <p>This method delegates the update of an existing news article to the news service.
     * It handles any exceptions that occur during the update process and displays
     * appropriate error messages to the user.</p>
     *
     * <p><strong>Error Handling:</strong></p>
     * <ul>
     *   <li>Catches and logs any exceptions from the service layer</li>
     *   <li>Displays user-friendly error messages</li>
     *   <li>Uses localized error message keys</li>
     *   <li>Includes exception details for debugging</li>
     * </ul>
     *
     * @param id the unique identifier of the news article to update
     * @param title the new title for the news article
     * @param content the new content for the news article
     * @param author the new author for the news article
     * @throws RuntimeException if the news service encounters an error
     * @see NewsService#updateNews(Long, String, String, String)
     * @see NotificationHelper#showError(String)
     */
    private void updateNews(Long id, String title, String content, String author) {
        try {
            newsService.updateNews(id, title, content, author);
        } catch (Exception e) {
            org.apolenkov.application.views.utils.NotificationHelper.showError(
                    getTranslation("admin.news.error.update", e.getMessage()));
        }
    }

    /**
     * Deletes a news article with confirmation.
     *
     * <p>This method displays a confirmation dialog before deleting a news article.
     * It includes the article title in the confirmation message to ensure the
     * administrator is deleting the correct item.</p>
     *
     * <p><strong>Confirmation Process:</strong></p>
     * <ul>
     *   <li>Shows confirmation dialog with article title</li>
     *   <li>Requires explicit user confirmation</li>
     *   <li>Displays success/error notifications</li>
     *   <li>Automatically refreshes the news list after deletion</li>
     * </ul>
     *
     * <p><strong>Security Features:</strong></p>
     * <ul>
     *   <li>HTML escaping of article title to prevent XSS</li>
     *   <li>Confirmation required before deletion</li>
     *   <li>Proper error handling and user feedback</li>
     * </ul>
     *
     * @param news the news article to delete
     * @see DialogHelper#createConfirmationDialog(String, String, Runnable, Runnable)
     * @see NotificationHelper#showSuccess(String)
     * @see NotificationHelper#showError(String)
     * @see org.apache.commons.text.StringEscapeUtils#escapeHtml4(String)
     */
    private void deleteNews(News news) {
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
                        org.apolenkov.application.views.utils.NotificationHelper.showSuccess(
                                getTranslation("admin.news.deleted"));
                    } catch (Exception ex) {
                        org.apolenkov.application.views.utils.NotificationHelper.showError(
                                getTranslation("admin.news.error.delete", ex.getMessage()));
                    }
                },
                null);
        confirmDialog.open();
    }

    /**
     * Refreshes the news data displayed in the grid.
     *
     * <p>This method updates the local news list with fresh data from the service
     * and refreshes the data provider to ensure the grid displays the current state.
     * It's called after create, update, and delete operations to maintain data consistency.</p>
     *
     * <p><strong>Data Flow:</strong></p>
     * <ul>
     *   <li>Clears the local news list</li>
     *   <li>Fetches fresh data from the news service</li>
     *   <li>Updates the data provider</li>
     *   <li>Triggers grid refresh to display changes</li>
     * </ul>
     *
     * @see NewsService#getAllNews()
     * @see ListDataProvider#refreshAll()
     */
    private void refreshNews() {
        newsList.clear();
        newsList.addAll(newsService.getAllNews());
        dataProvider.refreshAll();
    }

    /**
     * Gets the page title for this view.
     *
     * <p>This method implements the {@link HasDynamicTitle} interface to provide
     * a dynamic page title that reflects the current view's purpose. The title
     * is retrieved from the internationalization system to ensure proper
     * localization.</p>
     *
     * @return the localized page title for the admin news view
     * @see HasDynamicTitle#getPageTitle()
     * @see #getTranslation(String)
     */
    @Override
    public String getPageTitle() {
        return getTranslation("admin.content.page.title");
    }
}
