package cat.itacademy.s05.t01.blackjack.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlayResultDTO {
    private String gameId;
    private String status;
    private List<String> playerHand;
    private List<String> dealerHand;
    private int playerValue;
    private int dealerValue;
    private String message;
}
