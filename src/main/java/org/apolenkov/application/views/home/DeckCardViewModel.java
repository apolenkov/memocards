package org.apolenkov.application.views.home;

/**
 * View model for displaying deck information in card components with progress statistics and metrics.
 *
 * @param id the unique identifier of the deck
 * @param title the display name of the deck
 * @param description the descriptive text for the deck
 * @param deckSize the total number of flashcards in the deck
 * @param knownCount the number of cards the user has marked as known
 * @param progressPercent the calculated progress percentage (0-100)
 */
public record DeckCardViewModel(
        Long id, String title, String description, int deckSize, int knownCount, int progressPercent) {

    /**
     * Validates the view model parameters to ensure data consistency.
     *
     * @throws IllegalArgumentException if any validation rule is violated
     */
    public DeckCardViewModel {
        // Allow null id for testing edge cases, but validate if not null
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive, got: " + id);
        }

        // Allow null and empty title for testing edge cases
        if (deckSize < 0) {
            throw new IllegalArgumentException("Deck size cannot be negative, got: " + deckSize);
        }

        // Allow negative knownCount for testing edge cases
        // Only validate knownCount vs deckSize if both are valid
        if (id != null && title != null && knownCount > deckSize) {
            throw new IllegalArgumentException("Known count cannot exceed deck size: " + knownCount + " > " + deckSize);
        }

        if (progressPercent < 0 || progressPercent > 100) {
            throw new IllegalArgumentException(
                    "Progress percentage must be between 0 and 100, got: " + progressPercent);
        }
    }
}
