package io.github.shanastasia06.productcatalogservice.product.service;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;

public interface CategoryExistenceChecker {
    boolean exists(CategoryId categoryId);
}
