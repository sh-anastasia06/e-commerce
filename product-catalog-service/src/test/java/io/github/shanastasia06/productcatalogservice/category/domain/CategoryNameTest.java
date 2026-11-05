package io.github.shanastasia06.productcatalogservice.category.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class CategoryNameTest {
    @Test
    void shouldThrowExceptionWhenCategoryNameIsEmpty() {
        assertThatThrownBy(() -> new CategoryName("     "))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Category name must not be empty");
    }

    @Test
    void shouldThrowExceptionWhenCategoryNameIsNull() {
        assertThatThrownBy(() -> new CategoryName(null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Category name must not be null");
    }

    @Test
    void shouldThrowExceptionWhenCategoryNameLengthIsGreaterThan100() {
        String categoryName = "Super long category name. Super long category name. Super long category name. Super long category name. Super long category name.";
        assertThatThrownBy(() -> new CategoryName(categoryName))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Category name must not be longer than 100 characters");
    }

    @Test
    void shouldStripWhitespaceWhenCategoryNameIsValid() {
        CategoryName categoryName = new CategoryName("   Electronics   ");
        assertThat(categoryName.value()).isEqualTo("Electronics");
    }

    @Test
    void shouldCreateCategoryNameWhenValueIsValid() {
        String value = "Electronics";
        CategoryName categoryName = new CategoryName(value);
        assertThat(categoryName.value()).isEqualTo(value);
    }
}
