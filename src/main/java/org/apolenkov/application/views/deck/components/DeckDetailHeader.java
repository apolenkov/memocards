package org.apolenkov.application.views.deck.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unified header component for deck view containing navigation, title, description, and actions.
 * Combines functionality of DeckHeader, DeckInfo, and DeckActions for simpler code structure.
 *
 * <p>Features:
 * <ul>
 *   <li>Back navigation to decks list</li>
 *   <li>Deck title and statistics display</li>
 *   <li>Deck description section</li>
 *   <li>Action buttons: Practice, Edit, Delete</li>
 * </ul>
 */
public final class DeckDetailHeader extends Composite<VerticalLayout> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckDetailHeader.class);

    // Header components
    private final Button backButton;
    private final H2 deckTitle;
    private final Span deckStats;

    // Description section
    private final Div descriptionSection;
    private final Span description;

    // Action buttons
    private final Button practiceButton;
    private final Button editDeckButton;
    private final Button deleteDeckButton;

    /**
     * Creates a new DeckDetailHeader component.
     */
    public DeckDetailHeader() {
        this.backButton = new Button();
        this.deckTitle = new H2();
        this.deckStats = new Span();
        this.descriptionSection = new Div();
        this.description = new Span();
        this.practiceButton = new Button();
        this.editDeckButton = new Button();
        this.deleteDeckButton = new Button();
    }

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout container = new VerticalLayout();
        container.setSpacing(true);
        container.setPadding(false);
        container.setWidthFull();

        // Header row: [← Back] [Title + Stats]
        HorizontalLayout headerRow = createHeaderRow();

        // Description section
        Div descSection = createDescriptionSection();

        // Action buttons row
        HorizontalLayout actionsRow = createActionsRow();

        container.add(headerRow, descSection, actionsRow);
        return container;
    }

    /**
     * Creates header row with back button and title.
     *
     * @return configured header row
     */
    private HorizontalLayout createHeaderRow() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.addClassName(DeckConstants.DECK_VIEW_HEADER_CLASS);

        // Left section: Back button + Title + Stats
        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);
        leftSection.addClassName(DeckConstants.DECK_VIEW_TITLE_SECTION_CLASS);

        configureBackButton();
        configureTitle();
        configureStats();

        leftSection.add(backButton, deckTitle, deckStats);
        header.add(leftSection);

        return header;
    }

    /**
     * Creates description section with deck info.
     *
     * @return configured description section
     */
    private Div createDescriptionSection() {
        descriptionSection.addClassName(DeckConstants.DECK_VIEW_INFO_SECTION_CLASS);
        descriptionSection.addClassName(DeckConstants.SURFACE_PANEL_CLASS);

        description.addClassName(DeckConstants.DECK_VIEW_DESCRIPTION_CLASS);
        description.setText(getTranslation(DeckConstants.DECK_DESCRIPTION_LOADING));

        descriptionSection.add(description);
        return descriptionSection;
    }

    /**
     * Creates actions row with practice, edit, and delete buttons.
     *
     * @return configured actions row
     */
    private HorizontalLayout createActionsRow() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        actions.addClassName(DeckConstants.DECK_VIEW_ACTIONS_CLASS);

        configurePracticeButton();
        configureEditDeckButton();
        configureDeleteDeckButton();

        actions.add(practiceButton, editDeckButton, deleteDeckButton);
        return actions;
    }

    /**
     * Configures the back navigation button.
     */
    private void configureBackButton() {
        backButton.setText(getTranslation(DeckConstants.DECKS_TITLE_KEY));
        backButton.setIcon(VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClassName(DeckConstants.DECK_VIEW_BACK_BUTTON_CLASS);
        backButton.addClickListener(e -> NavigationHelper.navigateToDecks());
    }

    /**
     * Configures the deck title display.
     */
    private void configureTitle() {
        deckTitle.setText(getTranslation(DeckConstants.DECK_LOADING));
        deckTitle.addClassName(DeckConstants.DECK_VIEW_TITLE_CLASS);
    }

    /**
     * Configures the deck statistics display.
     */
    private void configureStats() {
        deckStats.addClassName(DeckConstants.DECK_VIEW_STATS_CLASS);
    }

    /**
     * Configures the practice button.
     */
    private void configurePracticeButton() {
        practiceButton.setText(getTranslation(DeckConstants.DECK_START_SESSION));
        practiceButton.setIcon(VaadinIcon.PLAY.create());
        practiceButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
    }

    /**
     * Configures the edit deck button.
     */
    private void configureEditDeckButton() {
        editDeckButton.setText(getTranslation(DeckConstants.COMMON_EDIT));
        editDeckButton.setIcon(VaadinIcon.EDIT.create());
        editDeckButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        editDeckButton
                .getElement()
                .setProperty(DeckConstants.TITLE_PROPERTY, getTranslation(DeckConstants.DECK_EDIT_TOOLTIP));
    }

    /**
     * Configures the delete deck button.
     */
    private void configureDeleteDeckButton() {
        deleteDeckButton.setText(getTranslation(DeckConstants.COMMON_DELETE));
        deleteDeckButton.setIcon(VaadinIcon.TRASH.create());
        deleteDeckButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
    }

    // ==================== Public API ====================

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

    /**
     * Updates the deck description with the provided text.
     *
     * @param descriptionText the new description text to display
     */
    public void setDescription(final String descriptionText) {
        description.setText(descriptionText);
    }

    /**
     * Adds a listener for practice button clicks.
     *
     * @param listener the event listener for practice button clicks
     * @return registration for removing the listener
     */
    public Registration addPracticeClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        return practiceButton.addClickListener(e -> {
            LOGGER.debug("Practice button clicked");
            listener.onComponentEvent(e);
        });
    }

    /**
     * Adds a listener for edit deck button clicks.
     *
     * @param listener the event listener for edit deck button clicks
     * @return registration for removing the listener
     */
    public Registration addEditDeckClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        return editDeckButton.addClickListener(e -> {
            LOGGER.debug("Edit deck button clicked");
            listener.onComponentEvent(e);
        });
    }

    /**
     * Adds a listener for delete deck button clicks.
     *
     * @param listener the event listener for delete deck button clicks
     * @return registration for removing the listener
     */
    public Registration addDeleteDeckClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        return deleteDeckButton.addClickListener(e -> {
            LOGGER.debug("Delete deck button clicked");
            listener.onComponentEvent(e);
        });
    }
}
