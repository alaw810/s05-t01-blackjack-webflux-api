package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.NewGameRequest;
import cat.itacademy.s05.t01.blackjack.dto.NewGameResponse;
import cat.itacademy.s05.t01.blackjack.model.mysql.Player;
import cat.itacademy.s05.t01.blackjack.repository.mongo.GameReactiveRepository;
import cat.itacademy.s05.t01.blackjack.repository.mysql.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameReactiveRepository gameRepository;

    @InjectMocks
    private GameServiceImpl gameService;

    @Test
    void createNewGame_ShouldCreatePlayer_WhenPlayerDoesNotExist() {
        NewGameRequest request = new NewGameRequest("Adria");

        when(playerRepository.findByName("Adria"))
                .thenReturn(Mono.empty());

        Player savedPlayer = Player.builder()
                .id(1L)
                .name("Adria")
                .gamesPlayed(0)
                .gamesWon(0)
                .gamesLost(0)
                .build();

        when(playerRepository.save(any(Player.class)))
                .thenReturn(Mono.just(savedPlayer));

        Mono<NewGameResponse> result = gameService.createNewGame(request);

        StepVerifier.create(result)
                .expectNextMatches(res ->
                        res.getPlayerName().equals("Adria")
                )
                .verifyComplete();

        verify(playerRepository).save(any(Player.class));
    }
}
