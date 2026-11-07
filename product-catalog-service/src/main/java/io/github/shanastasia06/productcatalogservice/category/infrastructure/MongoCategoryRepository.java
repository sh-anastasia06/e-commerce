package io.github.shanastasia06.productcatalogservice.category.infrastructure;

import org.springframework.dao.DuplicateKeyException;
import io.github.shanastasia06.productcatalogservice.category.domain.Category;
import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.category.domain.CategoryRepository;
import io.github.shanastasia06.productcatalogservice.category.domain.Slug;
import io.github.shanastasia06.productcatalogservice.category.exception.SlugAlreadyExistsException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class MongoCategoryRepository implements CategoryRepository {
    private final SpringDataCategoryRepository springDataRepository;
    private final CategoryMapper categoryMapper;

    MongoCategoryRepository(SpringDataCategoryRepository springDataRepository, CategoryMapper categoryMapper) {
        this.springDataRepository = springDataRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public Category save(Category category) {
        try {
            CategoryDocument saved = springDataRepository.save(categoryMapper.toDocument(category));
            return categoryMapper.toDomain(saved);
        } catch (DuplicateKeyException e) {
            throw new SlugAlreadyExistsException(category.getSlug());
        }
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return springDataRepository.findById(id.value()).map(categoryMapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return springDataRepository.findAll().stream().map(categoryMapper::toDomain).toList();
    }

    @Override
    public boolean existsBySlug(Slug slug) {
        return springDataRepository.existsBySlug(slug.value());
    }

    @Override
    public boolean existsBySlugAndIdNot(Slug slug, CategoryId id) {
        return springDataRepository.existsBySlugAndIdNot(slug.value(), id.value());
    }

    @Override
    public void deleteById(CategoryId id) {
        springDataRepository.deleteById(id.value());
    }
}
