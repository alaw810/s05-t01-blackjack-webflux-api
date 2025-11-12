package cat.itacademy.s05.t01.blackjack.util;

import java.util.List;

public class BlackjackRules {

    private static final int BLACKJACK = 21;
    private static final int FACE_VALUE = 10;
    private static final int ACE_HIGH = 11;
    private static final int ACE_LOW_ADJUSTMENT = 10;

    private BlackjackRules() {}

    public static int calculateHandValue(List<String> hand) {
        int total = 0;
        int aces = 0;

        for (String card : hand) {
            int value = cardValue(card);
            total += value;
            if (isAce(card)) aces++;
        }

        while (total > BLACKJACK && aces-- > 0) {
            total -= ACE_LOW_ADJUSTMENT;
        }

        return total;
    }

    public static boolean isBlackjack(List<String> hand) {
        return hand.size() == 2 && calculateHandValue(hand) == BLACKJACK;
    }

    private static int cardValue(String card) {
        String rank = card.substring(0, card.length() - 1);
        return switch (rank) {
            case "J", "Q", "K" -> FACE_VALUE;
            case "A" -> ACE_HIGH;
            default -> Integer.parseInt(rank);
        };
    }

    private static boolean isAce(String card) {
        return card.startsWith("A");
    }
}
