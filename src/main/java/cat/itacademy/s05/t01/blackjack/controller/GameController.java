package cat.itacademy.s05.t01.blackjack.controller;

import cat.itacademy.s05.t01.blackjack.dto.NewGameRequest;
import cat.itacademy.s05.t01.blackjack.dto.NewGameResponse;
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
}
