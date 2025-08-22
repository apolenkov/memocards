package org.apolenkov.application.views.home;

/**
 * View model for displaying deck information in card components.
 *
 * <p>This record encapsulates all the data needed to render a deck card,
 * including basic deck information, progress statistics, and calculated
 * metrics for the user interface.</p>
 *
 * @param id the unique identifier of the deck
 * @param title the display name of the deck
 * @param description the descriptive text for the deck
 * @param deckSize the total number of flashcards in the deck
 * @param knownCount the number of cards the user has marked as known
 * @param progressPercent the calculated progress percentage (0-100)
 */
public record DeckCardViewModel(
        Long id, String title, String description, int deckSize, int knownCount, int progressPercent) {}
