package org.apolenkov.application.views.deck.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;

/**
 * Component for deck action buttons including practice, add, edit and delete.
 * Provides a consistent layout for action buttons with proper styling
 * and follows the component pattern established in the refactoring.
 */
public final class DeckActions extends HorizontalLayout {

    // Constants
    private static final String TITLE_PROPERTY = "title";

    // UI Components
    private final Button practiceButton;
    private final Button addFlashcardButton;
    private final Button editDeckButton;
    private final Button deleteDeckButton;

    /**
     * Creates a new DeckActions component.
     * Initializes UI components without configuring them to avoid this-escape warnings.
     */
    public DeckActions() {
        this.practiceButton = new Button();
        this.addFlashcardButton = new Button();
        this.editDeckButton = new Button();
        this.deleteDeckButton = new Button();
    }

    /**
     * Configures the actions layout with proper styling.
     * Sets up the horizontal layout with full width.
     */
    private void configureLayout() {
        setWidthFull();
    }

    /**
     * Configures the practice button.
     * Sets up button with proper styling and icon.
     */
    private void configurePracticeButton() {
        practiceButton.setText(getTranslation("common.start"));
        practiceButton.setIcon(VaadinIcon.PLAY.create());
        practiceButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        practiceButton.setText(getTranslation("deck.startSession"));
    }

    /**
     * Configures the add flashcard button.
     * Sets up button with proper styling, icon and test ID.
     */
    private void configureAddFlashcardButton() {
        addFlashcardButton.setText(getTranslation("common.add"));
        addFlashcardButton.setIcon(VaadinIcon.PLUS.create());
        addFlashcardButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addFlashcardButton.setText(getTranslation("deck.addCard"));
        addFlashcardButton.getElement().setAttribute("data-testid", "deck-add-card");
    }

    /**
     * Configures the edit deck button.
     * Sets up button with proper styling and tooltip.
     */
    private void configureEditDeckButton() {
        editDeckButton.setText(getTranslation("common.edit"));
        editDeckButton.setIcon(VaadinIcon.EDIT.create());
        editDeckButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        editDeckButton.getElement().setProperty(TITLE_PROPERTY, getTranslation("deck.edit.tooltip"));
    }

    /**
     * Configures the delete deck button.
     * Sets up button with proper styling and error variant.
     */
    private void configureDeleteDeckButton() {
        deleteDeckButton.setText(getTranslation("common.delete"));
        deleteDeckButton.setIcon(VaadinIcon.TRASH.create());
        deleteDeckButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
    }

    /**
     * Adds all buttons to the layout.
     * Arranges buttons in proper order.
     */
    private void addComponents() {
        add(practiceButton, addFlashcardButton, editDeckButton, deleteDeckButton);
    }

    /**
     * Adds a listener for practice button clicks.
     *
     * @param listener the event listener for practice button clicks
     * @return registration for removing the listener
     */
    public Registration addPracticeClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        return practiceButton.addClickListener(listener);
    }

    /**
     * Adds a listener for add flashcard button clicks.
     *
     * @param listener the event listener for add flashcard button clicks
     * @return registration for removing the listener
     */
    public Registration addAddFlashcardClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        return addFlashcardButton.addClickListener(listener);
    }

    /**
     * Adds a listener for edit deck button clicks.
     *
     * @param listener the event listener for edit deck button clicks
     * @return registration for removing the listener
     */
    public Registration addEditDeckClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        return editDeckButton.addClickListener(listener);
    }

    /**
     * Adds a listener for delete deck button clicks.
     *
     * @param listener the event listener for delete deck button clicks
     * @return registration for removing the listener
     */
    public Registration addDeleteDeckClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        return deleteDeckButton.addClickListener(listener);
    }

    /**
     * Initializes the component when attached to the UI.
     * Configures layout and components to avoid this-escape warnings.
     *
     * @param attachEvent the attachment event
     */
    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        configureLayout();
        configurePracticeButton();
        configureAddFlashcardButton();
        configureEditDeckButton();
        configureDeleteDeckButton();
        addComponents();
    }
}
