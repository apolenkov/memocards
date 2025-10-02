package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import org.apolenkov.application.views.practice.business.PracticeSessionManager;

/**
 * Progress component for practice view.
 * Displays current progress, statistics, and completion percentage.
 */
public final class PracticeProgress extends Composite<Div> {

    // UI Components
    private final Span statsSpan;

    /**
     * Creates a new PracticeProgress component.
     */
    public PracticeProgress() {
        this.statsSpan = new Span();
        setupLayout();
    }

    /**
     * Sets up the progress layout.
     */
    private void setupLayout() {
        Div progressSection = getContent();
        progressSection.addClassName(PracticeConstants.PRACTICE_PROGRESS_CLASS);

        statsSpan.addClassName(PracticeConstants.PRACTICE_PROGRESS_TEXT_CLASS);
        statsSpan.setText(getTranslation(PracticeConstants.PRACTICE_GET_READY_KEY));

        progressSection.add(statsSpan);
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

        statsSpan.setText(getTranslation(
                PracticeConstants.PRACTICE_PROGRESS_LINE_KEY,
                progress.current(),
                progress.total(),
                progress.totalViewed(),
                progress.correct(),
                progress.hard(),
                percent));
    }
}
