package io.github.shanastasia06.productcatalogservice.category.service;

import io.github.shanastasia06.productcatalogservice.category.domain.*;
import io.github.shanastasia06.productcatalogservice.category.exception.CategoryInUseException;
import io.github.shanastasia06.productcatalogservice.category.exception.CategoryNotFoundException;
import io.github.shanastasia06.productcatalogservice.category.exception.SlugAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryUsageChecker categoryUsageChecker;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, categoryUsageChecker);
    }

    @Test
    void create_withValidData_savesAndReturnsCategory() {
        when(categoryRepository.existsBySlug(new Slug("electronics"))).thenReturn(false);
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.create("Electronics", "electronics", "Phones and laptops");

        assertThat(result.getName().value()).isEqualTo("Electronics");
        assertThat(result.getSlug().value()).isEqualTo("electronics");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_withDuplicateSlug_throwsException() {
        when(categoryRepository.existsBySlug(any(Slug.class))).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create("Electronics", "electronics", "Desc"))
                .isInstanceOf(SlugAlreadyExistsException.class);

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getById_validId_returnsCategory() {
        UUID uuid = UUID.randomUUID();
        CategoryId categoryId = new CategoryId(uuid);
        Category category = Category.create(new CategoryName("Electronics"), new Slug("electronics"), "Desc");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        Category result = categoryService.getById(uuid);

        assertThat(result).isNotNull();
        assertThat(result.getName().value()).isEqualTo("Electronics");
    }

    @Test
    void getById_nonExistentId_throwsException() {
        UUID uuid = UUID.randomUUID();
        CategoryId categoryId = new CategoryId(uuid);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(uuid))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void getAll_returnsCategoryList() {
        Category category1 = Category.create(new CategoryName("Electronics"), new Slug("electronics"), null);
        Category category2 = Category.create(new CategoryName("Clothing"), new Slug("clothing"), null);

        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));

        List<Category> result = categoryService.getAll();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void update_validData_updatesAndReturnsCategory() {
        Category category = Category.create(new CategoryName("Old Name"), new Slug("old-slug"), "Old Desc");
        UUID uuid = category.getId().value(); // Берем реальный ID созданной категории

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.existsBySlugAndIdNot(eq(new Slug("new-slug")), eq(category.getId()))).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.update(uuid, "New Name", "new-slug", "New Desc");

        assertThat(result.getName().value()).isEqualTo("New Name");
        assertThat(result.getSlug().value()).isEqualTo("new-slug");
        assertThat(result.getDescription()).isEqualTo("New Desc");
        verify(categoryRepository).save(category);
    }

    @Test
    void update_nonExistentId_throwsException() {
        UUID uuid = UUID.randomUUID();
        CategoryId categoryId = new CategoryId(uuid);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(uuid, "Name", "slug", "Desc"))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void update_duplicateSlug_throwsException() {
        Category category = Category.create(new CategoryName("Name"), new Slug("slug"), "Desc");
        UUID uuid = category.getId().value();
        CategoryId categoryId = category.getId();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsBySlugAndIdNot(new Slug("existing-slug"), categoryId)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(uuid, "Name", "existing-slug", "Desc"))
                .isInstanceOf(SlugAlreadyExistsException.class);

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void delete_existingId_deletesCategory() {
        UUID uuid = UUID.randomUUID();
        CategoryId categoryId = new CategoryId(uuid);
        Category category = Category.create(new CategoryName("Electronics"), new Slug("electronics"), null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        categoryService.delete(uuid);

        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void delete_nonExistentId_throwsException() {
        UUID uuid = UUID.randomUUID();
        CategoryId categoryId = new CategoryId(uuid);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(uuid))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void delete_categoryInUse_throwsException() {
        UUID uuid = UUID.randomUUID();
        CategoryId categoryId = new CategoryId(uuid);
        Category category = Category.create(new CategoryName("Electronics"), new Slug("electronics"), null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryUsageChecker.isInUse(categoryId)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete(uuid))
                .isInstanceOf(CategoryInUseException.class);

        verify(categoryRepository, never()).deleteById(any());
    }
}
