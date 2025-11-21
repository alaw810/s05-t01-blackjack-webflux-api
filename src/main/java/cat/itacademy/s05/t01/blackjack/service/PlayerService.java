package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.PlayerResponse;
import cat.itacademy.s05.t01.blackjack.dto.PlayerUpdateRequest;
import reactor.core.publisher.Mono;

public interface PlayerService {

    Mono<PlayerResponse> updatePlayerName(Long playerId, PlayerUpdateRequest request);

}
