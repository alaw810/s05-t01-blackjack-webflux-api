package cat.itacademy.s05.t01.blackjack.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private String timestamp;
    private String error;
    private String message;
    private String path;
}
