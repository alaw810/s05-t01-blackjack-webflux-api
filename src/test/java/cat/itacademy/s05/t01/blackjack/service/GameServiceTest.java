package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.*;
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

    @Test
    void getGame_ShouldReturnGameDetails_WhenGameExists() {
        String gameId = "game-100";

        Game game = Game.builder()
                .id(gameId)
                .playerId(1L)
                .playerHand(List.of("AH", "7D"))
                .dealerHand(List.of("9C", "6S"))
                .deck(List.of("2H", "4D", "KC"))
                .status("IN_PROGRESS")
                .build();

        when(gameRepository.findById(gameId))
                .thenReturn(Mono.just(game));

        Mono<GameDetailsResponse> result = gameService.getGame(gameId);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getGameId()).isEqualTo(gameId);
                    assertThat(response.getPlayerId()).isEqualTo(1L);
                    assertThat(response.getPlayerHand()).containsExactly("AH", "7D");
                    assertThat(response.getDealerHand()).containsExactly("9C", "6S");
                    assertThat(response.getStatus()).isEqualTo("IN_PROGRESS");
                    assertThat(response.getPlayerHandValue()).isGreaterThan(0);
                    assertThat(response.getDealerHandValue()).isGreaterThan(0);
                    assertThat(response.getRemainingDeckSize()).isEqualTo(3);
                })
                .verifyComplete();

        verify(gameRepository, times(1)).findById(gameId);
    }

    @Test
    void getGame_ShouldReturnError_WhenGameDoesNotExist() {
        String gameId = "not-found";

        when(gameRepository.findById(gameId))
                .thenReturn(Mono.empty());

        Mono<GameDetailsResponse> result = gameService.getGame(gameId);

        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof org.springframework.web.server.ResponseStatusException &&
                                ((org.springframework.web.server.ResponseStatusException) error)
                                        .getStatusCode().value() == 404
                )
                .verify();

        verify(gameRepository, times(1)).findById(gameId);
    }

    @Test
    void playMove_HIT_ShouldAddCardToPlayer() {
        String gameId = "g1";

        Game existingGame = Game.builder()
                .id(gameId)
                .playerId(1L)
                .playerHand(new ArrayList<>(List.of("5H", "6D")))
                .dealerHand(new ArrayList<>(List.of("10C", "7S")))
                .deck(new ArrayList<>(List.of("9H", "4C", "8D")))
                .status("IN_PROGRESS")
                .build();

        when(gameRepository.findById(gameId))
                .thenReturn(Mono.just(existingGame));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        PlayRequestDTO request = new PlayRequestDTO("HIT");

        Mono<PlayResultDTO> result = gameService.playMove(gameId, request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getPlayerHand()).hasSize(3);
                    assertThat(response.getPlayerHand()).containsExactly("5H", "6D", "9H");
                    assertThat(response.getRemainingDeckSize()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    void playMove_HIT_ShouldCauseBust() {
        String gameId = "g2";

        Game game = Game.builder()
                .id(gameId)
                .playerId(1L)
                .playerHand(new ArrayList<>(List.of("10H", "9D")))
                .dealerHand(new ArrayList<>(List.of("5C", "7D")))
                .deck(new ArrayList<>(List.of("8C", "6H")))
                .status("IN_PROGRESS")
                .build();

        when(gameRepository.findById(gameId))
                .thenReturn(Mono.just(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        PlayRequestDTO request = new PlayRequestDTO("HIT");

        Mono<PlayResultDTO> result = gameService.playMove(gameId, request);

        when(playerRepository.findById(1L))
                .thenReturn(Mono.just(Player.builder()
                        .id(1L)
                        .name("Test")
                        .gamesPlayed(0)
                        .gamesWon(0)
                        .gamesLost(0)
                        .build()));

        when(playerRepository.save(any(Player.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getPlayerValue()).isGreaterThan(21);
                    assertThat(response.getStatus()).isEqualTo("PLAYER_BUST");
                })
                .verifyComplete();
    }

    @Test
    void playMove_STAND_ShouldTriggerDealerTurn() {
        String gameId = "g3";

        Game game = Game.builder()
                .id(gameId)
                .playerId(1L)
                .playerHand(new ArrayList<>(List.of("10H", "8D")))
                .dealerHand(new ArrayList<>(List.of("5C", "7D")))
                .deck(new ArrayList<>(List.of("6H", "4C", "9S")))
                .status("IN_PROGRESS")
                .build();

        when(gameRepository.findById(gameId))
                .thenReturn(Mono.just(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        PlayRequestDTO request = new PlayRequestDTO("STAND");

        Mono<PlayResultDTO> result = gameService.playMove(gameId, request);

        when(playerRepository.findById(1L))
                .thenReturn(Mono.just(Player.builder()
                        .id(1L)
                        .name("Test")
                        .gamesPlayed(0)
                        .gamesWon(0)
                        .gamesLost(0)
                        .build()));

        when(playerRepository.save(any(Player.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(result)
                .assertNext(response -> {
                    int dealerValue = response.getDealerValue();
                    assertThat(dealerValue).isGreaterThanOrEqualTo(17);
                    assertThat(response.getStatus()).isIn("DEALER_BUST", "PLAYER_WIN", "PLAYER_LOSE", "TIE");
                })
                .verifyComplete();
    }

    @Test
    void playMove_DOUBLE_ShouldAddCardAndFinishTurn() {
        String gameId = "g4";

        Game game = Game.builder()
                .id(gameId)
                .playerId(1L)
                .playerHand(new ArrayList<>(List.of("9H", "2D")))
                .dealerHand(new ArrayList<>(List.of("7C", "8S")))
                .deck(new ArrayList<>(List.of("10D", "3C", "6S")))
                .status("IN_PROGRESS")
                .build();

        when(gameRepository.findById(gameId))
                .thenReturn(Mono.just(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        PlayRequestDTO request = new PlayRequestDTO("DOUBLE");

        Mono<PlayResultDTO> result = gameService.playMove(gameId, request);

        when(playerRepository.findById(1L))
                .thenReturn(Mono.just(Player.builder()
                        .id(1L)
                        .name("Test")
                        .gamesPlayed(0)
                        .gamesWon(0)
                        .gamesLost(0)
                        .build()));

        when(playerRepository.save(any(Player.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getPlayerHand()).hasSize(3);
                    assertThat(response.getStatus()).isNotEqualTo("IN_PROGRESS");
                })
                .verifyComplete();
    }

    @Test
    void playMove_ShouldUpdatePlayerStatsWhenGameEnds() {
        String gameId = "g5";

        Game game = Game.builder()
                .id(gameId)
                .playerId(1L)
                .playerHand(new ArrayList<>(List.of("10H", "9D")))
                .dealerHand(new ArrayList<>(List.of("5C", "7D")))
                .deck(new ArrayList<>(List.of("8C")))
                .status("IN_PROGRESS")
                .build();

        Player player = Player.builder()
                .id(1L)
                .name("Alice")
                .gamesPlayed(0)
                .gamesWon(0)
                .gamesLost(0)
                .build();

        when(gameRepository.findById(gameId)).thenReturn(Mono.just(game));
        when(playerRepository.findById(1L)).thenReturn(Mono.just(player));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(playerRepository.save(any(Player.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        PlayRequestDTO request = new PlayRequestDTO("HIT");

        Mono<PlayResultDTO> result = gameService.playMove(gameId, request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatus()).isEqualTo("PLAYER_BUST");
                })
                .verifyComplete();

        verify(playerRepository, times(1)).save(any(Player.class));
    }

}
