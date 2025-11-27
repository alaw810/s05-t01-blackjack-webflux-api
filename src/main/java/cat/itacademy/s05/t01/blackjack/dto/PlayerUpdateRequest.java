package cat.itacademy.s05.t01.blackjack.dto;

import jakarta.validation.constraints.NotBlank;

public record PlayerUpdateRequest(
        @NotBlank(message = "Name cannot be empty")
        String newName
) { }
