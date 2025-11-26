package cat.itacademy.s05.t01.blackjack.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
