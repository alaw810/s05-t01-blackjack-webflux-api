package cat.itacademy.s05.t01.blackjack.dto;

import jakarta.validation.constraints.NotNull;

public record PlayRequestDTO(
        @NotNull(message = "Move must be provided")
        String move
) { }
