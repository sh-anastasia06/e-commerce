package io.github.shanastasia06.productcatalogservice.category.api;

import io.github.shanastasia06.productcatalogservice.category.exception.CategoryInUseException;
import io.github.shanastasia06.productcatalogservice.category.exception.CategoryNotFoundException;
import io.github.shanastasia06.productcatalogservice.category.exception.SlugAlreadyExistsException;
import io.github.shanastasia06.productcatalogservice.common.api.ErrorCode;
import io.github.shanastasia06.productcatalogservice.common.api.ErrorResponse;
import io.github.shanastasia06.productcatalogservice.common.api.ErrorResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "io.github.shanastasia06.productcatalogservice.category")
class CategoryExceptionHandler {
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFoundException(CategoryNotFoundException e) {
        return ErrorResponses.build(HttpStatus.NOT_FOUND, ErrorCode.CATEGORY_NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(SlugAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleSlugAlreadyExistsException(SlugAlreadyExistsException e) {
        return ErrorResponses.build(HttpStatus.CONFLICT, ErrorCode.SLUG_ALREADY_EXISTS, e.getMessage(), null);
    }

    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<ErrorResponse> handleCategoryInUseException(CategoryInUseException e) {
        return ErrorResponses.build(HttpStatus.CONFLICT, ErrorCode.CATEGORY_IN_USE, e.getMessage(), null);
    }
}
