package cat.itacademy.s05.t01.blackjack.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerRankingResponse {
    private Long id;
    private String name;
    private int gamesPlayed;
    private int gamesWon;
    private int gamesLost;
    private double winRate;
}
