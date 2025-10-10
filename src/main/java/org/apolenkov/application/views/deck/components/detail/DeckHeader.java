package org.apolenkov.application.views.deck.components.detail;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.utils.NavigationHelper;

/**
 * Header component for deck view containing navigation, title and statistics.
 * Provides a consistent header layout with back navigation, deck title display,
 * and statistics information.
 */
public final class DeckHeader extends Composite<HorizontalLayout> {

    // UI Components
    private final Button backButton;
    private final H2 deckTitle;
    private final Span deckStats;

    /**
     * Creates a new DeckHeader component.
     * Initializes UI components without configuring them to avoid this-escape warnings.
     */
    public DeckHeader() {
        this.backButton = new Button();
        this.deckTitle = new H2();
        this.deckStats = new Span();
    }

    @Override
    protected HorizontalLayout initContent() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);

        configureBackButton();
        configureTitle();
        configureStats();

        leftSection.add(backButton, deckTitle, deckStats);
        header.add(leftSection);
        return header;
    }

    /**
     * Configures the back navigation button.
     * Sets up button with proper styling and navigation functionality.
     */
    private void configureBackButton() {
        backButton.setText(getTranslation(DeckConstants.COMMON_BACK));
        backButton.setIcon(VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> NavigationHelper.navigateToDecks());
        backButton.setText(getTranslation(DeckConstants.DECKS_TITLE_KEY));
    }

    /**
     * Configures the deck title display.
     * Sets up the title with proper styling and initial loading state.
     */
    private void configureTitle() {
        deckTitle.setText(getTranslation(DeckConstants.DECK_LOADING));
        deckTitle.addClassName(DeckConstants.DECK_VIEW_TITLE_CLASS);
    }

    /**
     * Configures the deck statistics display.
     * Sets up the stats span with proper styling.
     */
    private void configureStats() {
        deckStats.addClassName(DeckConstants.DECK_VIEW_STATS_CLASS);
    }

    /**
     * Updates the deck title with the provided text.
     *
     * @param title the new title text to display
     */
    public void setDeckTitle(final String title) {
        deckTitle.setText(title);
    }

    /**
     * Updates the deck statistics with the provided text.
     *
     * @param stats the new statistics text to display
     */
    public void setDeckStats(final String stats) {
        deckStats.setText(stats);
    }
}
