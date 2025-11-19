package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.NewGameRequest;
import cat.itacademy.s05.t01.blackjack.dto.NewGameResponse;
import cat.itacademy.s05.t01.blackjack.repository.mongo.GameReactiveRepository;
import cat.itacademy.s05.t01.blackjack.repository.mysql.PlayerRepository;
import reactor.core.publisher.Mono;

public class GameServiceImpl implements GameService {

    private final PlayerRepository playerRepository;
    private final GameReactiveRepository gameRepository;

    public GameServiceImpl(PlayerRepository playerRepository,
                           GameReactiveRepository gameRepository) {
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    public Mono<NewGameResponse> createNewGame(NewGameRequest request) {
        return Mono.empty();
    }
}
