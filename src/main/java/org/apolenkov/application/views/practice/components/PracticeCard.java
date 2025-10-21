package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Optional;
import org.apolenkov.application.model.Card;
import org.apolenkov.application.model.PracticeDirection;
import org.apolenkov.application.views.practice.constants.PracticeConstants;

/**
 * Card component for practice view.
 * Displays card content with question, answer, and optional example.
 */
public final class PracticeCard extends Composite<Div> {

    // UI Components
    private Div cardContent;

    /**
     * Creates a new PracticeCard component.
     */
    public PracticeCard() {
        // Constructor - data only
    }

    @Override
    protected Div initContent() {
        Div cardContainer = new Div();
        cardContainer.addClassName(PracticeConstants.PRACTICE_CARD_CONTAINER_CLASS);

        cardContent = new Div();
        cardContent.addClassName(PracticeConstants.PRACTICE_CARD_CONTENT_CLASS);
        cardContent.add(new Span(getTranslation(PracticeConstants.PRACTICE_LOADING_CARDS_KEY)));

        cardContainer.add(cardContent);
        return cardContainer;
    }

    /**
     * Displays the question card with the current card.
     *
     * @param currentCard the current card to display
     * @param direction the practice direction
     * @throws IllegalArgumentException if currentCard is null or direction is null
     */
    public void displayQuestionCard(final Card currentCard, final PracticeDirection direction) {
        if (currentCard == null) {
            throw new IllegalArgumentException("Current card cannot be null");
        }
        if (direction == null) {
            throw new IllegalArgumentException("Practice direction cannot be null");
        }

        cardContent.removeAll();

        VerticalLayout cardLayout = createCardLayout();
        String questionText = getQuestionText(currentCard, direction);
        H1 question = new H1(Optional.ofNullable(questionText).orElse(""));
        Span transcription = new Span(" ");

        cardLayout.add(question, transcription);
        cardContent.add(cardLayout);
    }

    /**
     * Displays the answer card with question, divider, answer and optional example.
     *
     * @param currentCard the current card to display
     * @param direction the practice direction
     * @throws IllegalArgumentException if currentCard is null or direction is null
     */
    public void displayAnswerCard(final Card currentCard, final PracticeDirection direction) {
        if (currentCard == null) {
            throw new IllegalArgumentException("Current card cannot be null");
        }
        if (direction == null) {
            throw new IllegalArgumentException("Practice direction cannot be null");
        }

        cardContent.removeAll();

        VerticalLayout cardLayout = createCardLayout();
        String questionText = getQuestionText(currentCard, direction);
        String answerText = getAnswerText(currentCard, direction);

        H2 question = new H2(Optional.ofNullable(questionText).orElse(""));
        Hr divider = new Hr();
        H1 answer = new H1(Optional.ofNullable(answerText).orElse(""));

        cardLayout.add(question, divider, answer);

        addExampleIfPresent(currentCard, cardLayout);
        cardContent.add(cardLayout);
    }

    /**
     * Displays the completion screen with session results.
     *
     * @param deckTitle the deck title
     * @param correctCount number of correct answers
     * @param totalCards total number of cards
     * @param hardCount number of hard cards
     * @param sessionMinutes session duration in minutes
     * @param avgSeconds average time per card in seconds
     * @throws IllegalArgumentException if deckTitle is null or blank, or counts are negative
     */
    public void displayCompletion(
            final String deckTitle,
            final int correctCount,
            final int totalCards,
            final int hardCount,
            final long sessionMinutes,
            final long avgSeconds) {

        if (deckTitle == null || deckTitle.isBlank()) {
            throw new IllegalArgumentException("Deck title cannot be null or blank");
        }
        if (correctCount < 0) {
            throw new IllegalArgumentException("Correct count cannot be negative");
        }
        if (totalCards < 0) {
            throw new IllegalArgumentException("Total cards cannot be negative");
        }
        if (hardCount < 0) {
            throw new IllegalArgumentException("Hard count cannot be negative");
        }
        if (sessionMinutes < 0) {
            throw new IllegalArgumentException("Session minutes cannot be negative");
        }
        if (avgSeconds < 0) {
            throw new IllegalArgumentException("Average seconds cannot be negative");
        }

        cardContent.removeAll();
        VerticalLayout completionLayout = new VerticalLayout();
        completionLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        completionLayout.setSpacing(true);

        H1 completionTitle = new H1(getTranslation(PracticeConstants.PRACTICE_SESSION_COMPLETE_KEY, deckTitle));
        H3 results =
                new H3(getTranslation(PracticeConstants.PRACTICE_RESULTS_KEY, correctCount, totalCards, hardCount));
        Span timeInfo = new Span(getTranslation(PracticeConstants.PRACTICE_TIME_KEY, sessionMinutes, avgSeconds));

        completionLayout.add(completionTitle, results, timeInfo);
        cardContent.add(completionLayout);
    }

    /**
     * Creates a card layout for displaying card content.
     *
     * @return configured card layout
     */
    private VerticalLayout createCardLayout() {
        VerticalLayout cardLayout = new VerticalLayout();
        cardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        cardLayout.setSpacing(true);
        return cardLayout;
    }

    /**
     * Gets the question text based on practice direction.
     *
     * @param card the card
     * @param direction the practice direction
     * @return the question text
     */
    private String getQuestionText(final Card card, final PracticeDirection direction) {
        return direction == PracticeDirection.BACK_TO_FRONT ? card.getBackText() : card.getFrontText();
    }

    /**
     * Gets the answer text based on practice direction.
     *
     * @param card the card
     * @param direction the practice direction
     * @return the answer text
     */
    private String getAnswerText(final Card card, final PracticeDirection direction) {
        return direction == PracticeDirection.BACK_TO_FRONT ? card.getFrontText() : card.getBackText();
    }

    /**
     * Adds example text to the card layout if present.
     *
     * @param currentCard the current card
     * @param cardLayout the card layout to add example to
     */
    private void addExampleIfPresent(final Card currentCard, final VerticalLayout cardLayout) {
        if (currentCard.getExample() != null && !currentCard.getExample().isBlank()) {
            Span exampleText =
                    new Span(getTranslation(PracticeConstants.PRACTICE_EXAMPLE_PREFIX_KEY, currentCard.getExample()));
            cardLayout.add(exampleText);
        }
    }
}
