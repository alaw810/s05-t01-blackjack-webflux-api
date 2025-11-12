package cat.itacademy.s05.t01.blackjack.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeckFactoryTest {

    @Test
    void deckShouldContain52UniqueCards() {
        List<String> deck = DeckFactory.createShuffledDeck();

        assertThat(deck)
                .hasSize(52)
                .doesNotHaveDuplicates();
    }

    @Test
    void deckShouldContainAllSuitsAndRanks() {
        List<String> deck = DeckFactory.createShuffledDeck();

        List<String> suits = List.of("H", "D", "C", "S");
        List<String> ranks = List.of("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K");

        for (String suit : suits) {
            for (String rank : ranks) {
                assertThat(deck).contains(rank + suit);
            }
        }
    }
}
