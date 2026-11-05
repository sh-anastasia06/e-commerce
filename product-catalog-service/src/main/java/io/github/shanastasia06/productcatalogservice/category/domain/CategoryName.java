package io.github.shanastasia06.productcatalogservice.category.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;

public record CategoryName(String value) {
    private static final int MAX_LENGTH = 100;

    public CategoryName {
        if (value == null) {
            throw new DomainValidationException("Category name must not be null");
        }
        value = value.strip();
        if (value.isEmpty()) {
            throw new DomainValidationException("Category name must not be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new DomainValidationException(
                    "Category name must not be longer than " + MAX_LENGTH + " characters");
        }
    }
}
