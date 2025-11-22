package cat.itacademy.s05.t01.blackjack.exception;

import cat.itacademy.s05.t01.blackjack.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponse buildErrorResponse(HttpStatus status, Throwable ex, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();

        return ErrorResponse.builder()
                .timestamp(OffsetDateTime.now().toString())
                .error(status.name())
                .message(ex.getMessage())
                .path(path)
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse body = buildErrorResponse(status, ex, exchange);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler({InvalidMoveException.class, ValidationException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse body = buildErrorResponse(status, ex, exchange);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse body = buildErrorResponse(status, ex, exchange);
        return ResponseEntity.status(status).body(body);
    }
}
