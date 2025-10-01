package org.apolenkov.application.views.deck.components;

/**
 * Constants for deck components.
 * Centralizes all string constants used across deck components to avoid duplication
 * and ensure consistency.
 */
public final class DeckConstants {

    // Translation keys for deck operations
    public static final String DECKS_TITLE_KEY = "main.decks";
    public static final String FILL_REQUIRED_KEY = "dialog.fillRequired";

    // Translation keys for deck dialogs
    public static final String DECK_EDIT_TITLE = "deck.edit.title";
    public static final String DECK_DECK_TITLE = "dialog.deckTitle";
    public static final String DECK_DESCRIPTION = "dialog.description";
    public static final String DECK_DESCRIPTION_PLACEHOLDER = "dialog.description.placeholder";
    public static final String DECK_CREATE_ENTER_TITLE = "deckCreate.enterTitle";
    public static final String DECK_EDIT_SAVE = "deck.edit.save";
    public static final String DECK_EDIT_SUCCESS = "deck.edit.success";

    // Translation keys for deck deletion
    public static final String DECK_DELETE_CONFIRM_TITLE = "deck.delete.confirmTitle";
    public static final String DECK_DELETE_CONFIRM_DESCRIPTION = "deck.delete.confirmDescription";
    public static final String DECK_DELETE_CARD_COUNT = "deck.delete.cardCount";
    public static final String DECK_DELETE_CONFIRM_INPUT = "deck.delete.confirmInput";
    public static final String DECK_DELETE_CONFIRM = "deck.delete.confirm";
    public static final String DECK_DELETE_SUCCESS = "deck.delete.success";
    public static final String DECK_DELETE_CONFIRMATION_MISMATCH = "deck.delete.confirmationMismatch";

    // Translation keys for flashcard dialogs
    public static final String DECK_COL_FRONT = "deck.col.front";
    public static final String DECK_COL_BACK = "deck.col.back";
    public static final String DECK_EXAMPLE_OPTIONAL = "deck.example.optional";
    public static final String DECK_IMAGE_URL_OPTIONAL = "deck.imageUrl.optional";
    public static final String DECK_CARD_ADD_TITLE = "deck.card.addTitle";
    public static final String DECK_CARD_EDIT_TITLE = "deck.card.editTitle";
    public static final String DECK_CARD_UPDATED = "deck.card.updated";
    public static final String DECK_CARD_ADDED = "deck.card.added";

    // Translation keys for common actions
    public static final String COMMON_CANCEL = "common.cancel";
    public static final String COMMON_BACK = "common.back";
    public static final String COMMON_START = "common.start";
    public static final String COMMON_EDIT = "common.edit";
    public static final String COMMON_DELETE = "common.delete";

    // Translation keys for dialog actions
    public static final String DIALOG_SAVE = "dialog.save";
    public static final String DIALOG_CREATE = "dialog.create";

    // CSS classes
    public static final String SURFACE_PANEL_CLASS = "surface-panel";
    public static final String CONTAINER_MD_CLASS = "container-md";
    public static final String DECK_VIEW_SECTION_CLASS = "deck-view__section";
    public static final String DECK_VIEW_TITLE_CLASS = "deck-view__title";

    // Dialog CSS classes
    public static final String DIALOG_MD_CLASS = "dialog-md";
    public static final String TEXT_AREA_MD_CLASS = "text-area--md";
    public static final String TEXT_AREA_SM_CLASS = "text-area--sm";

    // Deck deletion CSS classes

    // Deck card CSS classes
    public static final String DECK_CARD_CLASS = "deck-card";

    // Deck toolbar CSS classes
    public static final String DECK_TOOLBAR_CLASS = "deck-toolbar";

    // Deck list CSS classes
    public static final String DECK_LIST_CLASS = "deck-list";
    public static final String DECKS_EMPTY_MESSAGE_CLASS = "decks-empty-message";
    public static final String DECKS_SECTION_CLASS = "decks-section";
    public static final String DECKS_VIEW_TITLE_CLASS = "decks-view__title";

    // Deck create CSS classes
    public static final String DECK_CREATE_FORM_CLASS = "deck-create__form";
    public static final String DECK_CREATE_FORM_LAYOUT_CLASS = "deck-create__form-layout";

    // Grid CSS classes
    public static final String ACTIONS_LAYOUT_CLASS = "actions-layout";
    public static final String KNOWN_STATUS_CLASS = "known-status";

    // HTML attributes
    public static final String TITLE_PROPERTY = "title";

    // Private constructor to prevent instantiation
    private DeckConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
