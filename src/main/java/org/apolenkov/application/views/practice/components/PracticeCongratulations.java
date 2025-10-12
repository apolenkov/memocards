package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apolenkov.application.views.practice.constants.PracticeConstants;

/**
 * Congratulations component for practice view.
 * Displays inline congratulations when all cards are mastered.
 */
public final class PracticeCongratulations extends Composite<VerticalLayout> {

    private String deckTitle;
    private transient Runnable onBackToDeck;

    /**
     * Creates a new PracticeCongratulations component.
     *
     * @param deckTitleParam the title of the deck
     * @param onBackToDeckParam callback executed when user clicks back to deck
     */
    public PracticeCongratulations(final String deckTitleParam, final Runnable onBackToDeckParam) {
        this.deckTitle = deckTitleParam;
        this.onBackToDeck = onBackToDeckParam;
    }

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("practice-congratulations");
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setWidthFull();

        // Create celebration icon
        Div iconContainer = createCelebrationIcon();

        // Create title
        H2 title = new H2(getTranslation(PracticeConstants.PRACTICE_ALL_KNOWN_TITLE_KEY));
        title.addClassName("congratulations-title");

        // Create message
        Paragraph message = new Paragraph(getTranslation(PracticeConstants.PRACTICE_ALL_KNOWN_MESSAGE_KEY, deckTitle));
        message.addClassName("congratulations-message");

        // Create action button
        Button backToDeckButton = new Button(getTranslation(PracticeConstants.PRACTICE_BACK_TO_DECK_KEY));
        backToDeckButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        backToDeckButton.addClickListener(e -> {
            if (onBackToDeck != null) {
                onBackToDeck.run();
            }
        });

        layout.add(iconContainer, title, message, backToDeckButton);
        return layout;
    }

    /**
     * Creates a celebration icon container with animation.
     *
     * @return a Div container with animated trophy icon
     */
    private Div createCelebrationIcon() {
        Div iconContainer = new Div();
        iconContainer.addClassName("celebration-icon-container");

        Span trophyIcon = new Span("🏆");
        trophyIcon.addClassName("trophy-icon");

        iconContainer.add(trophyIcon);
        return iconContainer;
    }

    /**
     * Updates the congratulations component with new deck title and callback.
     *
     * @param newDeckTitle the new deck title
     * @param newOnBackToDeck the new callback
     */
    public void updateContent(final String newDeckTitle, final Runnable newOnBackToDeck) {
        this.deckTitle = newDeckTitle;
        this.onBackToDeck = newOnBackToDeck;

        // Rebuild the content with new data
        VerticalLayout layout = getContent();
        layout.removeAll();

        // Create celebration icon
        Div iconContainer = createCelebrationIcon();

        // Create title
        H2 title = new H2(getTranslation(PracticeConstants.PRACTICE_ALL_KNOWN_TITLE_KEY));
        title.addClassName("congratulations-title");

        // Create message
        Paragraph message = new Paragraph(getTranslation(PracticeConstants.PRACTICE_ALL_KNOWN_MESSAGE_KEY, deckTitle));
        message.addClassName("congratulations-message");

        // Create action button
        Button backToDeckButton = new Button(getTranslation(PracticeConstants.PRACTICE_BACK_TO_DECK_KEY));
        backToDeckButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        backToDeckButton.addClickListener(e -> {
            if (onBackToDeck != null) {
                onBackToDeck.run();
            }
        });

        layout.add(iconContainer, title, message, backToDeckButton);
    }
}
