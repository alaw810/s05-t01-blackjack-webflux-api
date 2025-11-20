package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.NewGameRequest;
import cat.itacademy.s05.t01.blackjack.dto.NewGameResponse;
import reactor.core.publisher.Mono;

public interface GameService {

    Mono<NewGameResponse> createNewGame(NewGameRequest request);
}
