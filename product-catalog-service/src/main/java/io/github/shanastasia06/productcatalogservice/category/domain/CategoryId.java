package io.github.shanastasia06.productcatalogservice.category.domain;

import java.util.Objects;
import java.util.UUID;

public record CategoryId(UUID value) {
    public CategoryId {
        Objects.requireNonNull(value, "category id must not be null");
    }

    public static CategoryId generate() {
        return new CategoryId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
