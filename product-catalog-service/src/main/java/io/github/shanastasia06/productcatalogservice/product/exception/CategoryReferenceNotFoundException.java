package io.github.shanastasia06.productcatalogservice.product.exception;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.common.exception.DomainException;

public class CategoryReferenceNotFoundException extends DomainException {
    public CategoryReferenceNotFoundException(CategoryId id) {
        super("Category <" + id.toString() + "> referenced by the product does not exist");
    }
}
