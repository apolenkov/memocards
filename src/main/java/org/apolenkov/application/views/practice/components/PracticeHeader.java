package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apolenkov.application.views.shared.utils.ButtonHelper;

/**
 * Header component for practice view.
 * Displays deck title, back button, and navigation controls.
 */
public final class PracticeHeader extends Composite<HorizontalLayout> {

    // UI Components
    private final H2 deckTitle;
    private Button backButton;

    /**
     * Creates a new PracticeHeader component.
     */
    public PracticeHeader() {
        this.deckTitle = new H2();
        setupLayout();
    }

    /**
     * Sets up the header layout with back button and title.
     */
    private void setupLayout() {
        HorizontalLayout headerLayout = getContent();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);

        Button backBtn = createBackButton();
        deckTitle.addClassName(PracticeConstants.PRACTICE_VIEW_DECK_TITLE_CLASS);

        leftSection.add(backBtn, deckTitle);
        headerLayout.add(leftSection);
    }

    /**
     * Creates the back button with navigation logic.
     *
     * @return configured back button
     */
    private Button createBackButton() {
        this.backButton = ButtonHelper.createButton(
                getTranslation(PracticeConstants.COMMON_BACK_KEY),
                VaadinIcon.ARROW_LEFT,
                e -> {
                    // Navigation logic will be handled by parent component
                    // This is a placeholder for now
                },
                ButtonVariant.LUMO_TERTIARY);
        return backButton;
    }

    /**
     * Updates the deck title display.
     *
     * @param deckTitleText the title text to display
     * @throws IllegalArgumentException if title is null or blank
     */
    public void setDeckTitle(final String deckTitleText) {
        if (deckTitleText == null || deckTitleText.isBlank()) {
            throw new IllegalArgumentException("Deck title cannot be null or blank");
        }
        deckTitle.setText(deckTitleText);
    }

    /**
     * Sets the click handler for the back button.
     *
     * @param clickHandler the click handler for back navigation
     * @throws IllegalArgumentException if clickHandler is null
     */
    public void setBackButtonHandler(final Runnable clickHandler) {
        if (clickHandler == null) {
            throw new IllegalArgumentException("Click handler cannot be null");
        }
        // Recreate button with new handler
        backButton = ButtonHelper.createButton(
                getTranslation(PracticeConstants.COMMON_BACK_KEY),
                VaadinIcon.ARROW_LEFT,
                e -> clickHandler.run(),
                ButtonVariant.LUMO_TERTIARY);

        // Update the layout
        HorizontalLayout headerLayout = getContent();
        headerLayout.removeAll();

        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);
        leftSection.add(backButton, deckTitle);
        headerLayout.add(leftSection);
    }
}
