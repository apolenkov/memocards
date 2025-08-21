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

@Route(value = "admin/news", layout = PublicLayout.class)
@RouteAlias(value = "admin/content", layout = PublicLayout.class)
@RolesAllowed("ROLE_ADMIN")
public class AdminNewsView extends VerticalLayout implements HasDynamicTitle {

    private final transient NewsService newsService;
    private final transient ListDataProvider<News> dataProvider;
    private final transient List<News> newsList;

    public AdminNewsView(NewsService newsService) {
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

        newsGrid.getColumnByKey("title").setWidth("420px");
        newsGrid.getColumnByKey("content").setWidth("420px");
        newsGrid.getColumnByKey("author").setWidth("420px");
        newsGrid.getColumnByKey("createdAt").setWidth("420px");
        newsGrid.getColumnByKey("updatedAt").setWidth("420px");
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

    private void showNewsDialog(News news) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setHeight("500px");

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
        contentField.setHeight("200px");
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

    private void createNews(String title, String content, String author) {
        try {
            newsService.createNews(title, content, author);
        } catch (Exception e) {
            org.apolenkov.application.views.utils.NotificationHelper.showError(
                    getTranslation("admin.news.error.create", e.getMessage()));
        }
    }

    private void updateNews(Long id, String title, String content, String author) {
        try {
            newsService.updateNews(id, title, content, author);
        } catch (Exception e) {
            org.apolenkov.application.views.utils.NotificationHelper.showError(
                    getTranslation("admin.news.error.update", e.getMessage()));
        }
    }

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

    private void refreshNews() {
        newsList.clear();
        newsList.addAll(newsService.getAllNews());
        dataProvider.refreshAll();
    }

    @Override
    public String getPageTitle() {
        return getTranslation("admin.content.page.title");
    }
}
