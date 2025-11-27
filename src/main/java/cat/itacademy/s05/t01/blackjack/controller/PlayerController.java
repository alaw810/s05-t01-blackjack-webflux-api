package cat.itacademy.s05.t01.blackjack.controller;

import cat.itacademy.s05.t01.blackjack.dto.PlayerRankingResponse;
import cat.itacademy.s05.t01.blackjack.dto.PlayerResponse;
import cat.itacademy.s05.t01.blackjack.dto.PlayerUpdateRequest;
import cat.itacademy.s05.t01.blackjack.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
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
            @Valid @RequestBody PlayerUpdateRequest request
    ) {
        return playerService.updatePlayerName(id, request);
    }

    @GetMapping("/ranking")
    @ResponseStatus(HttpStatus.OK)
    public Flux<PlayerRankingResponse> getRanking() {
        return playerService.getRanking();
    }

}
