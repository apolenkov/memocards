package org.apolenkov.application.views.landing.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import org.apolenkov.application.model.News;
import org.apolenkov.application.service.news.NewsService;
import org.apolenkov.application.views.landing.constants.LandingConstants;

/**
 * News section component for the landing page.
 * Displays news title and list of news cards.
 */
public final class NewsSection extends Composite<Div> {

    private final transient NewsService newsService;

    /**
     * Creates a new NewsSection component.
     *
     * @param service service for retrieving news content
     */
    public NewsSection(final NewsService service) {
        this.newsService = service;
    }

    @Override
    protected Div initContent() {
        Div content = new Div();
        content.addClassName(LandingConstants.SURFACE_PANEL_CLASS);
        content.addClassName(LandingConstants.LANDING_NEWS_SECTION_CLASS);

        H3 newsTitle = createNewsTitle();
        Div newsList = createNewsList();

        content.add(newsTitle, newsList);
        return content;
    }

    /**
     * Creates the news section title.
     *
     * @return H3 element with the news title
     */
    private H3 createNewsTitle() {
        H3 newsTitle = new H3(getTranslation(LandingConstants.LANDING_NEWS_KEY));
        newsTitle.addClassName(LandingConstants.LANDING_NEWS_TITLE_CLASS);
        return newsTitle;
    }

    /**
     * Creates a news list container with all news cards.
     *
     * @return Div containing all news cards
     */
    private Div createNewsList() {
        Div newsList = new Div();
        newsList.addClassName(LandingConstants.LANDING_NEWS_LIST_CLASS);

        for (News item : newsService.getAllNews()) {
            Div card = createNewsCard(item);
            newsList.add(card);
        }

        return newsList;
    }

    /**
     * Creates a single news card component.
     *
     * @param item the news item to display in the card
     * @return Div representing the news card
     */
    private Div createNewsCard(final News item) {
        Div card = new Div();
        card.addClassName(LandingConstants.SURFACE_CARD_CLASS);
        card.addClassName(LandingConstants.LANDING_NEWS_CARD_CLASS);

        H3 cardTitle = createCardTitle(item);
        Paragraph cardContent = createCardContent(item);
        Div accent = createCardAccent();

        card.add(cardTitle, cardContent, accent);
        return card;
    }

    /**
     * Creates the card title.
     *
     * @param item the news item
     * @return H3 element with the card title
     */
    private H3 createCardTitle(final News item) {
        H3 cardTitle = new H3(item.getTitle());
        cardTitle.addClassName(LandingConstants.LANDING_NEWS_CARD_TITLE_CLASS);
        return cardTitle;
    }

    /**
     * Creates the card content paragraph.
     *
     * @param item the news item
     * @return Paragraph element with the card content
     */
    private Paragraph createCardContent(final News item) {
        Paragraph cardContent = new Paragraph(item.getContent());
        cardContent.addClassName(LandingConstants.LANDING_NEWS_CARD_CONTENT_CLASS);
        return cardContent;
    }

    /**
     * Creates the card accent element.
     *
     * @return Div element for card accent
     */
    private Div createCardAccent() {
        Div accent = new Div();
        accent.addClassName(LandingConstants.LANDING_NEWS_CARD_ACCENT_CLASS);
        return accent;
    }
}
