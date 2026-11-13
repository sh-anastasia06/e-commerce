package io.github.shanastasia06.productcatalogservice.common.api;

import io.github.shanastasia06.productcatalogservice.common.exception.ConcurrentUpdateException;
import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ErrorDetail> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> new ErrorDetail(e.getField(), e.getDefaultMessage()))
                .toList();
        return ErrorResponses.build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                "Invalid input parameters", details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ErrorResponses.build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                "Invalid value for parameter '" + ex.getName() + "'", null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        return ErrorResponses.build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                "Malformed request body", null);
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ErrorResponse> handleDomainValidation(DomainValidationException ex) {
        return ErrorResponses.build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, ex.getMessage(), null);
    }

    @ExceptionHandler(ConcurrentUpdateException.class)
    public ResponseEntity<ErrorResponse> handleConcurrentUpdate(ConcurrentUpdateException ex) {
        return ErrorResponses.build(HttpStatus.CONFLICT, ErrorCode.CONCURRENT_MODIFICATION, ex.getMessage(), null);
    }
}
