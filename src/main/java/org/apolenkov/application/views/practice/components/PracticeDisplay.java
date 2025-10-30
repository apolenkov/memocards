package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apolenkov.application.views.practice.business.PracticeSessionManager;
import org.apolenkov.application.views.practice.constants.PracticeConstants;

/**
 * Unified display component for practice view header and progress.
 * Combines title, back button, and progress statistics in single component for simpler code structure.
 *
 * <p>Features:
 * <ul>
 *   <li>Deck title display</li>
 *   <li>Back navigation button</li>
 *   <li>Progress statistics with completion percentage</li>
 * </ul>
 */
public final class PracticeDisplay extends Composite<VerticalLayout> {

    // Header components
    private final Button backButton;
    private final H2 deckTitle;

    // Progress components
    private final Div progressSection;
    private final Span progressStats;

    /**
     * Creates a new PracticeDisplay component.
     */
    public PracticeDisplay() {
        this.backButton = new Button();
        this.deckTitle = new H2();
        this.progressSection = new Div();
        this.progressStats = new Span();
    }

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout container = new VerticalLayout();
        container.setSpacing(true);
        container.setPadding(false);
        container.setWidthFull();
        container.setAlignItems(FlexComponent.Alignment.CENTER);

        // Header row: [← Back] [Deck Title]
        HorizontalLayout headerRow = createHeaderRow();

        // Progress section
        Div progressDisplay = createProgressSection();

        container.add(headerRow, progressDisplay);
        return container;
    }

    /**
     * Creates header row with back button and title.
     *
     * @return configured header row
     */
    private HorizontalLayout createHeaderRow() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);

        configureBackButton();
        configureTitle();

        leftSection.add(backButton, deckTitle);
        header.add(leftSection);

        return header;
    }

    /**
     * Creates progress section with statistics.
     *
     * @return configured progress section
     */
    private Div createProgressSection() {
        progressSection.addClassName(PracticeConstants.PRACTICE_PROGRESS_CLASS);

        progressStats.addClassName(PracticeConstants.PRACTICE_PROGRESS_TEXT_CLASS);
        progressStats.setText(getTranslation(PracticeConstants.PRACTICE_GET_READY_KEY));

        progressSection.add(progressStats);
        return progressSection;
    }

    /**
     * Configures the back navigation button.
     */
    private void configureBackButton() {
        backButton.setText(getTranslation(PracticeConstants.COMMON_BACK_KEY));
        backButton.setIcon(VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    }

    /**
     * Configures the deck title display.
     */
    private void configureTitle() {
        deckTitle.addClassName(PracticeConstants.PRACTICE_VIEW_DECK_TITLE_CLASS);
    }

    // ==================== Public API ====================

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
        backButton.addClickListener(e -> clickHandler.run());
    }

    /**
     * Updates the progress display with current session statistics.
     *
     * @param progress the current progress information
     * @throws IllegalArgumentException if progress is null
     */
    public void updateProgress(final PracticeSessionManager.Progress progress) {
        if (progress == null) {
            throw new IllegalArgumentException("Progress cannot be null");
        }

        // Calculate progress based on completed cards, not current position
        int completedCards = progress.totalViewed();
        int totalCards = progress.total();
        int percent = totalCards > 0 ? Math.round((float) completedCards / totalCards * 100) : 0;

        progressStats.setText(getTranslation(
                PracticeConstants.PRACTICE_PROGRESS_LINE_KEY,
                progress.current(),
                progress.total(),
                progress.totalViewed(),
                progress.correct(),
                progress.hard(),
                percent));

        // Ensure progress is visible when updated
        progressSection.setVisible(true);
    }

    /**
     * Shows the progress section.
     * Used when returning to practice mode from other states.
     */
    public void showProgress() {
        progressSection.setVisible(true);
    }
}
