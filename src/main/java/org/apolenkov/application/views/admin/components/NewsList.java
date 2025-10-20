package org.apolenkov.application.views.admin.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import org.apolenkov.application.model.News;
import org.apolenkov.application.views.admin.constants.AdminConstants;

/**
 * Reusable list component for displaying news articles.
 * Handles rendering of news cards and empty state display.
 */
public final class NewsList extends Composite<VerticalLayout> {

    // Callbacks for news actions
    private transient NewsActionCallback editCallback;
    private transient NewsActionCallback deleteCallback;

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        return layout;
    }

    /**
     * Sets the callback for edit action.
     *
     * @param callback the callback to execute when edit is clicked
     */
    public void setEditCallback(final NewsActionCallback callback) {
        this.editCallback = callback;
    }

    /**
     * Sets the callback for delete action.
     *
     * @param callback the callback to execute when delete is clicked
     */
    public void setDeleteCallback(final NewsActionCallback callback) {
        this.deleteCallback = callback;
    }

    /**
     * Refreshes the news list with new data.
     *
     * @param newsList the list of news to display
     */
    public void refreshNews(final List<News> newsList) {
        getContent().removeAll();

        if (newsList.isEmpty()) {
            displayEmptyState();
            return;
        }

        displayNewsCards(newsList);
    }

    /**
     * Displays empty state when no news found.
     */
    private void displayEmptyState() {
        Span empty = new Span(getTranslation(AdminConstants.ADMIN_CONTENT_SEARCH_NO_RESULTS_KEY));
        empty.addClassName(AdminConstants.ADMIN_CONTENT_EMPTY_MESSAGE_CLASS);
        getContent().add(empty);
    }

    /**
     * Displays news cards for filtered news.
     *
     * @param newsList the news to display
     */
    private void displayNewsCards(final List<News> newsList) {
        newsList.forEach(news -> {
            NewsCard card = new NewsCard(news, editCallback, deleteCallback);
            getContent().add(card);
        });
    }

    /**
     * Callback interface for news actions.
     */
    @FunctionalInterface
    public interface NewsActionCallback {
        /**
         * Executes action on news item.
         *
         * @param news the news item
         */
        void execute(News news);
    }
}
