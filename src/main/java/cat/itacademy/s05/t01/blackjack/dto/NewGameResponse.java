package cat.itacademy.s05.t01.blackjack.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NewGameResponse {

    private String gameId;
    private String playerName;

    private List<String> playerHand;
    private List<String> dealerHand;

    private int playerHandValue;
    private int dealerHandValue;

    private int remainingDeckSize;
    private String status; // e.g. "IN_PROGRESS"
}
