package cat.itacademy.s05.t01.blackjack.controller;

import cat.itacademy.s05.t01.blackjack.dto.*;
import cat.itacademy.s05.t01.blackjack.exception.ValidationException;
import cat.itacademy.s05.t01.blackjack.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<NewGameResponse> createNewGame(@Valid @RequestBody NewGameRequest request) {
        return gameService.createNewGame(request);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<GameDetailsResponse> getGame(@PathVariable String id) {
        return gameService.getGame(id);
    }

    @PostMapping("/{id}/play")
    @ResponseStatus(HttpStatus.OK)
    public Mono<PlayResultDTO> playMove(
            @PathVariable String id,
            @Valid @RequestBody PlayRequestDTO request
    ) {
        return gameService.playMove(id, request);
    }

    @DeleteMapping("/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteGame(@PathVariable String id) {
        return gameService.deleteGame(id);
    }
}
