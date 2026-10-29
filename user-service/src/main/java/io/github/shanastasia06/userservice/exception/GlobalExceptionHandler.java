package io.github.shanastasia06.userservice.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // validation error -> 400 Bad Request + details
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex){
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation filed",
                details
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // includes 409 Conflict for duplicates
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex){
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                ex.getStatusCode().value(),
                ex.getReason() != null ? ex.getReason() : ex.getMessage(),
                List.of()
        );

        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex){
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "INVALID_CREDENTIALS",
                List.of("Invalid email or password")
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex){
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "EMAIL_ALREADY_EXISTS",
                List.of("Email already exists")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // catches all unexpected db exceptions
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorMessage = "Database integrity violation";
        List<String> details = new ArrayList<>();

        // root cause
        Throwable rootCause = ex.getRootCause();

        if (rootCause instanceof org.postgresql.util.PSQLException psqlException) {
            String sqlState = psqlException.getSQLState();

            // unique index violation
            if ("23505".equals(sqlState)) {
                status = HttpStatus.CONFLICT;
                errorMessage = "Database conflict: A record with these unique fields already exists";
            }
            // string length truncation
            else if ("22001".equals(sqlState)) {
                status = HttpStatus.BAD_REQUEST;
                errorMessage = "Field length exceeded";
            }

            details.add(psqlException.getServerErrorMessage() != null ? psqlException.getServerErrorMessage().getMessage() : psqlException.getMessage());
        } else {
            details.add(ex.getMostSpecificCause().getMessage());
        }

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                errorMessage,
                details
        );

        return ResponseEntity.status(status).body(errorResponse);
    }
}
