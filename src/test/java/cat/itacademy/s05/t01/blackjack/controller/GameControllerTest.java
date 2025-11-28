package cat.itacademy.s05.t01.blackjack.controller;

import cat.itacademy.s05.t01.blackjack.dto.*;
import cat.itacademy.s05.t01.blackjack.exception.GlobalExceptionHandler;
import cat.itacademy.s05.t01.blackjack.exception.InvalidMoveException;
import cat.itacademy.s05.t01.blackjack.exception.NotFoundException;
import cat.itacademy.s05.t01.blackjack.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
                .bindToController(gameController)
                .controllerAdvice(new GlobalExceptionHandler())
                .configureClient()
                .baseUrl("/game")
                .build();
    }

    @Test
    void createNewGame_ShouldReturn201AndGameDetails() {
        NewGameResponse response = NewGameResponse.builder()
                .gameId("game-123")
                .playerName("Alice")
                .playerHand(List.of("AH", "5D"))
                .dealerHand(List.of("7C", "8S"))
                .playerHandValue(16)
                .dealerHandValue(15)
                .remainingDeckSize(48)
                .status("IN_PROGRESS")
                .build();

        when(gameService.createNewGame(any(NewGameRequest.class)))
                .thenReturn(Mono.just(response));

        webTestClient.post().uri("/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new NewGameRequest("Alice"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.gameId").isEqualTo("game-123")
                .jsonPath("$.playerName").isEqualTo("Alice");
    }

    @Test
    void createNewGame_ShouldCallServiceWithCorrectRequest() {
        when(gameService.createNewGame(any(NewGameRequest.class)))
                .thenReturn(Mono.just(NewGameResponse.builder()
                        .gameId("id")
                        .playerName("Charlie")
                        .build()));

        webTestClient.post().uri("/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new NewGameRequest("Charlie"))
                .exchange()
                .expectStatus().isCreated();

        verify(gameService, times(1)).createNewGame(any(NewGameRequest.class));
    }

    @Test
    void createNewGame_ShouldReturn400_WhenNameIsEmpty() {
        webTestClient.post().uri("/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new NewGameRequest(""))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Player name cannot be empty");

    }

    @Test
    void getGame_ShouldReturn200AndGameDetails() {
        GameDetailsResponse response = GameDetailsResponse.builder()
                .gameId("123")
                .playerId(1L)
                .playerHand(List.of("AH", "7D"))
                .dealerHand(List.of("9C", "6S"))
                .status("IN_PROGRESS")
                .playerHandValue(18)
                .dealerHandValue(15)
                .remainingDeckSize(40)
                .build();

        when(gameService.getGame("123"))
                .thenReturn(Mono.just(response));

        webTestClient.get().uri("/123")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.gameId").isEqualTo("123")
                .jsonPath("$.playerId").isEqualTo(1)
                .jsonPath("$.status").isEqualTo("IN_PROGRESS");
    }

    @Test
    void getGame_ShouldReturn404_WhenGameNotFound() {
        when(gameService.getGame("missing"))
                .thenReturn(Mono.error(new NotFoundException("Game not found")));

        webTestClient.get().uri("/missing")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getGame_ShouldCallService() {
        when(gameService.getGame("abc"))
                .thenReturn(Mono.just(GameDetailsResponse.builder()
                        .gameId("abc")
                        .playerId(5L)
                        .build()));

        webTestClient.get().uri("/abc")
                .exchange()
                .expectStatus().isOk();

        verify(gameService, times(1)).getGame("abc");
    }

    @Test
    void getGame_ShouldReturn404AndJson() {
        when(gameService.getGame("missing"))
                .thenReturn(Mono.error(new NotFoundException("Game not found")));

        webTestClient.get().uri("/missing")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Game not found")
                .jsonPath("$.path").isEqualTo("/game/missing");
    }


    @Test
    void playMove_ShouldReturn200AndGameState() {
        PlayResultDTO result = PlayResultDTO.builder()
                .gameId("g1")
                .status("IN_PROGRESS")
                .playerHand(List.of("5H", "6D", "9H"))
                .dealerHand(List.of("10C", "7S"))
                .playerValue(20)
                .dealerValue(17)
                .remainingDeckSize(40)
                .build();

        when(gameService.playMove("g1", new PlayRequestDTO("HIT")))
                .thenReturn(Mono.just(result));

        webTestClient.post()
                .uri("/g1/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PlayRequestDTO("HIT"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.gameId").isEqualTo("g1")
                .jsonPath("$.status").isEqualTo("IN_PROGRESS")
                .jsonPath("$.playerHand.length()").isEqualTo(3);
    }

    @Test
    void playMove_ShouldReturn400_WhenMoveIsInvalid() {
        when(gameService.playMove(eq("g1"), any()))
                .thenReturn(Mono.error(new InvalidMoveException("Move is invalid")));

        webTestClient.post()
                .uri("/g1/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PlayRequestDTO("INVALID"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Move is invalid");
    }

    @Test
    void playMove_ShouldReturn404_WhenGameNotFound() {
        when(gameService.playMove(eq("missing"), any()))
                .thenReturn(Mono.error(new NotFoundException("Game not found")));

        webTestClient.post()
                .uri("/missing/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PlayRequestDTO("HIT"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void playMove_ShouldCallServiceWithCorrectArguments() {
        PlayRequestDTO req = new PlayRequestDTO("STAND");

        when(gameService.playMove("g123", req))
                .thenReturn(Mono.just(
                        PlayResultDTO.builder().gameId("g123").status("PLAYER_WIN").build()
                ));

        webTestClient.post()
                .uri("/g123/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk();

        verify(gameService, times(1)).playMove("g123", req);
    }

    @Test
    void playMove_ShouldReturn400_WhenGameIsAlreadyFinished() {
        when(gameService.playMove(eq("g1"), any()))
                .thenReturn(Mono.error(new IllegalStateException("Game is already finished")));

        webTestClient.post()
                .uri("/g1/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PlayRequestDTO("HIT"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Game is already finished");
    }

    @Test
    void playMove_ShouldReturn400_WhenMoveIsNotAllowed() {
        when(gameService.playMove(eq("g1"), any()))
                .thenReturn(Mono.error(new InvalidMoveException("Invalid move")));

        webTestClient.post()
                .uri("/g1/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PlayRequestDTO("HIT"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("BAD_REQUEST")
                .jsonPath("$.message").isEqualTo("Invalid move");
    }

    @Test
    void playMove_ShouldReturnFullJsonStructure() {
        PlayResultDTO result = PlayResultDTO.builder()
                .gameId("g1")
                .status("PLAYER_WIN")
                .playerHand(List.of("10H", "9D"))
                .dealerHand(List.of("5C", "7D"))
                .playerValue(19)
                .dealerValue(12)
                .remainingDeckSize(40)
                .message("Player wins!")
                .build();

        when(gameService.playMove(eq("g1"), any()))
                .thenReturn(Mono.just(result));

        webTestClient.post()
                .uri("/g1/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PlayRequestDTO("HIT"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.gameId").isEqualTo("g1")
                .jsonPath("$.status").isEqualTo("PLAYER_WIN")
                .jsonPath("$.message").isEqualTo("Player wins!")
                .jsonPath("$.playerValue").isEqualTo(19);
    }

    @Test
    void deleteGame_ShouldReturn204() {
        when(gameService.deleteGame("g1"))
                .thenReturn(Mono.empty());

        webTestClient.delete().uri("/g1/delete")
                .exchange()
                .expectStatus().isNoContent();

        verify(gameService, times(1)).deleteGame("g1");
    }

    @Test
    void deleteGame_ShouldReturn404_WhenGameNotFound() {
        when(gameService.deleteGame("missing"))
                .thenReturn(Mono.error(new NotFoundException("Game not found")));

        webTestClient.delete().uri("/missing/delete")
                .exchange()
                .expectStatus().isNotFound();
    }

}
