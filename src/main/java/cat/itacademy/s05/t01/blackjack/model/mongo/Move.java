package cat.itacademy.s05.t01.blackjack.model.mongo;

import cat.itacademy.s05.t01.blackjack.exception.ValidationException;

public enum Move {
    HIT, STAND;

    public static Move from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Move must be provided");
        }

        try {
            return Move.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Move is invalid");
        }
    }
}
