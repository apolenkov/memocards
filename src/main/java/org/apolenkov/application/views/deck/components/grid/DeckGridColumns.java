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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;

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
        grid.addColumn(
                flashcard -> {
                    String example = flashcard.getExample();
                    return example != null && !example.trim().isEmpty() ? example : "-";
                })
                .setHeader(grid.getTranslation(DeckConstants.DECK_COL_EXAMPLE))
                .setFlexGrow(2);
    }

    /**
     * Adds the status column to the grid.
     *
     * @param grid the grid to add column to
     * @param knownCardIdsSupplier supplier for known card IDs (called once per render)
     */
    public static void addStatusColumn(final Grid<Flashcard> grid, final Supplier<Set<Long>> knownCardIdsSupplier) {
        grid.addComponentColumn(flashcard -> createStatusComponent(flashcard, knownCardIdsSupplier))
                .setHeader(grid.getTranslation(DeckConstants.DECK_COL_STATUS))
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(1);
    }

    /**
     * Creates a status component for a flashcard.
     * Checks if card is known using preloaded Set to avoid database queries.
     *
     * @param flashcard the flashcard to create status for
     * @param knownCardIdsSupplier supplier for known card IDs
     * @return the status component
     */
    private static Span createStatusComponent(
            final Flashcard flashcard, final Supplier<Set<Long>> knownCardIdsSupplier) {
        Set<Long> knownCardIds = knownCardIdsSupplier.get();
        boolean known = knownCardIds.contains(flashcard.getId());
        if (known) {
            Span statusSpan = new Span("✓");
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
        grid.addComponentColumn(flashcard ->
                        createActionsComponent(flashcard, editCallback, toggleCallback, deleteCallback, grid))
                .setKey(DeckConstants.ACTIONS_COLUMN_KEY)
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
     * @param grid the grid instance for translations
     * @return the actions component
     */
    private static HorizontalLayout createActionsComponent(
            final Flashcard flashcard,
            final Consumer<Flashcard> editCallback,
            final Consumer<Flashcard> toggleCallback,
            final Consumer<Flashcard> deleteCallback,
            final Grid<Flashcard> grid) {
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
        editButton
                .getElement()
                .setProperty(DeckConstants.TITLE_PROPERTY, grid.getTranslation(DeckConstants.GRID_EDIT_TOOLTIP));

        Button toggleKnown = ButtonHelper.createIconButton(
                VaadinIcon.CHECK,
                e -> Optional.ofNullable(toggleCallback).ifPresent(cb -> cb.accept(flashcard)),
                ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_SUCCESS);
        toggleKnown
                .getElement()
                .setProperty(DeckConstants.TITLE_PROPERTY, grid.getTranslation(DeckConstants.GRID_TOGGLE_TOOLTIP));

        Button deleteButton = ButtonHelper.createIconButton(
                VaadinIcon.TRASH,
                e -> Optional.ofNullable(deleteCallback).ifPresent(cb -> cb.accept(flashcard)),
                ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_ERROR);
        deleteButton
                .getElement()
                .setProperty(DeckConstants.TITLE_PROPERTY, grid.getTranslation(DeckConstants.GRID_DELETE_TOOLTIP));

        actions.add(editButton, toggleKnown, deleteButton);
        return actions;
    }
}
