package org.apolenkov.application.views.stats.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import java.util.List;
import java.util.Map;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.stats.constants.StatsConstants;

/**
 * UI component for creating deck pagination with navigation controls.
 * Extends Composite to access translation methods directly.
 */
public final class DeckPaginationBuilder extends Composite<VerticalLayout> {

    // Data
    private final transient List<Deck> decks;
    private final transient Map<Long, StatsRepository.DeckAggregate> aggregates;

    // State
    private int currentDeckIndex = 0;

    // Event Registrations
    private Registration prevButtonListenerRegistration;
    private Registration nextButtonListenerRegistration;

    /**
     * Creates a new DeckPaginationBuilder with required dependencies.
     *
     * @param decksParam list of user's decks
     * @param aggregatesParam aggregated statistics for all decks
     */
    public DeckPaginationBuilder(
            final List<Deck> decksParam, final Map<Long, StatsRepository.DeckAggregate> aggregatesParam) {
        this.decks = decksParam;
        this.aggregates = aggregatesParam;
    }

    @Override
    protected VerticalLayout initContent() {
        return createDeckPagination();
    }

    /**
     * Creates deck pagination with navigation controls.
     *
     * @return configured vertical layout with pagination
     */
    private VerticalLayout createDeckPagination() {
        VerticalLayout paginationContainer = new VerticalLayout();
        paginationContainer.setSpacing(true);
        paginationContainer.setAlignItems(FlexComponent.Alignment.CENTER);

        // Create navigation controls
        HorizontalLayout navigationLayout = createNavigationControls();
        Div currentDeckContainer = createDeckContainer();

        // Setup pagination logic
        setupPaginationLogic(navigationLayout, currentDeckContainer);

        paginationContainer.add(navigationLayout, currentDeckContainer);
        return paginationContainer;
    }

    /**
     * Creates navigation controls for deck pagination.
     *
     * @return configured navigation layout
     */
    private HorizontalLayout createNavigationControls() {
        HorizontalLayout navigationLayout = new HorizontalLayout();
        navigationLayout.setSpacing(true);
        navigationLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        navigationLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Button prevButton = ButtonHelper.createIconButton(
                VaadinIcon.CHEVRON_LEFT, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        prevButton
                .getElement()
                .setAttribute(StatsConstants.TITLE_ATTRIBUTE, getTranslation(StatsConstants.STATS_PREVIOUS_DECK_KEY));

        Button nextButton = ButtonHelper.createIconButton(
                VaadinIcon.CHEVRON_RIGHT, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        nextButton
                .getElement()
                .setAttribute(StatsConstants.TITLE_ATTRIBUTE, getTranslation(StatsConstants.STATS_NEXT_DECK_KEY));

        Span pageIndicator = new Span();
        pageIndicator.addClassName(StatsConstants.STATS_PAGINATION_INDICATOR_CLASS);

        navigationLayout.add(prevButton, pageIndicator, nextButton);
        return navigationLayout;
    }

    /**
     * Creates container for current deck display.
     *
     * @return configured deck container
     */
    private Div createDeckContainer() {
        Div currentDeckContainer = new Div();
        currentDeckContainer.setWidthFull();
        currentDeckContainer.addClassName(StatsConstants.STATS_CURRENT_DECK_CONTAINER_CLASS);
        return currentDeckContainer;
    }

    /**
     * Sets up pagination logic and event handlers.
     *
     * @param navigationLayout layout with navigation buttons
     * @param currentDeckContainer container for deck display
     */
    private void setupPaginationLogic(final HorizontalLayout navigationLayout, final Div currentDeckContainer) {
        // Find components
        Button prevButton = (Button) navigationLayout
                .getChildren()
                .filter(Button.class::isInstance)
                .findFirst()
                .orElse(null);

        Button nextButton = (Button) navigationLayout
                .getChildren()
                .filter(Button.class::isInstance)
                .skip(1)
                .findFirst()
                .orElse(null);

        Span pageIndicator = (Span) navigationLayout
                .getChildren()
                .filter(Span.class::isInstance)
                .findFirst()
                .orElse(null);

        // Update display function
        Runnable updateDisplay = () -> updateDeckDisplay(currentDeckContainer, pageIndicator, prevButton, nextButton);

        // Button click handlers
        if (prevButton != null) {
            prevButtonListenerRegistration = prevButton.addClickListener(e -> {
                if (currentDeckIndex > 0) {
                    currentDeckIndex--;
                    updateDisplay.run();
                }
            });
        }

        if (nextButton != null) {
            nextButtonListenerRegistration = nextButton.addClickListener(e -> {
                if (currentDeckIndex < decks.size() - 1) {
                    currentDeckIndex++;
                    updateDisplay.run();
                }
            });
        }

        // Initial display
        updateDisplay.run();
    }

    /**
     * Cleans up event listeners when the component is detached.
     * Prevents memory leaks by removing event listener registrations.
     *
     * @param detachEvent the detach event
     */
    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        if (prevButtonListenerRegistration != null) {
            prevButtonListenerRegistration.remove();
            prevButtonListenerRegistration = null;
        }
        if (nextButtonListenerRegistration != null) {
            nextButtonListenerRegistration.remove();
            nextButtonListenerRegistration = null;
        }
        super.onDetach(detachEvent);
    }

    /**
     * Updates deck display with current deck information.
     *
     * @param container container to update
     * @param pageIndicator indicator to update
     * @param prevButton previous button to enable/disable
     * @param nextButton next button to enable/disable
     */
    private void updateDeckDisplay(
            final Div container, final Span pageIndicator, final Button prevButton, final Button nextButton) {

        if (decks.isEmpty()) {
            container.removeAll();
            container.add(new Span(getTranslation(StatsConstants.STATS_NO_DECKS_KEY)));
            if (pageIndicator != null) {
                pageIndicator.setText("");
            }
            if (prevButton != null) {
                prevButton.setEnabled(false);
            }
            if (nextButton != null) {
                nextButton.setEnabled(false);
            }
            return;
        }

        container.removeAll();
        Deck currentDeck = decks.get(currentDeckIndex);
        var stats =
                aggregates.getOrDefault(currentDeck.getId(), new StatsRepository.DeckAggregate(0, 0, 0, 0, 0, 0, 0, 0));
        container.add(new DeckStatCard(currentDeck, stats));

        // Update page indicator
        if (pageIndicator != null) {
            pageIndicator.setText(
                    getTranslation(StatsConstants.STATS_DECK_PAGE_KEY, currentDeckIndex + 1, decks.size()));
        }

        // Update button states
        if (prevButton != null) {
            prevButton.setEnabled(currentDeckIndex > 0);
        }
        if (nextButton != null) {
            nextButton.setEnabled(currentDeckIndex < decks.size() - 1);
        }
    }
}
