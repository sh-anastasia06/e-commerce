package io.github.shanastasia06.productcatalogservice.common.api;

import io.github.shanastasia06.productcatalogservice.category.exception.CategoryNotFoundException;
import io.github.shanastasia06.productcatalogservice.category.exception.SlugAlreadyExistsException;
import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(CategoryNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ErrorCode.CATEGORY_NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ErrorDetail> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> new ErrorDetail(e.getField(), e.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, ex.getMessage(), details);
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ErrorResponse> handleDomainValidation(DomainValidationException ex) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, ex.getMessage(), null);
    }

    @ExceptionHandler(SlugAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleSlugAlreadyExists(SlugAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ErrorCode.SLUG_ALREADY_EXISTS, ex.getMessage(), null);
    }

    // malformed uuid in path
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, ex.getMessage(), null);
    }

    // helper
    private ResponseEntity<ErrorResponse> build(HttpStatus status, ErrorCode code,
                                                String message, List<ErrorDetail> details) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), code.name(), message, Instant.now(), details));
    }
}
