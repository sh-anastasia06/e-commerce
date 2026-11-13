package io.github.shanastasia06.productcatalogservice.product.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;

public record ProductName(String value) {
    private static final int MAX_LENGTH = 200;

    public ProductName {
        if (value == null) {
            throw new DomainValidationException("Product name must not be null");
        }
        value = value.strip();
        if (value.isEmpty()) {
            throw new DomainValidationException("Product name must not be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new DomainValidationException("Product name must not be longer than " + MAX_LENGTH + " characters");
        }
    }
}
