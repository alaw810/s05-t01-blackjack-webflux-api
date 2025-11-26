package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.PlayerRankingResponse;
import cat.itacademy.s05.t01.blackjack.dto.PlayerResponse;
import cat.itacademy.s05.t01.blackjack.dto.PlayerUpdateRequest;
import cat.itacademy.s05.t01.blackjack.exception.NotFoundException;
import cat.itacademy.s05.t01.blackjack.model.mysql.Player;
import cat.itacademy.s05.t01.blackjack.repository.mysql.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerServiceImpl playerService;

    @Test
    void updatePlayerName_ShouldUpdateName_WhenPlayerExists() {
        Long playerId = 1L;

        Player existing = Player.builder()
                .id(playerId)
                .name("OldName")
                .gamesPlayed(3)
                .gamesWon(2)
                .gamesLost(1)
                .build();

        Player updated = Player.builder()
                .id(playerId)
                .name("NewName")
                .gamesPlayed(3)
                .gamesWon(2)
                .gamesLost(1)
                .build();

        when(playerRepository.findById(playerId))
                .thenReturn(Mono.just(existing));

        when(playerRepository.save(any(Player.class)))
                .thenReturn(Mono.just(updated));

        Mono<PlayerResponse> result =
                playerService.updatePlayerName(playerId, new PlayerUpdateRequest("NewName"));

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo(playerId);
                    assertThat(response.getName()).isEqualTo("NewName");
                    assertThat(response.getGamesPlayed()).isEqualTo(3);
                })
                .verifyComplete();

        verify(playerRepository, times(1)).findById(playerId);
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    void updatePlayerName_ShouldReturn404_WhenPlayerNotFound() {
        Long playerId = 99L;

        when(playerRepository.findById(playerId))
                .thenReturn(Mono.empty());

        Mono<PlayerResponse> result =
                playerService.updatePlayerName(playerId, new PlayerUpdateRequest("DoesNotMatter"));

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();

        verify(playerRepository, times(1)).findById(playerId);
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void getRanking_ShouldReturnPlayersOrderedByWinsAndWinRate() {
        Player p1 = Player.builder()
                .id(1L).name("Alice")
                .gamesPlayed(10).gamesWon(7).gamesLost(3)
                .build();

        Player p2 = Player.builder()
                .id(2L).name("Bob")
                .gamesPlayed(10).gamesWon(9).gamesLost(1)
                .build();

        Player p3 = Player.builder()
                .id(3L).name("Charlie")
                .gamesPlayed(10).gamesWon(9).gamesLost(1)
                .build();

        when(playerRepository.findAll())
                .thenReturn(Flux.just(p1, p3, p2)); // desordenados

        Flux<PlayerRankingResponse> result = playerService.getRanking();

        StepVerifier.create(result)
                .assertNext(r -> assertThat(r.getId()).isEqualTo(2L))  // Bob (9 wins)
                .assertNext(r -> assertThat(r.getId()).isEqualTo(3L))  // Charlie (9 wins, same winRate)
                .assertNext(r -> assertThat(r.getId()).isEqualTo(1L))  // Alice (7 wins)
                .verifyComplete();
    }

    @Test
    void getRanking_ShouldReturnEmptyList_WhenNoPlayersExist() {
        when(playerRepository.findAll()).thenReturn(Flux.empty());

        Flux<PlayerRankingResponse> result = playerService.getRanking();

        StepVerifier.create(result)
                .verifyComplete();
    }

}
