package cat.itacademy.s05.t01.blackjack.util;

import java.util.List;

public class BlackjackRules {

    public static int calculateHandValue(List<String> hand) {
        int total = 0;
        int aces = 0;

        for (String card : hand) {
            String rank = card.substring(0, card.length() - 1);

            switch (rank) {
                case "J", "Q", "K" -> total += 10;
                case "A" -> {
                    aces++;
                    total += 11;
                }
                default -> total += Integer.parseInt(rank);
            }
        }

        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }

        return total;
    }

    public static boolean isBlackjack(List<String> hand) {
        return hand.size() == 2 && calculateHandValue(hand) == 21;
    }
}
