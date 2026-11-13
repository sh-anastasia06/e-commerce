package io.github.shanastasia06.productcatalogservice.common.api;

public enum ErrorCode {
    VALIDATION_ERROR,
    PRODUCT_NOT_FOUND,
    CATEGORY_NOT_FOUND,
    SKU_ALREADY_EXISTS,
    SLUG_ALREADY_EXISTS,
    INVALID_PRODUCT_STATE,
    CATEGORY_IN_USE,
    CONCURRENT_MODIFICATION
}
