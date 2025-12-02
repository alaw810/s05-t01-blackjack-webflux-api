package cat.itacademy.s05.t01.blackjack.exception;

import cat.itacademy.s05.t01.blackjack.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponse buildErrorResponse(HttpStatus status, Throwable ex, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();

        return ErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .error(status.name())
                .message(ex.getMessage())
                .path(path)
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNotFound(NotFoundException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse body = buildErrorResponse(status, ex, exchange);
        return Mono.just(ResponseEntity.status(status).body(body));
    }

    @ExceptionHandler({InvalidMoveException.class, ValidationException.class, IllegalStateException.class})
    public Mono<ResponseEntity<ErrorResponse>> handleBadRequest(RuntimeException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse body = buildErrorResponse(status, ex, exchange);
        return Mono.just(ResponseEntity.status(status).body(body));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArg(IllegalArgumentException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse body = buildErrorResponse(status, ex, exchange);
        return Mono.just(ResponseEntity.status(status).body(body));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBindException(WebExchangeBindException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        String msg = ex.getFieldErrors().isEmpty()
                ? "Validation failed"
                : ex.getFieldErrors().get(0).getDefaultMessage();

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .error(status.name())
                .message(msg)
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity.status(status).body(body));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneric(Exception ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse body = buildErrorResponse(status, ex, exchange);
        return Mono.just(ResponseEntity.status(status).body(body));
    }
}
