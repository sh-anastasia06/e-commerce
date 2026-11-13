package io.github.shanastasia06.productcatalogservice.product.infrastructure;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.category.domain.CategoryRepository;
import io.github.shanastasia06.productcatalogservice.product.service.CategoryExistenceChecker;
import org.springframework.stereotype.Component;

@Component
public class CategoryExistenceAdapter implements CategoryExistenceChecker {
    private final CategoryRepository categoryRepository;

    public CategoryExistenceAdapter(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public boolean exists(CategoryId categoryId) {
        return categoryRepository.findById(categoryId).isPresent();
    }
}
