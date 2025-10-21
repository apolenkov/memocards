package org.apolenkov.application.views.deck.constants;

/**
 * Constants for deck components.
 * Centralizes all string constants used across deck components to avoid duplication
 * and ensure consistency.
 */
public final class DeckConstants {

    // Translation keys for deck operations
    public static final String DECKS_TITLE_KEY = "main.decks";
    public static final String FILL_REQUIRED_KEY = "dialog.fillRequired";
    public static final String DECK_CARDS_KEY = "deck.cards";
    public static final String DECK_INVALID_ID_KEY = "deck.invalidId";

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

    // Translation keys for card dialogs
    public static final String DECK_COL_FRONT = "deck.col.front";
    public static final String DECK_COL_BACK = "deck.col.back";
    public static final String DECK_EXAMPLE_OPTIONAL = "deck.example.optional";
    public static final String DECK_IMAGE_URL_OPTIONAL = "deck.imageUrl.optional";
    public static final String DECK_CARD_ADD_TITLE = "deck.card.addTitle";
    public static final String DECK_CARD_EDIT_TITLE = "deck.card.editTitle";
    public static final String DECK_CARD_UPDATED = "deck.card.updated";
    public static final String DECK_CARD_ADDED = "deck.card.added";
    public static final String DECK_CARD_DELETE_TITLE = "deck.card.deleteTitle";
    public static final String DECK_CARD_DELETE_DESCRIPTION = "deck.card.deleteDescription";
    public static final String DECK_CARD_DELETE_CONFIRM = "deck.card.deleteConfirm";
    public static final String DECK_CARD_DELETED = "deck.card.deleted";

    // Translation keys for common actions
    public static final String COMMON_CANCEL = "common.cancel";
    public static final String COMMON_BACK = "common.back";
    public static final String COMMON_EDIT = "common.edit";
    public static final String COMMON_DELETE = "common.delete";

    // Translation keys for dialog actions
    public static final String DIALOG_SAVE = "dialog.save";
    public static final String DIALOG_CREATE = "dialog.create";
    public static final String DIALOG_NEW_DECK = "dialog.newDeck";
    public static final String DIALOG_DESCRIPTION = "dialog.description";
    public static final String DIALOG_DESCRIPTION_PLACEHOLDER = "dialog.description.placeholder";

    // Translation keys for home components
    public static final String HOME_DECK_ICON = "home.deckIcon";
    public static final String HOME_PRACTICE = "home.practice";
    public static final String HOME_PROGRESS = "home.progress";
    public static final String HOME_PERCENT_SUFFIX = "home.percentSuffix";
    public static final String HOME_PROGRESS_DETAILS = "home.progress.details";
    public static final String HOME_ENTER_TITLE = "home.enterTitle";
    public static final String HOME_DECK_CREATED = "home.deckCreated";
    public static final String HOME_SEARCH_NO_RESULTS = "home.search.noResults";
    public static final String HOME_ADD_DECK = "home.addDeck";
    public static final String HOME_SEARCH_PLACEHOLDER = "home.search.placeholder";

    // Translation keys for deck operations
    public static final String DECK_START_SESSION = "deck.startSession";
    public static final String DECK_EDIT_TOOLTIP = "deck.edit.tooltip";
    public static final String DECK_LOADING = "deck.loading";
    public static final String DECK_NOT_FOUND = "deck.notFound";
    public static final String DECK_COUNT = "deck.count";
    public static final String DECK_COUNT_SHORT = "deck.count.short";
    public static final String DECK_MENU_PRACTICE = "deck.menu.practice";
    public static final String DECK_ADD_CARD = "deck.addCard";
    public static final String DECK_SEARCH_CARDS = "deck.searchCards";
    public static final String DECK_FILTER_LABEL = "deck.filter.label";
    public static final String DECK_FILTER_ALL = "deck.filter.all";
    public static final String DECK_FILTER_KNOWN = "deck.filter.known";
    public static final String DECK_FILTER_UNKNOWN = "deck.filter.unknown";
    public static final String DECK_RESET_PROGRESS = "deck.resetProgress";
    public static final String DECK_PROGRESS_RESET = "deck.progressReset";

    // Translation keys for deck creation
    public static final String DECK_CREATE_TITLE = "deckCreate.title";
    public static final String DECK_CREATE_BACK = "deckCreate.back";
    public static final String DECK_CREATE_SECTION = "deckCreate.section";
    public static final String DECK_CREATE_NAME = "deckCreate.name";
    public static final String DECK_CREATE_NAME_PLACEHOLDER = "deckCreate.name.placeholder";
    public static final String DECK_CREATE_DESCRIPTION = "deckCreate.description";
    public static final String DECK_CREATE_DESCRIPTION_PLACEHOLDER = "deckCreate.description.placeholder";
    public static final String DECK_CREATE_CREATE = "deckCreate.create";
    public static final String DECK_CREATE_CANCEL = "deckCreate.cancel";
    public static final String DECK_CREATE_CREATED = "deckCreate.created";
    public static final String DECK_CREATE_ERROR = "deckCreate.error";

    // Translation keys for deck deletion
    public static final String DECK_DELETE_SIMPLE_TITLE = "deck.delete.simpleTitle";
    public static final String DECK_DELETE_SIMPLE_DESCRIPTION = "deck.delete.simpleDescription";
    public static final String DECK_DELETE_SIMPLE_CONFIRM = "deck.delete.simpleConfirm";

    // CSS classes
    public static final String SURFACE_PANEL_CLASS = "surface-panel";
    public static final String CONTAINER_MD_CLASS = "container-md";
    public static final String DECK_VIEW_SECTION_CLASS = "deck-view__section";
    public static final String DECK_VIEW_TITLE_CLASS = "deck-view__title";

    // Dialog CSS classes
    public static final String DIALOG_MD_CLASS = "dialog-md";
    public static final String DIALOG_SM_CLASS = "dialog-sm";
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

    // Deck create CSS classes
    public static final String DECK_CREATE_FORM_CLASS = "deck-create__form";
    public static final String DECK_CREATE_FORM_LAYOUT_CLASS = "deck-create__form-layout";

    // Deck card CSS classes
    public static final String DECK_CARD_ICON_CLASS = "deck-card__icon";
    public static final String DECK_CARD_TITLE_CLASS = "deck-card__title";
    public static final String DECK_CARD_DESCRIPTION_CLASS = "deck-card__description";
    public static final String DECK_CARD_PRACTICE_BUTTON_CLASS = "deck-card__practice-button";
    public static final String DECK_CARD_PROGRESS_LABEL_CLASS = "deck-card__progress-label";
    public static final String DECK_CARD_PROGRESS_TEXT_CLASS = "deck-card__progress-text";
    public static final String DECK_CARD_PROGRESS_DETAILS_CLASS = "deck-card__progress-details";

    // Deck view CSS classes
    public static final String DECK_VIEW_STATS_CLASS = "deck-view__stats";
    public static final String DECK_VIEW_HEADER_CLASS = "deck-view__header";
    public static final String DECK_VIEW_ACTIONS_CLASS = "deck-view__actions";
    public static final String DECK_VIEW_BACK_BUTTON_CLASS = "deck-view__back-button";
    public static final String DECKS_VIEW_CLASS = "decks-view";
    public static final String DECKS_VIEW_CONTENT_CLASS = "decks-view__content";
    public static final String GLASS_MD_CLASS = "glass-md";

    // Common component CSS classes
    public static final String PAGE_TITLE_CLASS = "page-title";
    public static final String DECK_CENTERED_SECTION_CLASS = "deck-centered-section";
    public static final String DECK_HEADER_CENTER_CLASS = "deck-header-center";

    // Deck toolbar CSS classes
    public static final String DECK_TOOLBAR_SEARCH_CLASS = "deck-toolbar__search";
    public static final String DECK_TOOLBAR_ADD_BUTTON_CLASS = "deck-toolbar__add-button";

    // Deck deletion CSS classes
    public static final String DECK_DELETE_DIALOG_ICON_CLASS = "deck-delete-dialog__icon";
    public static final String DECK_DELETE_DIALOG_TITLE_CLASS = "deck-delete-dialog__title";
    public static final String DECK_DELETE_DIALOG_DESCRIPTION_CLASS = "deck-delete-dialog__description";
    public static final String DECK_DELETE_CONFIRM_WARNING_ICON_CLASS = "deck-delete-confirm__warning-icon";
    public static final String DECK_DELETE_CONFIRM_TITLE_CLASS = "deck-delete-confirm__title";
    public static final String DECK_DELETE_CONFIRM_DESCRIPTION_CLASS = "deck-delete-confirm__description";
    public static final String DECK_DELETE_CONFIRM_INFO_CLASS = "deck-delete-confirm__info";
    public static final String DECK_DELETE_CONFIRM_DECK_NAME_CLASS = "deck-delete-confirm__deck-name";
    public static final String DECK_DELETE_CONFIRM_CARD_COUNT_CLASS = "deck-delete-confirm__card-count";

    // Grid CSS classes
    public static final String DECK_GRID_SECTION_CLASS = "deck-grid-section";

    // Mobile menu CSS classes
    public static final String DECK_ACTIONS_MENU_CLASS = "deck-actions-menu";
    public static final String DECK_SEARCH_FIELD_CLASS = "deck-search-field";

    // HTML attributes
    public static final String TITLE_PROPERTY = "title";

    // Loading states
    public static final String DECK_LOADING_STATE = "deck.loading.state";

    // Menu actions for deck
    public static final String DECK_MENU_EDIT = "deck.menu.edit";
    public static final String DECK_MENU_DELETE = "deck.menu.delete";
    public static final String DECK_MENU_RESET = "deck.menu.resetProgress";

    // Menu actions for card
    public static final String CARD_MENU_EDIT = "card.menu.edit";
    public static final String CARD_MENU_TOGGLE = "card.menu.toggleKnown";
    public static final String CARD_MENU_DELETE = "card.menu.delete";

    // Accessibility
    public static final String MENU_ACTIONS_ARIA_LABEL = "menu.actions.ariaLabel";

    // Private constructor to prevent instantiation
    private DeckConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
