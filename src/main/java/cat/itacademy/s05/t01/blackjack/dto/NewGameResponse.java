package cat.itacademy.s05.t01.blackjack.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewGameResponse {
    private String gameId;
    private String playerName;
}
