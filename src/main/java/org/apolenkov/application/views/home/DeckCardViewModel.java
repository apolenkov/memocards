package org.apolenkov.application.views.home;

public record DeckCardViewModel(
        Long id, String title, String description, int deckSize, int knownCount, int progressPercent) {}
