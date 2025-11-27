package cat.itacademy.s05.t01.blackjack.dto;

import jakarta.validation.constraints.NotBlank;

public record PlayRequestDTO(
        @NotBlank(message = "Move cannot be empty")
        String move
) { }
