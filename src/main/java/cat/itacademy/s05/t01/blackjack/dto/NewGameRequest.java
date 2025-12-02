package cat.itacademy.s05.t01.blackjack.dto;

import jakarta.validation.constraints.NotBlank;

public record NewGameRequest(
        @NotBlank(message = "Player name cannot be empty")
        String playerName
) {}
