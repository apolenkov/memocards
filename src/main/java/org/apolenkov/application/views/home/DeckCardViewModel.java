package org.apolenkov.application.views.home;

public class DeckCardViewModel {
    public final Long id;
    public final String title;
    public final String description;
    public final int deckSize;
    public final int knownCount;
    public final int progressPercent;

    public DeckCardViewModel(
            Long id, String title, String description, int deckSize, int knownCount, int progressPercent) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.deckSize = deckSize;
        this.knownCount = knownCount;
        this.progressPercent = progressPercent;
    }
}
