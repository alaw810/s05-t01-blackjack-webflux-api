package cat.itacademy.s05.t01.blackjack.model.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "games")
public class Game {

    @Id
    private String id;

    private Long playerId;

    private List<String> playerHand;
    private List<String> dealerHand;

    private List<String> deck;

    private GameStatus status;
}
