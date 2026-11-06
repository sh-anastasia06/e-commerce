package io.github.shanastasia06.productcatalogservice.category.domain;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(CategoryId id);
    List<Category> findAll();
    boolean existsBySlug(Slug slug);
    boolean existsBySlugAndIdNot(Slug slug, CategoryId id);
    void deleteById(CategoryId id);
}
