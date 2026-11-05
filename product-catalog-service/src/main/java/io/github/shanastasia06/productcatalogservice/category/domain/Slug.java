package io.github.shanastasia06.productcatalogservice.category.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;

import java.util.regex.Pattern;

public record Slug(String value) {
    private static final Pattern PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    private static final int MAX_LENGTH = 100;

    public Slug {
        if  (value == null) {
            throw new DomainValidationException("Slug value must not be null");
        }
        if (value.isEmpty()) {
            throw new DomainValidationException("Slug value must not be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new DomainValidationException("Slug value must not be longer than 100 characters");
        }
        if (!isValid(value)) {
            throw new DomainValidationException("Slug value is not valid");
        }
    }

    private static boolean isValid(String value) {
        return PATTERN.matcher(value).matches();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
