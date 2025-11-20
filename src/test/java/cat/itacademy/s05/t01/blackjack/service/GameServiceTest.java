package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.NewGameRequest;
import cat.itacademy.s05.t01.blackjack.dto.NewGameResponse;
import cat.itacademy.s05.t01.blackjack.model.mongo.Game;
import cat.itacademy.s05.t01.blackjack.model.mysql.Player;
import cat.itacademy.s05.t01.blackjack.repository.mongo.GameReactiveRepository;
import cat.itacademy.s05.t01.blackjack.repository.mysql.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GameServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameReactiveRepository gameRepository;

    @InjectMocks
    private GameServiceImpl gameService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        when(playerRepository.save(any(Player.class)))
                .thenReturn(Mono.just(
                        Player.builder()
                                .id(999L)
                                .name("dummy")
                                .gamesPlayed(0)
                                .gamesWon(0)
                                .gamesLost(0)
                                .build()
                ));
    }

    @Test
    void createNewGame_ShouldCreateGameInMongo() {
        NewGameRequest request = new NewGameRequest("Alice");

        Player existingPlayer = Player.builder()
                .id(1L)
                .name("Alice")
                .gamesPlayed(0)
                .gamesWon(0)
                .gamesLost(0)
                .build();

        when(playerRepository.findByName("Alice"))
                .thenReturn(Mono.just(existingPlayer));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> {
                    Game game = invocation.getArgument(0);
                    game.setId("game-123");
                    return Mono.just(game);
                });

        Mono<NewGameResponse> result = gameService.createNewGame(request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getGameId()).isEqualTo("game-123");
                    assertThat(response.getPlayerName()).isEqualTo("Alice");
                })
                .verifyComplete();

        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    void createNewGame_ShouldCreatePlayerIfNotExists() {
        NewGameRequest request = new NewGameRequest("Bob");

        when(playerRepository.findByName("Bob"))
                .thenReturn(Mono.empty());

        Player savedPlayer = Player.builder()
                .id(5L)
                .name("Bob")
                .gamesPlayed(0)
                .gamesWon(0)
                .gamesLost(0)
                .build();

        when(playerRepository.save(any(Player.class)))
                .thenReturn(Mono.just(savedPlayer));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> {
                    Game game = invocation.getArgument(0);
                    game.setId("game-999");
                    return Mono.just(game);
                });

        Mono<NewGameResponse> result = gameService.createNewGame(request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getGameId()).isEqualTo("game-999");
                    assertThat(response.getPlayerName()).isEqualTo("Bob");
                })
                .verifyComplete();

        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    void createNewGame_ShouldNotCreatePlayerIfAlreadyExists() {
        NewGameRequest request = new NewGameRequest("Charlie");

        Player existingPlayer = Player.builder()
                .id(10L)
                .name("Charlie")
                .gamesPlayed(0)
                .gamesWon(0)
                .gamesLost(0)
                .build();

        when(playerRepository.findByName(anyString()))
                .thenReturn(Mono.just(existingPlayer));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> {
                    Game game = invocation.getArgument(0);
                    game.setId("game-456");
                    return Mono.just(game);
                });

        Mono<NewGameResponse> result = gameService.createNewGame(request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getPlayerName()).isEqualTo("Charlie");
                    assertThat(response.getGameId()).isEqualTo("game-456");
                })
                .verifyComplete();

        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void createNewGame_ShouldInitializeDeckAndHandsCorrectly() {
        NewGameRequest request = new NewGameRequest("Dana");

        Player player = Player.builder()
                .id(20L)
                .name("Dana")
                .gamesPlayed(0)
                .gamesWon(0)
                .gamesLost(0)
                .build();

        when(playerRepository.findByName("Dana"))
                .thenReturn(Mono.just(player));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> {
                    Game game = invocation.getArgument(0);
                    game.setId("game-init");
                    return Mono.just(game);
                });

        Mono<NewGameResponse> result = gameService.createNewGame(request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    List<String> playerHand = response.getPlayerHand();
                    List<String> dealerHand = response.getDealerHand();

                    assertThat(playerHand).hasSize(2);
                    assertThat(dealerHand).hasSize(2);

                    List<String> combined = new ArrayList<>(playerHand);
                    combined.retainAll(dealerHand);
                    assertThat(combined).isEmpty();

                    assertThat(response.getRemainingDeckSize()).isEqualTo(48);

                    assertThat(response.getPlayerHandValue()).isBetween(4, 21);
                    assertThat(response.getDealerHandValue()).isBetween(4, 21);

                    assertThat(response.getStatus()).isEqualTo("IN_PROGRESS");
                })
                .verifyComplete();
    }
}
