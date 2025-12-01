package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.NewGameRequest;
import cat.itacademy.s05.t01.blackjack.dto.PlayRequestDTO;
import cat.itacademy.s05.t01.blackjack.exception.InvalidMoveException;
import cat.itacademy.s05.t01.blackjack.exception.NotFoundException;
import cat.itacademy.s05.t01.blackjack.exception.ValidationException;
import cat.itacademy.s05.t01.blackjack.model.mongo.Game;
import cat.itacademy.s05.t01.blackjack.model.mongo.GameStatus;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
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
    }

    private Player mockPlayer(long id, String name) {
        return Player.builder()
                .id(id)
                .name(name)
                .gamesPlayed(0)
                .gamesWon(0)
                .gamesLost(0)
                .build();
    }

    private Game mockGame(
            String id,
            List<String> player,
            List<String> dealer,
            List<String> deck,
            GameStatus status
    ) {
        return Game.builder()
                .id(id)
                .playerId(1L)
                .playerHand(new ArrayList<>(player))
                .dealerHand(new ArrayList<>(dealer))
                .deck(new ArrayList<>(deck))
                .status(status)
                .build();
    }

    private void mockGameSave() {
        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    private void mockPlayerRepo() {
        when(playerRepository.findById(anyLong()))
                .thenReturn(Mono.just(mockPlayer(1L, "Test")));

        when(playerRepository.save(any(Player.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    @Test
    void createNewGame_ShouldCreateGameInMongo() {
        NewGameRequest request = new NewGameRequest("Alice");
        Player existing = mockPlayer(1L, "Alice");

        when(playerRepository.findByName("Alice")).thenReturn(Mono.just(existing));
        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> {
                    Game g = invocation.getArgument(0);
                    g.setId("game-123");
                    return Mono.just(g);
                });

        StepVerifier.create(gameService.createNewGame(request))
                .assertNext(res -> {
                    assertThat(res.getGameId()).isEqualTo("game-123");
                    assertThat(res.getPlayerName()).isEqualTo("Alice");
                })
                .verifyComplete();
    }

    @Test
    void createNewGame_ShouldCreatePlayerIfNotExists() {
        NewGameRequest request = new NewGameRequest("Bob");

        when(playerRepository.findByName("Bob")).thenReturn(Mono.empty());
        when(playerRepository.save(any(Player.class))).thenReturn(Mono.just(mockPlayer(5L, "Bob")));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> {
                    Game g = invocation.getArgument(0);
                    g.setId("game-999");
                    return Mono.just(g);
                });

        StepVerifier.create(gameService.createNewGame(request))
                .assertNext(res -> {
                    assertThat(res.getPlayerName()).isEqualTo("Bob");
                    assertThat(res.getGameId()).isEqualTo("game-999");
                })
                .verifyComplete();
    }

    @Test
    void createNewGame_ShouldNotCreatePlayerIfAlreadyExists() {
        NewGameRequest request = new NewGameRequest("Charlie");

        when(playerRepository.findByName(anyString()))
                .thenReturn(Mono.just(mockPlayer(10L, "Charlie")));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> {
                    Game g = invocation.getArgument(0);
                    g.setId("game-456");
                    return Mono.just(g);
                });

        StepVerifier.create(gameService.createNewGame(request))
                .assertNext(res -> assertThat(res.getPlayerName()).isEqualTo("Charlie"))
                .verifyComplete();

        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void createNewGame_ShouldInitializeDeckAndHandsCorrectly() {
        NewGameRequest request = new NewGameRequest("Dana");

        when(playerRepository.findByName("Dana"))
                .thenReturn(Mono.just(mockPlayer(20L, "Dana")));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> {
                    Game g = invocation.getArgument(0);
                    g.setId("game-init");
                    return Mono.just(g);
                });

        StepVerifier.create(gameService.createNewGame(request))
                .assertNext(res -> {
                    assertThat(res.getPlayerHand()).hasSize(2);
                    assertThat(res.getDealerHand()).hasSize(1);
                    assertThat(res.getRemainingDeckSize()).isEqualTo(48);
                    assertThat(res.getStatus()).isEqualTo("IN_PROGRESS");
                })
                .verifyComplete();
    }

    @Test
    void getGame_ShouldReturnGameDetails_WhenGameExists() {
        Game game = mockGame(
                "game-100",
                List.of("AH", "7D"),
                List.of("9C", "6S"),
                List.of("2H", "4D", "KC"),
                GameStatus.IN_PROGRESS
        );

        when(gameRepository.findById("game-100"))
                .thenReturn(Mono.just(game));

        StepVerifier.create(gameService.getGame("game-100"))
                .assertNext(res -> {
                    assertThat(res.getGameId()).isEqualTo("game-100");
                    assertThat(res.getPlayerId()).isEqualTo(1L);
                    assertThat(res.getPlayerHand()).containsExactly("AH", "7D");
                })
                .verifyComplete();
    }

    @Test
    void getGame_ShouldReturnError_WhenGameDoesNotExist() {
        when(gameRepository.findById("not-found")).thenReturn(Mono.empty());

        StepVerifier.create(gameService.getGame("not-found"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void playMove_ShouldThrowInvalidMoveException_WhenMoveIsInvalid() {
        assertThrows(
                ValidationException.class,
                () -> gameService.playMove("g1", new PlayRequestDTO("INVALID"))
        );
    }

    @Test
    void playMove_ShouldThrowIllegalStateException_WhenGameIsFinished() {
        Game game = mockGame(
                "g2",
                List.of("5H", "6D"),
                List.of("10C", "7S"),
                List.of("9H"),
                GameStatus.PLAYER_WIN
        );

        when(gameRepository.findById("g2")).thenReturn(Mono.just(game));

        StepVerifier.create(gameService.playMove("g2", new PlayRequestDTO("HIT")))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void playMove_HIT_ShouldAddCardToPlayer() {
        Game game = mockGame(
                "g1",
                List.of("5H", "6D"),
                List.of("10C", "7S"),
                List.of("9H", "4C", "8D"),
                GameStatus.IN_PROGRESS
        );

        when(gameRepository.findById("g1")).thenReturn(Mono.just(game));
        mockGameSave();

        StepVerifier.create(gameService.playMove("g1", new PlayRequestDTO("HIT")))
                .assertNext(res -> assertThat(res.getPlayerHand()).hasSize(3))
                .verifyComplete();
    }

    @Test
    void playMove_HIT_ShouldCausePlayerBust() {
        Game game = mockGame(
                "g2",
                List.of("10H", "9D"),
                List.of("5C", "7D"),
                List.of("8C", "6H"),
                GameStatus.IN_PROGRESS
        );

        when(gameRepository.findById("g2")).thenReturn(Mono.just(game));
        mockGameSave();
        mockPlayerRepo();

        StepVerifier.create(gameService.playMove("g2", new PlayRequestDTO("HIT")))
                .assertNext(res -> assertThat(res.getStatus()).isEqualTo("PLAYER_BUST"))
                .verifyComplete();

        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    void playMove_STAND_ShouldEndGameAndUpdateStats() {
        Game game = mockGame(
                "g3",
                List.of("10H", "8D"),
                List.of("5C", "7D"),
                List.of("6H", "4C", "9S"),
                GameStatus.IN_PROGRESS
        );

        when(gameRepository.findById("g3")).thenReturn(Mono.just(game));
        mockGameSave();
        mockPlayerRepo();

        StepVerifier.create(gameService.playMove("g3", new PlayRequestDTO("STAND")))
                .assertNext(res -> assertThat(res.getStatus()).isNotEqualTo("IN_PROGRESS"))
                .verifyComplete();

        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    void playMove_DOUBLE_ShouldAddCardAndFinishTurn() {
        Game game = mockGame(
                "g4",
                List.of("9H", "2D"),
                List.of("7C", "8S"),
                List.of("10D", "3C", "6S"),
                GameStatus.IN_PROGRESS
        );

        when(gameRepository.findById("g4")).thenReturn(Mono.just(game));
        mockGameSave();
        mockPlayerRepo();

        StepVerifier.create(gameService.playMove("g4", new PlayRequestDTO("DOUBLE")))
                .assertNext(res -> assertThat(res.getStatus()).isNotEqualTo("IN_PROGRESS"))
                .verifyComplete();

        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    void deleteGame_ShouldDeleteExistingGame() {
        String gameId = "g100";

        when(gameRepository.findById(gameId))
                .thenReturn(Mono.just(Game.builder().id(gameId).build()));

        when(gameRepository.deleteById(gameId))
                .thenReturn(Mono.empty());

        Mono<Void> result = gameService.deleteGame(gameId);

        StepVerifier.create(result)
                .verifyComplete();

        verify(gameRepository, times(1)).deleteById(gameId);
    }

    @Test
    void deleteGame_ShouldReturn404_WhenGameDoesNotExist() {
        String gameId = "missing";

        when(gameRepository.findById(gameId))
                .thenReturn(Mono.empty());

        Mono<Void> result = gameService.deleteGame(gameId);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();

        verify(gameRepository, never()).deleteById(anyString());
    }
}
