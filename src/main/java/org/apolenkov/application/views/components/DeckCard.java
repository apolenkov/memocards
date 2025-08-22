package org.apolenkov.application.views.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import org.apolenkov.application.views.home.DeckCardViewModel;

/**
 * Reusable card component for displaying deck information.
 *
 * <p>This component renders a clickable card showing comprehensive deck details
 * including title, description, card count, progress indicators, and navigation
 * controls. It provides an intuitive interface for deck management and practice
 * initiation.</p>
 *
 * <p>The card features:</p>
 * <ul>
 *   <li>Deck title with card count information</li>
 *   <li>Descriptive text about the deck content</li>
 *   <li>Visual progress bar showing learning completion</li>
 *   <li>Detailed progress statistics (known/total cards)</li>
 *   <li>Practice button for immediate session start</li>
 *   <li>Click navigation to detailed deck view</li>
 * </ul>
 *
 * <p>The component automatically adapts its appearance based on the deck's
 * progress and provides consistent styling across the application.</p>
 */
public class DeckCard extends Div {

    private final transient DeckCardViewModel viewModel;

    /**
     * Constructs a new DeckCard with the specified view model.
     *
     * <p>Initializes the card with deck data and sets up the complete
     * user interface including layout, styling, and event handlers.
     * The card is immediately ready for display and interaction.</p>
     *
     * @param viewModel the view model containing deck data to display
     */
    public DeckCard(DeckCardViewModel viewModel) {
        this.viewModel = viewModel;

        add(buildContent());
        addClickListener(e -> navigateToDeck());
    }

    /**
     * Builds the complete card content and layout.
     *
     * <p>Creates and configures all visual elements including title,
     * description, progress indicators, and action buttons. The method
     * ensures proper spacing, alignment, and styling for consistent
     * appearance across all deck cards.</p>
     *
     * @return the main content component containing all card elements
     */
    private Component buildContent() {
        // Create main card layout with no padding or spacing for tight design
        VerticalLayout cardContent = new VerticalLayout();
        cardContent.setPadding(false);
        cardContent.setSpacing(false);

        // Apply consistent styling via theme CSS classes
        addClassName("deck-card");
        setWidthFull();

        // Create horizontal layout for title and icon with proper alignment
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Add deck icon with appropriate styling class
        Span icon = new Span(getTranslation("home.deckIcon"));
        icon.addClassName("deck-card__icon");

        // Create title with deck size information
        H3 title = new H3(viewModel.title() + " (" + viewModel.deckSize() + ")");
        title.addClassName("deck-card__title");

        titleLayout.add(icon, title);

        // Add description with styling class
        Span description = new Span(viewModel.description());
        description.addClassName("deck-card__description");

        // Build progress section with visual indicators
        HorizontalLayout progressLayout = buildProgress();

        // Create practice button with primary styling and navigation
        Button practiceButton = new Button(getTranslation("home.practice"));
        practiceButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        practiceButton.addClickListener(e -> navigateToPractice());
        practiceButton.addClassName("deck-card__practice-button");

        // Assemble all components in the main layout
        cardContent.add(titleLayout, description, progressLayout, practiceButton);
        return cardContent;
    }

    /**
     * Builds the progress visualization section.
     *
     * <p>Creates a comprehensive progress display including progress bar,
     * percentage text, and detailed statistics. The progress section
     * provides users with clear visual feedback about their learning
     * progress for the deck.</p>
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
        Span progressLabel = new Span(getTranslation("home.progress"));
        progressLabel.addClassName("deck-card__progress-label");

        // Create progress bar with normalized value (0.0 to 1.0)
        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(Math.clamp(percent / 100.0, 0.0, 1.0));
        progressBar.setWidthFull();
        layout.setFlexGrow(1, progressBar);

        // Display percentage text with suffix
        Span progressText = new Span(percent + getTranslation("home.percentSuffix"));
        progressText.addClassName("deck-card__progress-text");

        // Show detailed progress (e.g., "5 of 20 cards")
        Span progressDetails = new Span(getTranslation("home.progress.details", known, deckSize));
        progressDetails.addClassName("deck-card__progress-details");

        // Assemble progress components in horizontal layout
        layout.add(progressLabel, progressBar, progressText, progressDetails);
        return layout;
    }

    /**
     * Navigates to the detailed deck view.
     *
     * <p>Handles navigation to the deck detail page when the card is clicked.
     * Only navigates if a valid deck ID is available, ensuring robust
     * navigation behavior.</p>
     *
     * <p>This method is called automatically when the card receives a click
     * event, providing intuitive navigation for users exploring their decks.</p>
     */
    private void navigateToDeck() {
        if (viewModel.id() != null) {
            getUI().ifPresent(ui -> ui.navigate("deck/" + viewModel.id()));
        }
    }

    /**
     * Navigates to the practice view for this deck.
     *
     * <p>Handles navigation to the practice page when the practice button is clicked.
     * Only navigates if a valid deck ID is available, ensuring users can
     * immediately begin practicing with the selected deck.</p>
     *
     * <p>This method provides quick access to practice sessions, allowing
     * users to start learning without additional navigation steps.</p>
     */
    private void navigateToPractice() {
        if (viewModel.id() != null) {
            getUI().ifPresent(ui -> ui.navigate(
                    org.apolenkov.application.views.PracticeView.class,
                    viewModel.id().toString()));
        }
    }
}
