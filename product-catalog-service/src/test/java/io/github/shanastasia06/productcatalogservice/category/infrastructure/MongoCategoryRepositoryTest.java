package io.github.shanastasia06.productcatalogservice.category.infrastructure;

import io.github.shanastasia06.productcatalogservice.TestcontainersConfiguration;
import io.github.shanastasia06.productcatalogservice.category.domain.*;
import io.github.shanastasia06.productcatalogservice.category.exception.SlugAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

// stats only the mongo layer
@DataMongoTest
@Import({TestcontainersConfiguration.class, MongoCategoryRepository.class, CategoryMapperImpl.class})
public class MongoCategoryRepositoryTest {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private SpringDataCategoryRepository springDataRepository;

    @BeforeEach
    void cleanUp() {
        springDataRepository.deleteAll();
    }

    @Test
    void save_validCategory_succeeds(){
        Category category = Category.create(
                new CategoryName("Electronics"),
                new Slug("electronics"),
                "Description"
        );
        CategoryId id = category.getId();
        categoryRepository.save(category);
        Category loadedCategory = categoryRepository.findById(id).get();

        assertThat(loadedCategory.getId()).isEqualTo(category.getId());
        assertThat(loadedCategory.getName()).isEqualTo(category.getName());
        assertThat(loadedCategory.getSlug()).isEqualTo(category.getSlug());
        assertThat(loadedCategory.getDescription()).isEqualTo(category.getDescription());
    }

    @Test
    void save_categoryWithNullDescription_succeeds() {
        Category category = Category.create(
                new CategoryName("Electronics"),
                new Slug("electronics"),
                null
        );
        CategoryId id = category.getId();
        categoryRepository.save(category);
        Category loadedCategory = categoryRepository.findById(id).get();

        assertThat(loadedCategory.getDescription()).isNull();
    }

    @Test
    void save_withDuplicateSlug_throwsException() {
        Category category = Category.create(
                new CategoryName("Electronics"),
                new Slug("electronics"),
                null
        );
        categoryRepository.save(category);
        Category categoryDuplicate = Category.create(
                new CategoryName("Electronics 2"),
                new Slug("electronics"),
                null
        );

        assertThatThrownBy(() -> categoryRepository.save(categoryDuplicate))
                .isInstanceOf(SlugAlreadyExistsException.class);
    }

    @Test
    void existsBySlug_returnsTrueWhenSlugExists() {
        Category category = Category.create(
                new CategoryName("Electronics"),
                new Slug("electronics"),
                null
        );
        categoryRepository.save(category);

        boolean exists = categoryRepository.existsBySlug(new Slug("electronics"));
        assertThat(exists).isTrue();
    }

    @Test
    void existsBySlug_returnsFalseWhenSlugDoesNotExist() {
        boolean exists = categoryRepository.existsBySlug(new Slug("non-existent"));
        assertThat(exists).isFalse();
    }

    @Test
    void existsBySlugAndIdNot_returnsTrueWhenOtherCategoryHasSlug() {
        Category category1 = Category.create(
                new CategoryName("Electronics"),
                new Slug("electronics"),
                null
        );
        categoryRepository.save(category1);

        Category category2 = Category.create(
                new CategoryName("Phones"),
                new Slug("phones"),
                null
        );
        categoryRepository.save(category2);

        // Проверяем, существует ли slug "electronics" у документов, чье ID не равно ID category2
        boolean exists = categoryRepository.existsBySlugAndIdNot(new Slug("electronics"), category2.getId());
        assertThat(exists).isTrue();
    }

    @Test
    void existsBySlugAndIdNot_returnsFalseWhenSameCategoryHasSlug() {
        Category category = Category.create(
                new CategoryName("Electronics"),
                new Slug("electronics"),
                null
        );
        categoryRepository.save(category);

        boolean exists = categoryRepository.existsBySlugAndIdNot(new Slug("electronics"), category.getId());
        assertThat(exists).isFalse();
    }

    @Test
    void findAll_returnsAllCategories() {
        Category category1 = Category.create(
                new CategoryName("Electronics"),
                new Slug("electronics"),
                null
        );
        Category category2 = Category.create(
                new CategoryName("Clothing"),
                new Slug("clothing"),
                null
        );
        categoryRepository.save(category1);
        categoryRepository.save(category2);

        List<Category> categories = categoryRepository.findAll();

        assertThat(categories.size()).isEqualTo(2);

        assertThat(categories)
                .extracting(Category::getSlug)
                .containsExactlyInAnyOrder(new Slug("electronics"), new Slug("clothing"));
    }

    @Test
    void deleteById_removesCategory() {
        Category category = Category.create(
                new CategoryName("Electronics"),
                new Slug("electronics"),
                null
        );
        categoryRepository.save(category);
        CategoryId id = category.getId();

        categoryRepository.deleteById(id);

        assertThat(categoryRepository.findById(id)).isEmpty();
    }
}
