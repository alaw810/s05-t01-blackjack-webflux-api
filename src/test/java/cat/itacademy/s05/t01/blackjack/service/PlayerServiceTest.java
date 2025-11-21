package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.PlayerResponse;
import cat.itacademy.s05.t01.blackjack.dto.PlayerUpdateRequest;
import cat.itacademy.s05.t01.blackjack.model.mysql.Player;
import cat.itacademy.s05.t01.blackjack.repository.mysql.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
                .expectErrorMatches(error ->
                        error instanceof ResponseStatusException &&
                                ((ResponseStatusException) error)
                                        .getStatusCode().value() == HttpStatus.NOT_FOUND.value()
                )
                .verify();

        verify(playerRepository, times(1)).findById(playerId);
        verify(playerRepository, never()).save(any(Player.class));
    }
}
