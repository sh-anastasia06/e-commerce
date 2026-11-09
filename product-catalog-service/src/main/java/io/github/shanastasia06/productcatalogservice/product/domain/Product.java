package io.github.shanastasia06.productcatalogservice.product.domain;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import io.github.shanastasia06.productcatalogservice.product.exception.InvalidProductStateException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product {
    public static final int DESCRIPTION_MAX_LENGTH = 2000;

    @EqualsAndHashCode.Include
    private final ProductId id;
    private Sku sku;
    private ProductName name;
    private String description;
    private Money price;
    private CategoryId categoryId;
    private Attributes attributes;
    private ProductStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    // is null for a product that has not been persisted yet
    private final Long version;

    private Product(ProductId id, Sku sku, ProductName name, String description, Money price,
                    CategoryId categoryId, Attributes attributes, ProductStatus status, Instant createdAt, Instant updatedAt, Long version) {
        this.id = Objects.requireNonNull(id, "Product id must not be null");
        this.sku = Objects.requireNonNull(sku, "Product sku must not be null");
        this.name = Objects.requireNonNull(name, "Product name must not be null");
        this.description = description;
        this.price = Objects.requireNonNull(price, "Product price must not be null");
        this.categoryId = Objects.requireNonNull(categoryId, "Product category id must not be null");
        this.status = Objects.requireNonNull(status, "Product status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Product createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Product updatedAt must not be null");
        this.version = version;
        this.attributes = Objects.requireNonNull(attributes, "Product attributes must not be null");
    }

    public static Product create(Sku sku, ProductName name, String description,
                                 Money price, CategoryId categoryId, Attributes attributes, Instant now) {
        return new Product(ProductId.generate(), sku, name, normalizeDescription(description), price, categoryId, attributes, ProductStatus.DRAFT, now, now, null);
    }

    public static Product restore(ProductId id, Sku sku, ProductName name, String description,
                                  Money price, CategoryId categoryId, Attributes attributes, ProductStatus status, Instant createdAt, Instant updatedAt, Long version) {
        return new Product(id, sku, name, normalizeDescription(description), price, categoryId, attributes, status, createdAt, updatedAt, version);
    }

    public void activate(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        if (!status.canBeActivated()) {
            throw new InvalidProductStateException("Product " + id + " cannot be activated from status " + status);
        }
        if (!price.isPositive()) {
            throw new InvalidProductStateException("Product " + id + " cannot be activated with a zero price");
        }
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = now;
    }

    public void archive(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        if (!status.canBeArchived()) {
            throw new InvalidProductStateException("Product " + id + " cannot be archived from status " + status);
        }
        this.status = ProductStatus.ARCHIVED;
        this.updatedAt = now;
    }

    public void update(Sku sku, ProductName name, String description,
                       Money price, CategoryId categoryId, Attributes attributes, Instant now) {
        Objects.requireNonNull(sku, "Product sku must not be null");
        Objects.requireNonNull(name, "Product name must not be null");
        Objects.requireNonNull(price, "Product price must not be null");
        Objects.requireNonNull(categoryId, "Product category id must not be null");
        Objects.requireNonNull(attributes, "Product attributes must not be null");
        Objects.requireNonNull(now, "now must not be null");
        String normalizedDescription = normalizeDescription(description);

        if (!status.canBeEdited()) {
            throw new InvalidProductStateException("Product " + id + " is archived and cannot be modified");
        }
        if (status == ProductStatus.ACTIVE && !price.isPositive()) {
            throw new InvalidProductStateException("Product " + id + " is active and must have a price greater than zero");
        }

        this.sku = sku;
        this.name = name;
        this.description = normalizedDescription;
        this.price = price;
        this.categoryId = categoryId;
        this.attributes = attributes;
        this.updatedAt = now;
    }

    // aggregate cannot delete itself
    public void ensureCanBeDeleted() {
        if (!status.canBeDeleted()) {
            throw new InvalidProductStateException(
                    "Product " + id + " cannot be deleted from status " + status + ", archive it instead");
        }
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String stripped = description.strip();
        if (stripped.isEmpty()) {
            return null;
        }
        if (stripped.length() > DESCRIPTION_MAX_LENGTH) {
            throw new DomainValidationException(
                    "Product description must not be longer than " + DESCRIPTION_MAX_LENGTH + " characters"
            );
        }

        return stripped;
    }
}
