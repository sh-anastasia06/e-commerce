package io.github.shanastasia06.productcatalogservice.category.exception;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.common.exception.DomainException;

public class CategoryInUseException extends DomainException {
    public CategoryInUseException(CategoryId id) {
        super("Category <" + id + "> cannot be deleted because products still reference it");
    }
}
