package io.github.shanastasia06.productcatalogservice.product.infrastructure;

import io.github.shanastasia06.productcatalogservice.category.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryExistenceAdapterTest {
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryExistenceAdapter categoryExistenceAdapter;

    private CategoryId categoryId;

    @BeforeEach
    void setUp() {
        categoryId = CategoryId.generate();
    }

    @Test
    void exists_withExistentCategory_returnsTrue() {
        Category category = Category.create(
                new CategoryName("Electronics"),
                new Slug("electronics"),
                "Electronics description"
        );
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        boolean result = categoryExistenceAdapter.exists(categoryId);

        assertTrue(result);
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    void exists_withNonExistentCategory_returnsFalse() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        boolean result = categoryExistenceAdapter.exists(categoryId);

        assertThat(result).isFalse();
        verify(categoryRepository, times(1)).findById(categoryId);
    }
}