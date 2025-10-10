package org.apolenkov.application.views.deck.components.list;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import org.apolenkov.application.views.deck.business.DeckCardViewModel;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;

/**
 * Reusable card component for displaying deck information.
 * Renders a clickable card showing comprehensive deck details including title,
 * description, card count, progress indicators, and navigation controls.
 */
public final class DeckCard extends Composite<Div> {

    private final transient DeckCardViewModel viewModel;

    /**
     * Creates a new DeckCard with the specified view model.
     *
     * @param model the view model containing deck data to display
     */
    public DeckCard(final DeckCardViewModel model) {
        this.viewModel = model;
    }

    @Override
    protected Div initContent() {
        Div card = new Div();
        card.addClassName(DeckConstants.DECK_CARD_CLASS);
        card.setWidthFull();
        card.add(buildContent());
        card.addClickListener(e -> navigateToDeck());
        return card;
    }

    /**
     * Builds the complete card content and layout.
     * Creates and configures all visual elements including title, description,
     * progress indicators, and action buttons.
     *
     * @return the main content component containing all card elements
     */
    private VerticalLayout buildContent() {
        // Create main card layout with no padding or spacing for tight design
        VerticalLayout cardContent = new VerticalLayout();
        cardContent.setPadding(false);
        cardContent.setSpacing(false);

        // Create horizontal layout for title and icon with proper alignment
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Add deck icon with appropriate styling class
        Span icon = new Span(getTranslation(DeckConstants.HOME_DECK_ICON));
        icon.addClassName(DeckConstants.DECK_CARD_ICON_CLASS);

        // Create title with deck size information
        H3 title = new H3(viewModel.title() + " (" + viewModel.deckSize() + ")");
        title.addClassName(DeckConstants.DECK_CARD_TITLE_CLASS);

        titleLayout.add(icon, title);

        // Add description with styling class
        Span description = new Span(viewModel.description());
        description.addClassName(DeckConstants.DECK_CARD_DESCRIPTION_CLASS);

        // Build progress section with visual indicators
        HorizontalLayout progressLayout = buildProgress();

        // Create practice button with primary styling and navigation
        Button practiceButton = ButtonHelper.createButton(
                getTranslation(DeckConstants.HOME_PRACTICE),
                e -> navigateToPractice(),
                ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_PRIMARY);
        practiceButton.addClassName(DeckConstants.DECK_CARD_PRACTICE_BUTTON_CLASS);

        // Assemble all components in the main layout
        cardContent.add(titleLayout, description, progressLayout, practiceButton);
        return cardContent;
    }

    /**
     * Builds the progress visualization section.
     * Creates a comprehensive progress display including progress bar,
     * percentage text, and detailed statistics.
     *
     * @return a horizontal layout containing all progress indicators
     */
    private HorizontalLayout buildProgress() {
        // Create horizontal layout for progress indicators with proper spacing
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setWidthFull();

        // Extract progress data from view model
        int deckSize = viewModel.deckSize();
        int known = viewModel.knownCount();
        int percent = viewModel.progressPercent();

        // Create progress label with appropriate styling
        Span progressLabel = new Span(getTranslation(DeckConstants.HOME_PROGRESS));
        progressLabel.addClassName(DeckConstants.DECK_CARD_PROGRESS_LABEL_CLASS);

        // Create progress bar with normalized value (0.0 to 1.0)
        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(Math.clamp(percent / 100.0, 0.0, 1.0));
        progressBar.setWidthFull();
        layout.setFlexGrow(1, progressBar);

        // Display percentage text with suffix
        Span progressText = new Span(percent + getTranslation(DeckConstants.HOME_PERCENT_SUFFIX));
        progressText.addClassName(DeckConstants.DECK_CARD_PROGRESS_TEXT_CLASS);

        // Show detailed progress (e.g., "5 of 20 cards")
        Span progressDetails = new Span(getTranslation(DeckConstants.HOME_PROGRESS_DETAILS, known, deckSize));
        progressDetails.addClassName(DeckConstants.DECK_CARD_PROGRESS_DETAILS_CLASS);

        // Assemble progress components in horizontal layout
        layout.add(progressLabel, progressBar, progressText, progressDetails);
        return layout;
    }

    /**
     * Navigates to the detailed deck view.
     * Handles navigation to the deck detail page when the card is clicked.
     */
    private void navigateToDeck() {
        if (viewModel.id() != null) {
            NavigationHelper.navigateToDeck(viewModel.id());
        }
    }

    /**
     * Navigates to the practice view for this deck.
     * Handles navigation to the practice page when the practice button is clicked.
     */
    private void navigateToPractice() {
        if (viewModel.id() != null) {
            NavigationHelper.navigateToPractice(viewModel.id());
        }
    }
}
