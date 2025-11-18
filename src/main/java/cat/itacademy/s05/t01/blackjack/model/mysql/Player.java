package cat.itacademy.s05.t01.blackjack.model.mysql;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("players")
public class Player {

    @Id
    private Long id;

    private String name;

    private int gamesPlayed;
    private int gamesWon;
    private int gamesLost;

}
