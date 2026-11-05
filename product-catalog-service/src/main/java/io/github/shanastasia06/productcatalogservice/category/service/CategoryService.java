package io.github.shanastasia06.productcatalogservice.category.service;

import io.github.shanastasia06.productcatalogservice.category.domain.Category;
import io.github.shanastasia06.productcatalogservice.category.domain.CategoryName;
import io.github.shanastasia06.productcatalogservice.category.domain.Slug;
import io.github.shanastasia06.productcatalogservice.category.exception.SlugAlreadyExistsException;
import io.github.shanastasia06.productcatalogservice.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category create(String name, String slug, String description) {
        CategoryName categoryName = new CategoryName(name);
        Slug categorySlug = new Slug(slug);

        if (categoryRepository.existsBySlug(categorySlug)) {
            throw new SlugAlreadyExistsException(categorySlug);
        }

        Category category = Category.create(categoryName, categorySlug, description);
        return categoryRepository.save(category);
    }
}
