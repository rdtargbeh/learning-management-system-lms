package learning_management_system.backend.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(OffsetDateTime timestamp, int status, String code, String message, Map<String, String> fieldErrors) {
    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(OffsetDateTime.now(), status, code, message, null);
    }
}
