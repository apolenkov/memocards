package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;

/**
 * Component for displaying when all cards in a deck are already known.
 * Shows a congratulatory message and provides navigation back to the deck.
 */
public final class PracticeAllKnownView extends Composite<VerticalLayout> {

    // Data
    private final transient Deck deck;
    private final String deckTitle;

    /**
     * Creates a new PracticeAllKnownView for the specified deck.
     *
     * @param deckValue the deck for which all cards are known
     * @param deckTitleValue the display title of the deck
     * @throws IllegalArgumentException if deck is null or deckTitle is blank
     */
    public PracticeAllKnownView(final Deck deckValue, final String deckTitleValue) {
        if (deckValue == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }
        if (deckTitleValue == null || deckTitleValue.isBlank()) {
            throw new IllegalArgumentException("Deck title cannot be null or blank");
        }

        this.deck = deckValue;
        this.deckTitle = deckTitleValue;

        setupLayout();
        createContent();
    }

    /**
     * Sets up the main layout properties.
     */
    private void setupLayout() {
        getContent().setWidthFull();
        getContent().setPadding(true);
        getContent().setSpacing(true);
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);
        getContent().addClassName(PracticeConstants.CONTAINER_MD_CLASS);
    }

    /**
     * Creates the content for the all-known view.
     */
    private void createContent() {
        VerticalLayout contentContainer = createContentContainer();
        VerticalLayout messageSection = createMessageSection();
        VerticalLayout buttonSection = createButtonSection();

        contentContainer.add(messageSection, buttonSection);
        getContent().add(contentContainer);
    }

    /**
     * Creates the main content container.
     *
     * @return configured content container
     */
    private VerticalLayout createContentContainer() {
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.setWidthFull();
        contentContainer.addClassName(PracticeConstants.CONTAINER_MD_CLASS);
        contentContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        return contentContainer;
    }

    /**
     * Creates the message section with congratulations.
     *
     * @return configured message section
     */
    private VerticalLayout createMessageSection() {
        VerticalLayout messageSection = new VerticalLayout();
        messageSection.setSpacing(true);
        messageSection.setPadding(true);
        messageSection.setWidthFull();
        messageSection.addClassName(PracticeConstants.PRACTICE_VIEW_SECTION_CLASS);
        messageSection.addClassName(PracticeConstants.SURFACE_PANEL_CLASS);
        messageSection.setAlignItems(FlexComponent.Alignment.CENTER);

        H2 title = new H2(getTranslation(PracticeConstants.PRACTICE_ALL_KNOWN_TITLE_KEY));
        title.addClassName("practice-all-known__title");

        Span message = new Span(getTranslation(PracticeConstants.PRACTICE_ALL_KNOWN_MESSAGE_KEY, deckTitle));
        message.addClassName("practice-all-known__message");

        messageSection.add(title, message);
        return messageSection;
    }

    /**
     * Creates the button section with navigation options.
     *
     * @return configured button section
     */
    private VerticalLayout createButtonSection() {
        VerticalLayout buttonSection = new VerticalLayout();
        buttonSection.setSpacing(true);
        buttonSection.setPadding(true);
        buttonSection.setWidthFull();
        buttonSection.addClassName(PracticeConstants.PRACTICE_VIEW_SECTION_CLASS);
        buttonSection.addClassName(PracticeConstants.SURFACE_PANEL_CLASS);
        buttonSection.setAlignItems(FlexComponent.Alignment.CENTER);

        Button backToDeckButton = ButtonHelper.createPrimaryButton(
                getTranslation(PracticeConstants.PRACTICE_BACK_TO_DECK_KEY),
                e -> NavigationHelper.navigateToDeck(deck.getId()));
        backToDeckButton.addClassName("practice-all-known__back-button");

        Button homeButton = ButtonHelper.createTertiaryButton(
                getTranslation(PracticeConstants.PRACTICE_HOME_KEY), e -> NavigationHelper.navigateToDecks());
        homeButton.addClassName("practice-all-known__home-button");

        buttonSection.add(backToDeckButton, homeButton);
        return buttonSection;
    }
}
