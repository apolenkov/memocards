package org.apolenkov.application.views.admin.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.time.format.DateTimeFormatter;
import org.apolenkov.application.model.News;
import org.apolenkov.application.views.admin.components.NewsList.NewsActionCallback;
import org.apolenkov.application.views.admin.constants.AdminConstants;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;

/**
 * Card component for displaying a single news article.
 * Provides a consistent layout for news information with action buttons.
 */
public final class NewsCard extends Composite<VerticalLayout> {

    private final transient News news;
    private final transient NewsActionCallback editCallback;
    private final transient NewsActionCallback deleteCallback;

    /**
     * Creates a new NewsCard.
     *
     * @param newsItem the news item to display
     * @param editCallbackValue callback for edit action
     * @param deleteCallbackValue callback for delete action
     */
    public NewsCard(
            final News newsItem,
            final NewsActionCallback editCallbackValue,
            final NewsActionCallback deleteCallbackValue) {
        this.news = newsItem;
        this.editCallback = editCallbackValue;
        this.deleteCallback = deleteCallbackValue;
    }

    @Override
    protected VerticalLayout initContent() {
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
        HorizontalLayout metaInfo = createMetaInfo();

        // Action buttons
        HorizontalLayout actions = createActions();

        card.add(title, content, metaInfo, actions);
        return card;
    }

    /**
     * Creates metadata layout with author and date.
     *
     * @return layout with metadata
     */
    private HorizontalLayout createMetaInfo() {
        HorizontalLayout metaInfo = new HorizontalLayout();
        metaInfo.setSpacing(true);
        metaInfo.setAlignItems(FlexComponent.Alignment.CENTER);

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
        return metaInfo;
    }

    /**
     * Creates action buttons layout.
     *
     * @return layout with action buttons
     */
    private HorizontalLayout createActions() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        actions.addClassName(AdminConstants.NEWS_CARD_ACTIONS_CLASS);

        Button editBtn = ButtonHelper.createButton(
                getTranslation(AdminConstants.COMMON_EDIT_KEY),
                VaadinIcon.EDIT,
                e -> {
                    if (editCallback != null) {
                        editCallback.execute(news);
                    }
                },
                ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_SMALL);

        Button deleteBtn = ButtonHelper.createButton(
                getTranslation(AdminConstants.COMMON_DELETE_KEY),
                VaadinIcon.TRASH,
                e -> {
                    if (deleteCallback != null) {
                        deleteCallback.execute(news);
                    }
                },
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_SMALL);

        actions.add(editBtn, deleteBtn);
        return actions;
    }
}
