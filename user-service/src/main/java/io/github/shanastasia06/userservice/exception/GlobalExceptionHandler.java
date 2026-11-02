package io.github.shanastasia06.userservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // validation error -> 400 Bad Request + details
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ErrorDetail> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> new ErrorDetail(e.getField(), e.getDefaultMessage()))
                .toList();

        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid input parameters", details);
    }

    // includes 409 Conflict for duplicates
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String code = status instanceof HttpStatus s ? s.name() : "HTTP_" + status.value();
        String message = ex.getReason() != null ? ex.getReason() : "Request failed";

        return build(status, code, message);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password");
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "Email already exists");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found");
    }

    // catches all unexpected db exceptions
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);   // details stay in the server log

        if (ex.getRootCause() instanceof PSQLException psqlException) {
            String sqlState = psqlException.getSQLState();

            if ("23505".equals(sqlState)) {         // unique violation
                return build(HttpStatus.CONFLICT, "DATA_CONFLICT",
                        "A record with these unique values already exists");
            }
            if ("22001".equals(sqlState)) {         // value too long
                return build(HttpStatus.BAD_REQUEST, "FIELD_TOO_LONG",
                        "A field value is too long");
            }
        }
        return build(HttpStatus.BAD_REQUEST, "DATA_INTEGRITY_VIOLATION",
                "Database integrity violation");
    }

    // helpers
    private ResponseEntity<ErrorResponse> build(HttpStatusCode status, String code, String message) {
        return build(status, code, message, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatusCode status, String code, String message,
                                                List<ErrorDetail> details) {
        ErrorResponse body = new ErrorResponse(status.value(), code, message, Instant.now(), details);
        return ResponseEntity.status(status).body(body);
    }
}
