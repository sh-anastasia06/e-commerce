package io.github.shanastasia06.productcatalogservice.common.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;

public record PageQuery(int page, int size) {
    public static final int MAX_SIZE = 100;

    public PageQuery {
        if (page < 0) {
            throw new DomainValidationException("Page must not be negative");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new DomainValidationException("Size must be between 1 and " + MAX_SIZE);
        }
    }
}
