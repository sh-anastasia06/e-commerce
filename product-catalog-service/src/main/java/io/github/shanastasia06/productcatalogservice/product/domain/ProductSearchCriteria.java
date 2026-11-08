package io.github.shanastasia06.productcatalogservice.product.domain;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;

public record ProductSearchCriteria(CategoryId categoryId, Money minPrice, Money maxPrice) {
    public ProductSearchCriteria {
        if (minPrice != null && maxPrice != null) {
            if (minPrice.currency() != maxPrice.currency()) {
                throw new DomainValidationException("Min Price and Max Price must be in equal currency");
            }
            if (minPrice.isGreaterThan(maxPrice)) {
                throw new DomainValidationException("Max Price must be greater than or equal to Min Price");
            }
        }
    }
}
