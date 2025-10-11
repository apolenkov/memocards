package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apolenkov.application.views.practice.constants.PracticeConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;

/**
 * Actions component for practice view.
 * Manages practice action buttons (show answer, know, hard) and completion buttons.
 * Uses visibility management to avoid component reuse issues.
 */
public final class PracticeActions extends Composite<HorizontalLayout> {

    // UI Components - Practice buttons
    private Button showAnswerButton;
    private Button knowButton;
    private Button hardButton;

    // UI Components - Completion buttons (created dynamically)
    private Button repeatButton;
    private Button backToDeckButton;
    private Button homeButton;

    /**
     * Creates a new PracticeActions component.
     */
    public PracticeActions() {
        // Constructor - data only
    }

    @Override
    protected HorizontalLayout initContent() {
        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setSpacing(true);
        actionButtons.setWidthFull();
        actionButtons.setAlignItems(FlexComponent.Alignment.CENTER);
        actionButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        createActionButtons(actionButtons);
        return actionButtons;
    }

    /**
     * Creates the main action buttons for practice.
     * All buttons are added to the container immediately to avoid component reuse.
     *
     * @param container the container to add buttons to
     */
    private void createActionButtons(final HorizontalLayout container) {
        showAnswerButton = ButtonHelper.createButton(
                getTranslation(PracticeConstants.PRACTICE_SHOW_ANSWER_KEY),
                e -> {}, // Placeholder click handler
                ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_LARGE);

        knowButton = ButtonHelper.createButton(
                getTranslation(PracticeConstants.PRACTICE_KNOW_KEY),
                e -> {}, // Placeholder click handler
                ButtonVariant.LUMO_SUCCESS,
                ButtonVariant.LUMO_LARGE);
        knowButton.setVisible(false);

        hardButton = ButtonHelper.createButton(
                getTranslation(PracticeConstants.PRACTICE_HARD_KEY),
                e -> {}, // Placeholder click handler
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_LARGE);
        hardButton.setVisible(false);

        container.add(showAnswerButton, knowButton, hardButton);
    }

    /**
     * Sets the click handler for the show answer button.
     *
     * @param clickHandler the click handler
     * @throws IllegalArgumentException if clickHandler is null
     */
    public void setShowAnswerHandler(final Runnable clickHandler) {
        if (clickHandler == null) {
            throw new IllegalArgumentException("Click handler cannot be null");
        }
        showAnswerButton.addClickListener(e -> clickHandler.run());
    }

    /**
     * Sets the click handler for the know button.
     *
     * @param clickHandler the click handler
     * @throws IllegalArgumentException if clickHandler is null
     */
    public void setKnowHandler(final Runnable clickHandler) {
        if (clickHandler == null) {
            throw new IllegalArgumentException("Click handler cannot be null");
        }
        knowButton.addClickListener(e -> clickHandler.run());
    }

    /**
     * Sets the click handler for the hard button.
     *
     * @param clickHandler the click handler
     * @throws IllegalArgumentException if clickHandler is null
     */
    public void setHardHandler(final Runnable clickHandler) {
        if (clickHandler == null) {
            throw new IllegalArgumentException("Click handler cannot be null");
        }
        hardButton.addClickListener(e -> clickHandler.run());
    }

    /**
     * Updates button visibility for question state.
     */
    public void showQuestionState() {
        showAnswerButton.setVisible(true);
        knowButton.setVisible(false);
        hardButton.setVisible(false);
    }

    /**
     * Updates button visibility for answer state.
     */
    public void showAnswerState() {
        showAnswerButton.setVisible(false);
        knowButton.setVisible(true);
        hardButton.setVisible(true);
    }

    /**
     * Hides the action buttons after marking.
     */
    public void hideActionButtons() {
        knowButton.setVisible(false);
        hardButton.setVisible(false);
    }

    /**
     * Shows completion buttons (optional repeat, back to deck, home).
     * Hides practice buttons and shows completion buttons without removing from DOM.
     *
     * @param repeatHandler handler for repeat button (null if no failed cards)
     * @param backToDeckHandler handler for back to deck button
     * @param homeHandler handler for home button
     * @throws IllegalArgumentException if backToDeckHandler or homeHandler is null
     */
    public void showCompletionButtons(
            final Runnable repeatHandler, final Runnable backToDeckHandler, final Runnable homeHandler) {

        if (backToDeckHandler == null) {
            throw new IllegalArgumentException("Back to deck handler cannot be null");
        }
        if (homeHandler == null) {
            throw new IllegalArgumentException("Home handler cannot be null");
        }

        // Hide practice buttons instead of removing
        hidePracticeButtons();

        // Remove old completion buttons if they exist
        removeCompletionButtons();

        // Create and show new completion buttons
        createCompletionButtons(repeatHandler, backToDeckHandler, homeHandler);
    }

    /**
     * Resets to practice buttons state.
     * Hides completion buttons and shows practice buttons without component reuse.
     */
    public void resetToPracticeButtons() {
        // Remove completion buttons from DOM
        removeCompletionButtons();

        // Show practice buttons again
        showQuestionState();
    }

    /**
     * Hides all practice buttons (show answer, know, hard).
     */
    private void hidePracticeButtons() {
        showAnswerButton.setVisible(false);
        knowButton.setVisible(false);
        hardButton.setVisible(false);
    }

    /**
     * Creates and adds completion buttons to the layout.
     *
     * @param repeatHandler handler for repeat button (null if no failed cards)
     * @param backToDeckHandler handler for back to deck button
     * @param homeHandler handler for home button
     */
    private void createCompletionButtons(
            final Runnable repeatHandler, final Runnable backToDeckHandler, final Runnable homeHandler) {

        // Create repeat button only if there are failed cards to practice
        if (repeatHandler != null) {
            repeatButton = ButtonHelper.createButton(
                    getTranslation(PracticeConstants.PRACTICE_REPEAT_HARD_KEY),
                    e -> repeatHandler.run(),
                    ButtonVariant.LUMO_ERROR,
                    ButtonVariant.LUMO_LARGE);
            getContent().add(repeatButton);
        }

        backToDeckButton = ButtonHelper.createButton(
                getTranslation(PracticeConstants.PRACTICE_BACK_TO_DECK_KEY), e -> backToDeckHandler.run());

        homeButton = ButtonHelper.createButton(
                getTranslation(PracticeConstants.PRACTICE_BACK_TO_DECKS_KEY), e -> homeHandler.run());

        getContent().add(backToDeckButton, homeButton);
    }

    /**
     * Removes completion buttons from the layout if they exist.
     */
    private void removeCompletionButtons() {
        if (repeatButton != null) {
            getContent().remove(repeatButton);
            repeatButton = null;
        }
        if (backToDeckButton != null) {
            getContent().remove(backToDeckButton);
            backToDeckButton = null;
        }
        if (homeButton != null) {
            getContent().remove(homeButton);
            homeButton = null;
        }
    }
}
