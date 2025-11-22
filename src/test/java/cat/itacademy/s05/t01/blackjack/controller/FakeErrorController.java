package cat.itacademy.s05.t01.blackjack.controller;

import cat.itacademy.s05.t01.blackjack.exception.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FakeErrorController {

    @GetMapping("/fake/notfound")
    public String triggerNotFound() {
        throw new NotFoundException("Resource not found");
    }

    @GetMapping("/fake/invalid")
    public String triggerInvalidMove() {
        throw new InvalidMoveException("Invalid move");
    }

    @GetMapping("/fake/validation")
    public String triggerValidation() {
        throw new ValidationException("Invalid input");
    }

    @GetMapping("/fake/error")
    public String triggerGenericError() {
        throw new RuntimeException("Unexpected error");
    }
}
