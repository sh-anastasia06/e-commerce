package io.github.shanastasia06.productcatalogservice.category.service;

import io.github.shanastasia06.productcatalogservice.category.domain.*;
import io.github.shanastasia06.productcatalogservice.category.exception.CategoryNotFoundException;
import io.github.shanastasia06.productcatalogservice.category.exception.SlugAlreadyExistsException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/*
 * Takes plain data types and wraps into domain data types
 */

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

    public Category getById(UUID id) {
        return findOrThrow(new CategoryId(id));
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category update(UUID id, String name, String slug, String description) {
        CategoryName newName = new CategoryName(name);
        Slug newSlug = new Slug(slug);

        Category category = findOrThrow(new CategoryId(id));

        if (categoryRepository.existsBySlugAndIdNot(newSlug, category.getId())) {
            throw new SlugAlreadyExistsException(newSlug);
        }
        // business rule
        category.update(newName, newSlug, description);
        return categoryRepository.save(category);
    }

    /*
     * TODO: delete must refuse when products reference the category (CATEGORY_IN_USE)
     *  add when product domain will be done
     */
    public void delete(UUID id) {
        CategoryId categoryId = new CategoryId(id);
        // checks if document exists
        findOrThrow(categoryId);
        categoryRepository.deleteById(categoryId);
    }

    // helper method
    private Category findOrThrow(CategoryId id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
