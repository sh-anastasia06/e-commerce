package io.github.shanastasia06.productcatalogservice.category.exception;

import io.github.shanastasia06.productcatalogservice.category.domain.Slug;
import io.github.shanastasia06.productcatalogservice.common.exception.DomainException;

public class SlugAlreadyExistsException extends DomainException {
    public SlugAlreadyExistsException(Slug slug) {
        super("Category with Slug \\'" + slug + "\\' already exists");
    }
}
