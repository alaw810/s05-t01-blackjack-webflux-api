package cat.itacademy.s05.t01.blackjack.controller;

import cat.itacademy.s05.t01.blackjack.dto.PlayerResponse;
import cat.itacademy.s05.t01.blackjack.dto.PlayerUpdateRequest;
import cat.itacademy.s05.t01.blackjack.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/player")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<PlayerResponse> updatePlayerName(
            @PathVariable Long id,
            @RequestBody PlayerUpdateRequest request
    ) {
        return playerService.updatePlayerName(id, request);
    }
}
