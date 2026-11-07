package io.github.shanastasia06.productcatalogservice.category.exception;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.common.exception.DomainException;

public class CategoryNotFoundException extends DomainException {
    public CategoryNotFoundException(CategoryId categoryId) {
        super("Category with ID " + categoryId + " was not found");
    }
}
