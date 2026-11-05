package io.github.shanastasia06.productcatalogservice.category.reposotory;

import io.github.shanastasia06.productcatalogservice.category.domain.Category;
import io.github.shanastasia06.productcatalogservice.category.domain.Slug;
import io.github.shanastasia06.productcatalogservice.category.repository.CategoryRepository;
import io.github.shanastasia06.productcatalogservice.category.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository);
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
}
