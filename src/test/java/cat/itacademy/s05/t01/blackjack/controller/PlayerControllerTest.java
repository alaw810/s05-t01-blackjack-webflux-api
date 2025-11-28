package cat.itacademy.s05.t01.blackjack.controller;

import cat.itacademy.s05.t01.blackjack.dto.PlayerRankingResponse;
import cat.itacademy.s05.t01.blackjack.dto.PlayerResponse;
import cat.itacademy.s05.t01.blackjack.dto.PlayerUpdateRequest;
import cat.itacademy.s05.t01.blackjack.exception.GlobalExceptionHandler;
import cat.itacademy.s05.t01.blackjack.exception.NotFoundException;
import cat.itacademy.s05.t01.blackjack.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerControllerTest {

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private PlayerController playerController;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
                .bindToController(playerController)
                .controllerAdvice(new GlobalExceptionHandler())
                .configureClient()
                .baseUrl("/player")
                .build();
    }

    @Test
    void updatePlayerName_ShouldReturn200AndUpdatedPlayer() {
        PlayerResponse response = PlayerResponse.builder()
                .id(1L)
                .name("NewName")
                .gamesPlayed(3)
                .gamesWon(2)
                .gamesLost(1)
                .build();

        when(playerService.updatePlayerName(eq(1L), any()))
                .thenReturn(Mono.just(response));

        webTestClient.put()
                .uri("/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PlayerUpdateRequest("NewName"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("NewName");
    }

    @Test
    void updatePlayerName_ShouldReturn404_WhenPlayerNotFound() {
        when(playerService.updatePlayerName(eq(50L), any()))
                .thenReturn(Mono.error(new NotFoundException("Player not found")));

        webTestClient.put()
                .uri("/50")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PlayerUpdateRequest("Any"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updatePlayerName_ShouldCallServiceWithCorrectArguments() {
        when(playerService.updatePlayerName(eq(10L), any()))
                .thenReturn(Mono.just(
                        PlayerResponse.builder().id(10L).name("Test").build()
                ));

        PlayerUpdateRequest request = new PlayerUpdateRequest("Test");

        webTestClient.put()
                .uri("/10")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        verify(playerService, times(1)).updatePlayerName(10L, request);
    }

    @Test
    void updatePlayerName_ShouldReturn400_WhenNameIsEmpty() {
        webTestClient.put()
                .uri("/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PlayerUpdateRequest(""))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Name cannot be empty");
    }

    @Test
    void updatePlayerName_ShouldReturn500_OnUnexpectedError() {
        when(playerService.updatePlayerName(eq(1L), any()))
                .thenReturn(Mono.error(new RuntimeException("Boom")));

        webTestClient.put().uri("/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PlayerUpdateRequest("Test"))
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.error").isEqualTo("INTERNAL_SERVER_ERROR")
                .jsonPath("$.message").isEqualTo("Boom");
    }

    @Test
    void getRanking_ShouldReturn200AndListOfPlayers() {
        PlayerRankingResponse r1 = PlayerRankingResponse.builder()
                .id(1L).name("Alice").gamesWon(7).build();

        PlayerRankingResponse r2 = PlayerRankingResponse.builder()
                .id(2L).name("Bob").gamesWon(9).build();

        when(playerService.getRanking())
                .thenReturn(Flux.just(r1, r2));

        webTestClient.get()
                .uri("/ranking")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo("Alice");
    }

}
