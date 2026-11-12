package io.github.shanastasia06.productcatalogservice.category.service;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;

public interface CategoryUsageChecker {
    boolean isInUse(CategoryId categoryId);
}
