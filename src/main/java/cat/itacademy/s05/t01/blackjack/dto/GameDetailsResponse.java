package cat.itacademy.s05.t01.blackjack.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GameDetailsResponse {
    private String gameId;
    private Long playerId;
    private List<String> playerHand;
    private List<String> dealerHand;
    private String status;
    private int playerHandValue;
    private int dealerHandValue;
}
