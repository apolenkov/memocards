package org.apolenkov.application.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.DeckFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"dev"})
@org.junit.jupiter.api.Tag("integration")
class DeckFacadeIT {

    @Autowired
    DeckFacade deckFacade;

    @Test
    void createDeck_succeeds() {
        Deck d = new Deck();
        d.setTitle("IT facade deck");
        d.setUserId(1L);
        Deck saved = deckFacade.saveDeck(d);
        assertThat(saved.getId()).isNotNull();
    }
}
