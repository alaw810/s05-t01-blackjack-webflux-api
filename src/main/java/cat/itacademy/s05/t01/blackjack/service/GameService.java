package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.NewGameResponse;
import cat.itacademy.s05.t01.blackjack.model.mongo.Game;
import cat.itacademy.s05.t01.blackjack.model.mysql.Player;
import cat.itacademy.s05.t01.blackjack.repository.mongo.GameReactiveRepository;
import cat.itacademy.s05.t01.blackjack.repository.mysql.PlayerRepository;
import cat.itacademy.s05.t01.blackjack.util.DeckFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GameService {

    private final PlayerRepository playerRepository;
    private final GameReactiveRepository gameRepository;

    public GameService(PlayerRepository playerRepository, GameReactiveRepository gameRepository) {
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
    }

    public Mono<NewGameResponse> createNewGame(String playerName) {
        // TODO: implementar (guíate por los tests)
        return Mono.empty();
    }

}
