package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import org.apolenkov.application.views.practice.business.PracticeSessionManager;
import org.apolenkov.application.views.practice.constants.PracticeConstants;

/**
 * Progress component for practice view.
 * Displays current progress, statistics, and completion percentage.
 */
public final class PracticeProgress extends Composite<Div> {

    // UI Components
    private Span statsSpan;

    /**
     * Creates a new PracticeProgress component.
     */
    public PracticeProgress() {
        // Constructor - data only
    }

    @Override
    protected Div initContent() {
        Div progressSection = new Div();
        progressSection.addClassName(PracticeConstants.PRACTICE_PROGRESS_CLASS);

        statsSpan = new Span();
        statsSpan.addClassName(PracticeConstants.PRACTICE_PROGRESS_TEXT_CLASS);
        statsSpan.setText(getTranslation(PracticeConstants.PRACTICE_GET_READY_KEY));

        progressSection.add(statsSpan);
        return progressSection;
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
