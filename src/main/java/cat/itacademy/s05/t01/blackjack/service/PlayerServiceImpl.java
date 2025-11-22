package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.PlayerRankingResponse;
import cat.itacademy.s05.t01.blackjack.dto.PlayerResponse;
import cat.itacademy.s05.t01.blackjack.dto.PlayerUpdateRequest;
import cat.itacademy.s05.t01.blackjack.repository.mysql.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    @Override
    public Mono<PlayerResponse> updatePlayerName(Long playerId, PlayerUpdateRequest request) {

        if (request == null || request.newName() == null || request.newName().trim().isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be empty"));
        }

        return playerRepository.findById(playerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(player -> {
                    player.setName(request.newName().trim());
                    return playerRepository.save(player);
                })
                .map(saved -> PlayerResponse.builder()
                        .id(saved.getId())
                        .name(saved.getName())
                        .gamesPlayed(saved.getGamesPlayed())
                        .gamesWon(saved.getGamesWon())
                        .gamesLost(saved.getGamesLost())
                        .build());
    }

    @Override
    public Flux<PlayerRankingResponse> getRanking() {
        return Flux.empty();
    }

}
