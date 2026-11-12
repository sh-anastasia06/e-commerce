package io.github.shanastasia06.productcatalogservice.category.infrastructure;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.category.service.CategoryUsageChecker;
import io.github.shanastasia06.productcatalogservice.product.infrastructure.SpringDataProductRepository;
import org.springframework.stereotype.Component;

@Component
class CategoryUsageAdapter implements CategoryUsageChecker {
    private final SpringDataProductRepository productRepository;

    CategoryUsageAdapter(SpringDataProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public boolean isInUse(CategoryId categoryId) {
        return productRepository.existsByCategoryId(categoryId.value());
    }
}
