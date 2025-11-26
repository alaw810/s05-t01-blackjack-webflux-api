package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.PlayerRankingResponse;
import cat.itacademy.s05.t01.blackjack.dto.PlayerResponse;
import cat.itacademy.s05.t01.blackjack.dto.PlayerUpdateRequest;
import cat.itacademy.s05.t01.blackjack.exception.NotFoundException;
import cat.itacademy.s05.t01.blackjack.exception.ValidationException;
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
            return Mono.error(new ValidationException("Name cannot be empty"));
        }

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
                .sort((p1, p2) -> {
                    int byWins = Integer.compare(p2.getGamesWon(), p1.getGamesWon());
                    if (byWins != 0) return byWins;

                    double winRate1 = p1.getGamesPlayed() == 0 ? 0
                            : (double) p1.getGamesWon() / p1.getGamesPlayed();
                    double winRate2 = p2.getGamesPlayed() == 0 ? 0
                            : (double) p2.getGamesWon() / p2.getGamesPlayed();

                    int byWinRate = Double.compare(winRate2, winRate1);
                    if (byWinRate != 0) return byWinRate;

                    return Long.compare(p1.getId(), p2.getId());
                })
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

}
