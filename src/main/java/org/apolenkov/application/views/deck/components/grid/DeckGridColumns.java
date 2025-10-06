package org.apolenkov.application.views.deck.components.grid;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.Optional;
import java.util.function.Consumer;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.views.deck.components.DeckConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.TextFormattingUtils;

/**
 * Utility class for creating grid columns in the flashcard grid.
 * Provides methods to add different types of columns to the grid.
 */
public final class DeckGridColumns {

    private DeckGridColumns() {
        // Utility class
    }

    /**
     * Adds the front text column to the grid.
     *
     * @param grid the grid to add column to
     */
    public static void addFrontColumn(final Grid<Flashcard> grid) {
        grid.addColumn(Flashcard::getFrontText)
                .setHeader(grid.getTranslation(DeckConstants.DECK_COL_FRONT))
                .setFlexGrow(2);
    }

    /**
     * Adds the example column to the grid.
     *
     * @param grid the grid to add column to
     */
    public static void addExampleColumn(final Grid<Flashcard> grid) {
        grid.addColumn(flashcard -> TextFormattingUtils.formatPlaceholder(flashcard.getExample()))
                .setHeader(grid.getTranslation(DeckConstants.DECK_COL_EXAMPLE))
                .setFlexGrow(2);
    }

    /**
     * Adds the status column to the grid.
     *
     * @param grid the grid to add column to
     * @param statsService service for statistics tracking
     * @param currentDeckId current deck ID
     */
    public static void addStatusColumn(
            final Grid<Flashcard> grid, final StatsService statsService, final Long currentDeckId) {
        grid.addComponentColumn(flashcard -> createStatusComponent(flashcard, statsService, currentDeckId))
                .setHeader(grid.getTranslation(DeckConstants.DECK_COL_STATUS))
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(1);
    }

    /**
     * Creates a status component for a flashcard.
     *
     * @param flashcard the flashcard to create status for
     * @param statsService service for statistics tracking
     * @param currentDeckId current deck ID
     * @return the status component
     */
    private static Span createStatusComponent(
            final Flashcard flashcard, final StatsService statsService, final Long currentDeckId) {
        boolean known = currentDeckId != null && statsService.isCardKnown(currentDeckId, flashcard.getId());
        if (known) {
            Span statusSpan = new Span("✓"); // Simple checkmark instead of translation
            statusSpan.addClassName(DeckConstants.KNOWN_STATUS_CLASS);
            return statusSpan;
        } else {
            return new Span("-");
        }
    }

    /**
     * Adds the actions column to the grid.
     *
     * @param grid the grid to add column to
     * @param editCallback callback for edit action
     * @param toggleCallback callback for toggle known action
     * @param deleteCallback callback for delete action
     */
    public static void addActionsColumn(
            final Grid<Flashcard> grid,
            final Consumer<Flashcard> editCallback,
            final Consumer<Flashcard> toggleCallback,
            final Consumer<Flashcard> deleteCallback) {
        grid.addComponentColumn(
                        flashcard -> createActionsComponent(flashcard, editCallback, toggleCallback, deleteCallback))
                .setKey("actions")
                .setHeader(grid.getTranslation(DeckConstants.DECK_COL_ACTIONS))
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setAutoWidth(true);
    }

    /**
     * Creates an actions component for a flashcard.
     *
     * @param flashcard the flashcard to create actions for
     * @param editCallback callback for edit action
     * @param toggleCallback callback for toggle known action
     * @param deleteCallback callback for delete action
     * @return the actions component
     */
    private static HorizontalLayout createActionsComponent(
            final Flashcard flashcard,
            final Consumer<Flashcard> editCallback,
            final Consumer<Flashcard> toggleCallback,
            final Consumer<Flashcard> deleteCallback) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(false);
        actions.setPadding(false);
        actions.addClassName(DeckConstants.ACTIONS_LAYOUT_CLASS);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Button editButton = ButtonHelper.createIconButton(
                VaadinIcon.EDIT,
                e -> Optional.ofNullable(editCallback).ifPresent(cb -> cb.accept(flashcard)),
                ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_TERTIARY);
        editButton.getElement().setProperty(DeckConstants.TITLE_PROPERTY, "Edit");

        Button toggleKnown = ButtonHelper.createIconButton(
                VaadinIcon.CHECK,
                e -> Optional.ofNullable(toggleCallback).ifPresent(cb -> cb.accept(flashcard)),
                ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_SUCCESS);
        toggleKnown.getElement().setProperty(DeckConstants.TITLE_PROPERTY, "Toggle Known");

        Button deleteButton = ButtonHelper.createIconButton(
                VaadinIcon.TRASH,
                e -> Optional.ofNullable(deleteCallback).ifPresent(cb -> cb.accept(flashcard)),
                ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_ERROR);
        deleteButton.getElement().setProperty(DeckConstants.TITLE_PROPERTY, "Delete");

        actions.add(editButton, toggleKnown, deleteButton);
        return actions;
    }
}
