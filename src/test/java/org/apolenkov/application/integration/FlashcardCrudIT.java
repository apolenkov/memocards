package org.apolenkov.application.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.DeckFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"dev", "memory"})
@org.junit.jupiter.api.Tag("integration")
class FlashcardCrudIT {

    @Autowired
    DeckFacade deckFacade;

    @Test
    void createAndDeleteFlashcard() {
        Deck deck = new Deck();
        deck.setTitle("IT Cards");
        deck.setUserId(1L);
        Deck savedDeck = deckFacade.saveDeck(deck);

        Flashcard card = new Flashcard();
        card.setDeckId(savedDeck.getId());
        card.setFrontText("Hello");
        card.setBackText("Привет");
        Flashcard saved = deckFacade.saveFlashcard(card);
        assertThat(saved.getId()).isNotNull();

        List<Flashcard> cards = deckFacade.loadFlashcards(savedDeck.getId());
        assertThat(cards).extracting(Flashcard::getFrontText).contains("Hello");

        deckFacade.deleteFlashcard(saved.getId());
        cards = deckFacade.loadFlashcards(savedDeck.getId());
        assertThat(cards).noneMatch(c -> "Hello".equals(c.getFrontText()));
    }
}
