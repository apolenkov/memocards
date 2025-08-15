package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = "admin/news", layout = PublicLayout.class)
@RouteAlias(value = "admin/content", layout = PublicLayout.class)
@RolesAllowed("ADMIN")
public class AdminNewsView extends VerticalLayout implements HasDynamicTitle {

    private final NewsService newsService;
    private final Grid<News> newsGrid;
    private final ListDataProvider<News> dataProvider;
    private final List<News> newsList;

    public AdminNewsView(NewsService newsService) {
        this.newsService = newsService;
        this.newsList = new ArrayList<>();

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(getTranslation("admin.content.page.title"));
        add(title);

        Button addNewsBtn = new Button(getTranslation("admin.news.add"), e -> showNewsDialog(null));
        addNewsBtn.getStyle().set("margin-bottom", "1rem");
        add(addNewsBtn);

        newsGrid = new Grid<>(News.class);
        newsGrid.setColumns("title", "content", "author", "createdAt", "updatedAt");
        newsGrid.getColumnByKey("title").setHeader(getTranslation("admin.news.title"));
        newsGrid.getColumnByKey("content").setHeader(getTranslation("admin.news.content"));
        newsGrid.getColumnByKey("author").setHeader(getTranslation("admin.news.author"));
        newsGrid.getColumnByKey("createdAt").setHeader(getTranslation("admin.news.createdAt"));
        newsGrid.getColumnByKey("updatedAt").setHeader(getTranslation("admin.news.updatedAt"));
        newsGrid.getColumnByKey("createdAt")
                .setRenderer(new com.vaadin.flow.data.renderer.LocalDateTimeRenderer<News>(
                        News::getCreatedAt, "dd.MM.yyyy HH:mm"));
        newsGrid.getColumnByKey("updatedAt").setRenderer(new com.vaadin.flow.data.renderer.ComponentRenderer<>(news -> {
            if (news.getUpdatedAt() != null) {
                return new com.vaadin.flow.component.html.Span(
                        news.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            }
            return new com.vaadin.flow.component.html.Span(getTranslation("common.emDash"));
        }));

        newsGrid.getColumnByKey("title").setWidth("200px");
        newsGrid.getColumnByKey("content").setWidth("300px");
        newsGrid.getColumnByKey("author").setWidth("150px");
        newsGrid.getColumnByKey("createdAt").setWidth("150px");
        newsGrid.getColumnByKey("updatedAt").setWidth("150px");
        newsGrid.addComponentColumn(news -> {
                    HorizontalLayout actions = new HorizontalLayout();
                    actions.setSpacing(true);

                    Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> showNewsDialog(news));
                    editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
                    editBtn.getElement().setAttribute("title", getTranslation("dialog.edit"));

                    Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> deleteNews(news));
                    deleteBtn.addThemeVariants(
                            ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
                    deleteBtn.getElement().setAttribute("title", getTranslation("dialog.delete"));

                    actions.add(editBtn, deleteBtn);
                    return actions;
                })
                .setHeader(getTranslation("admin.users.actions"))
                .setWidth("140px")
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

        H3 dialogTitle = new H3(news == null ? getTranslation("admin.news.add") : getTranslation("dialog.edit"));
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
        Button saveBtn = new Button(getTranslation("dialog.save"), e -> {
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

        Button cancelBtn = new Button(getTranslation("dialog.cancel"), e -> dialog.close());
        buttons.add(saveBtn, cancelBtn);
        content.add(buttons);

        dialog.add(content);
        dialog.open();
    }

    private void createNews(String title, String content, String author) {
        try {
            newsService.createNews(title, content, author);
        } catch (Exception e) {
            com.vaadin.flow.component.notification.Notification.show(
                    getTranslation("admin.news.error.create", e.getMessage()),
                    3000,
                    com.vaadin.flow.component.notification.Notification.Position.MIDDLE);
        }
    }

    private void updateNews(Long id, String title, String content, String author) {
        try {
            newsService.updateNews(id, title, content, author);
        } catch (Exception e) {
            com.vaadin.flow.component.notification.Notification.show(
                    getTranslation("admin.news.error.update", e.getMessage()),
                    3000,
                    com.vaadin.flow.component.notification.Notification.Position.MIDDLE);
        }
    }

    private void deleteNews(News news) {
        com.vaadin.flow.component.dialog.Dialog confirmDialog = new com.vaadin.flow.component.dialog.Dialog();
        com.vaadin.flow.component.html.Span msg = new com.vaadin.flow.component.html.Span();
        msg.getElement()
                .setProperty(
                        "innerHTML",
                        getTranslation("admin.news.confirm.delete.prefix")
                                + " <b>" + org.apache.commons.text.StringEscapeUtils.escapeHtml4(news.getTitle())
                                + "</b>"
                                + getTranslation("admin.news.confirm.delete.suffix"));
        confirmDialog.add(msg);

        HorizontalLayout buttons = new HorizontalLayout();
        Button confirmBtn = new Button(getTranslation("dialog.delete"), e -> {
            try {
                newsService.deleteNews(news.getId());
                confirmDialog.close();
                refreshNews();
                com.vaadin.flow.component.notification.Notification.show(
                        getTranslation("admin.news.deleted"),
                        2000,
                        com.vaadin.flow.component.notification.Notification.Position.MIDDLE);
            } catch (Exception ex) {
                com.vaadin.flow.component.notification.Notification.show(
                        getTranslation("admin.news.error.delete", ex.getMessage()),
                        3000,
                        com.vaadin.flow.component.notification.Notification.Position.MIDDLE);
            }
        });
        confirmBtn.getStyle().set("color", "red");

        Button cancelBtn = new Button(getTranslation("dialog.cancel"), e -> confirmDialog.close());
        buttons.add(confirmBtn, cancelBtn);
        confirmDialog.add(buttons);

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
