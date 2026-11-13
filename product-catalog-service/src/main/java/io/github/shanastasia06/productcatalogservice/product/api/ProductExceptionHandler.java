package io.github.shanastasia06.productcatalogservice.product.api;

import io.github.shanastasia06.productcatalogservice.common.api.ErrorCode;
import io.github.shanastasia06.productcatalogservice.common.api.ErrorResponse;
import io.github.shanastasia06.productcatalogservice.common.api.ErrorResponses;
import io.github.shanastasia06.productcatalogservice.product.exception.CategoryReferenceNotFoundException;
import io.github.shanastasia06.productcatalogservice.product.exception.InvalidProductStateException;
import io.github.shanastasia06.productcatalogservice.product.exception.ProductNotFoundException;
import io.github.shanastasia06.productcatalogservice.product.exception.SkuAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "io.github.shanastasia06.productcatalogservice.product")
class ProductExceptionHandler {
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException e) {
        return ErrorResponses.build(HttpStatus.NOT_FOUND, ErrorCode.PRODUCT_NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(CategoryReferenceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryReferenceNotFoundException(CategoryReferenceNotFoundException e) {
        return ErrorResponses.build(HttpStatus.NOT_FOUND, ErrorCode.CATEGORY_NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(SkuAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleSkuAlreadyExistsException(SkuAlreadyExistsException e) {
        return ErrorResponses.build(HttpStatus.CONFLICT, ErrorCode.SKU_ALREADY_EXISTS, e.getMessage(), null);
    }

    @ExceptionHandler(InvalidProductStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidProductStateException(InvalidProductStateException e) {
        return ErrorResponses.build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_PRODUCT_STATE, e.getMessage(), null);
    }
}
