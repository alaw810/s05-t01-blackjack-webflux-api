package cat.itacademy.s05.t01.blackjack.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeckFactory {

    private static final List<String> SUITS = List.of("H", "D", "C", "S");
    private static final List<String> RANKS = List.of("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K");

    public static List<String> createShuffledDeck() {
        List<String> deck = new ArrayList<>();
        for (String suit : SUITS) {
            for (String rank : RANKS) {
                deck.add(rank + suit);
            }
        }
        Collections.shuffle(deck);
        return deck;
    }
}
