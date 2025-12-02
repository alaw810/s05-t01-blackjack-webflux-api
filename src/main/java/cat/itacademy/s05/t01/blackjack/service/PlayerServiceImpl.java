package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.PlayerRankingResponse;
import cat.itacademy.s05.t01.blackjack.dto.PlayerResponse;
import cat.itacademy.s05.t01.blackjack.dto.PlayerUpdateRequest;
import cat.itacademy.s05.t01.blackjack.exception.NotFoundException;
import cat.itacademy.s05.t01.blackjack.exception.ValidationException;
import cat.itacademy.s05.t01.blackjack.model.mysql.Player;
import cat.itacademy.s05.t01.blackjack.repository.mysql.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    @Override
    public Mono<PlayerResponse> updatePlayerName(Long playerId, PlayerUpdateRequest request) {

        return playerRepository.findById(playerId)
                .switchIfEmpty(Mono.error(new NotFoundException("Player not found")))
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
        return playerRepository.findAll()
                .sort(RANKING_ORDER)
                .map(player -> {
                    double winRate = player.getGamesPlayed() == 0 ? 0 :
                            (double) player.getGamesWon() / player.getGamesPlayed();

                    return PlayerRankingResponse.builder()
                            .id(player.getId())
                            .name(player.getName())
                            .gamesPlayed(player.getGamesPlayed())
                            .gamesWon(player.getGamesWon())
                            .gamesLost(player.getGamesLost())
                            .winRate(winRate)
                            .build();
                });
    }

    private static final Comparator<Player> RANKING_ORDER = Comparator
            .comparing(Player::getGamesWon).reversed()
            .thenComparing(p -> p.getGamesPlayed() == 0 ? 0 :
                    (double) p.getGamesWon() / p.getGamesPlayed(), Comparator.reverseOrder())
            .thenComparing(Player::getId);
}
