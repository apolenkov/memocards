package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apolenkov.application.views.practice.constants.PracticeConstants;
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
    }

    @Override
    protected HorizontalLayout initContent() {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);

        backButton = createBackButton();
        deckTitle.addClassName(PracticeConstants.PRACTICE_VIEW_DECK_TITLE_CLASS);

        leftSection.add(backButton, deckTitle);
        headerLayout.add(leftSection);

        return headerLayout;
    }

    /**
     * Creates the back button.
     *
     * @return configured back button
     */
    private Button createBackButton() {
        return ButtonHelper.createButton(
                getTranslation(PracticeConstants.COMMON_BACK_KEY),
                VaadinIcon.ARROW_LEFT,
                e -> {
                    /* Handler set via setBackButtonHandler() */
                },
                ButtonVariant.LUMO_TERTIARY);
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
        if (backButton != null) {
            backButton.addClickListener(e -> clickHandler.run());
        }
    }
}
