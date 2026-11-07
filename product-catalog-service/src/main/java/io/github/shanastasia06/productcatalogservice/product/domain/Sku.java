package io.github.shanastasia06.productcatalogservice.product.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;

import java.util.regex.Pattern;

public record Sku(String value) {
    private static final Pattern PATTERN = Pattern.compile("^[A-Z0-9]+(-[A-Z0-9]+)*$");
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 64;

    public Sku {
        if (value == null) {
            throw new DomainValidationException("Sku value must not be null");
        }
        if (value.isEmpty()) {
            throw new DomainValidationException("Sku value must not be empty");
        }
        if (value.length() > MAX_LENGTH || value.length() < MIN_LENGTH) {
            throw new DomainValidationException("Sku value must not be from" + MIN_LENGTH + " to " + MAX_LENGTH + " characters");
        }
        if (!isValid(value)) {
            throw new DomainValidationException("Sku value is not valid");
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
