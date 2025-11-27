package cat.itacademy.s05.t01.blackjack.dto;

import cat.itacademy.s05.t01.blackjack.model.mongo.Move;
import jakarta.validation.constraints.NotNull;

public record PlayRequestDTO(
        @NotNull(message = "Move must be provided")
        Move move
) { }
