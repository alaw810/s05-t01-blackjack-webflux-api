package cat.itacademy.s05.t01.blackjack.controller;

import cat.itacademy.s05.t01.blackjack.dto.*;
import cat.itacademy.s05.t01.blackjack.service.GameService;
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
    public Mono<NewGameResponse> createNewGame(@RequestBody NewGameRequest request) {
        if (request.playerName() == null || request.playerName().trim().isEmpty()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Player name cannot be empty"
            ));
        }
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
            @RequestBody PlayRequestDTO request
    ) {

        if (request == null || request.move() == null || request.move().trim().isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Move cannot be empty"));
        }

        return gameService.playMove(id, request);
    }

    @DeleteMapping("/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteGame(@PathVariable String id) {
        return gameService.deleteGame(id);
    }
}
