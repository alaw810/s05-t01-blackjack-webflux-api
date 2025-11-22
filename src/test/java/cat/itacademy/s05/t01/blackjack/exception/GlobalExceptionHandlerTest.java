package cat.itacademy.s05.t01.blackjack.exception;

import cat.itacademy.s05.t01.blackjack.controller.FakeErrorController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

class GlobalExceptionHandlerTest {

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(new FakeErrorController())
                .controllerAdvice(new GlobalExceptionHandler())
                .configureClient()
                .build();
    }

    @Test
    void handleNotFoundException_ShouldReturn404WithJson() {
        webTestClient.get()
                .uri("/fake/notfound")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Resource not found")
                .jsonPath("$.path").isEqualTo("/fake/notfound");
    }

    @Test
    void handleInvalidMoveException_ShouldReturn400WithJson() {
        webTestClient.get()
                .uri("/fake/invalid")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("BAD_REQUEST")
                .jsonPath("$.message").isEqualTo("Invalid move");
    }

    @Test
    void handleValidationException_ShouldReturn400WithJson() {
        webTestClient.get()
                .uri("/fake/validation")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid input");
    }

    @Test
    void handleGenericException_ShouldReturn500WithJson() {
        webTestClient.get()
                .uri("/fake/error")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.error").isEqualTo("INTERNAL_SERVER_ERROR")
                .jsonPath("$.message").isEqualTo("Unexpected error");
    }
}
