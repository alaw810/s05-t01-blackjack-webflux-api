package cat.itacademy.s05.t01.blackjack.controller;

import cat.itacademy.s05.t01.blackjack.dto.GameDetailsResponse;
import cat.itacademy.s05.t01.blackjack.dto.NewGameRequest;
import cat.itacademy.s05.t01.blackjack.dto.NewGameResponse;
import cat.itacademy.s05.t01.blackjack.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
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
                .bodyValue("{\"playerName\": \"Alice\"}")
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
                .bodyValue("{\"playerName\": \"Charlie\"}")
                .exchange()
                .expectStatus().isCreated();

        verify(gameService, times(1)).createNewGame(any(NewGameRequest.class));
    }

    @Test
    void createNewGame_ShouldReturn400_WhenNameIsEmpty() {
        webTestClient.post().uri("/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"playerName\": \"\"}")
                .exchange()
                .expectStatus().isBadRequest();
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
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));

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

}
