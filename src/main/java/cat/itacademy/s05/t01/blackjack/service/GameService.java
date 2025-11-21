package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.*;
import reactor.core.publisher.Mono;

public interface GameService {

    Mono<NewGameResponse> createNewGame(NewGameRequest request);
    Mono<GameDetailsResponse> getGame(String gameId);
    Mono<PlayResultDTO> playMove(String gameId, PlayRequestDTO request);
    Mono<Void> deleteGame(String gameId);
}
