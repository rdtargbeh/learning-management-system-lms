package learning_management_system.backend.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> api(ApiException e) {
        return ResponseEntity.status(e.status()).body(ErrorResponse.of(e.status().value(), e.code(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErrorResponse(OffsetDateTime.now(), 400, "VALIDATION_FAILED",
                "Request validation failed", fields));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> unreadable(Exception e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(400, "BAD_REQUEST", "Malformed request: " + rootMessage(e)));
    }

    /** Unique keys, foreign keys and CHECK constraints from the database surface as 409 with the constraint name. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integrity(DataIntegrityViolationException e) {
        String constraint = null;
        Throwable t = e;
        while (t != null) {
            if (t instanceof ConstraintViolationException cve) { constraint = cve.getConstraintName(); break; }
            t = t.getCause();
        }
        String msg = constraint != null ? "Rejected by database rule " + constraint : "Rejected by a database rule";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of(409, "CONSTRAINT_VIOLATION", msg));
    }

    /** Trigger errors (RAISE EXCEPTION) and row-level security denials arrive as JPA system exceptions. */
    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ErrorResponse> jpa(JpaSystemException e) {
        String m = rootMessage(e);
        if (m != null && m.contains("row-level security")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.of(403, "TENANT_ISOLATION", "Not allowed in this tenant"));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of(409, "DATABASE_RULE", m));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> denied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.of(403, "FORBIDDEN", "You do not have access to this action"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> other(Exception e) {
        String m = rootMessage(e);
        if (m != null && m.contains("row-level security")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.of(403, "TENANT_ISOLATION", "Not allowed in this tenant"));
        }
        log.error("Unhandled error", e);
        return ResponseEntity.internalServerError().body(ErrorResponse.of(500, "INTERNAL_ERROR", "Unexpected error"));
    }

    private static String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) t = t.getCause();
        String m = t.getMessage();
        if (m != null && m.startsWith("ERROR: ")) m = m.substring(7);
        if (m != null && m.contains("\n")) m = m.substring(0, m.indexOf('\n'));
        return m;
    }
}
