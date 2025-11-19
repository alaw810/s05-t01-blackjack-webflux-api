package cat.itacademy.s05.t01.blackjack.dto;

import java.util.List;

public record NewGameResponse(
        String playerName,
        List<String> playerHand,
        List<String> dealerHand,
        List<String> deck
) {}
