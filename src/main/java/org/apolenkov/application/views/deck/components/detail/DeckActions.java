package org.apolenkov.application.views.deck.components.detail;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component for deck action buttons including practice, add, edit and delete.
 * Provides a consistent layout for action buttons with proper styling.
 */
public final class DeckActions extends Composite<HorizontalLayout> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckActions.class);

    // UI Components
    private final Button practiceButton;
    private final Button editDeckButton;
    private final Button deleteDeckButton;

    /**
     * Creates a new DeckActions component.
     * Initializes UI components without configuring them to avoid this-escape warnings.
     */
    public DeckActions() {
        this.practiceButton = new Button();
        this.editDeckButton = new Button();
        this.deleteDeckButton = new Button();
    }

    @Override
    protected HorizontalLayout initContent() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);

        configurePracticeButton();
        configureEditDeckButton();
        configureDeleteDeckButton();

        actions.add(practiceButton, editDeckButton, deleteDeckButton);
        return actions;
    }

    /**
     * Configures the practice button.
     * Sets up button with proper styling and icon.
     */
    private void configurePracticeButton() {
        practiceButton.setText(getTranslation(DeckConstants.COMMON_START));
        practiceButton.setIcon(VaadinIcon.PLAY.create());
        practiceButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        practiceButton.setText(getTranslation(DeckConstants.DECK_START_SESSION));
    }

    /**
     * Configures the edit deck button.
     * Sets up button with proper styling and tooltip.
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
     * Sets up button with proper styling and error variant.
     */
    private void configureDeleteDeckButton() {
        deleteDeckButton.setText(getTranslation(DeckConstants.COMMON_DELETE));
        deleteDeckButton.setIcon(VaadinIcon.TRASH.create());
        deleteDeckButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
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
