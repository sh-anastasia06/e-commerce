package io.github.shanastasia06.productcatalogservice.category.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CategoryRequestTest {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldPassValidationForValidRequest() {
        CategoryRequest req = new CategoryRequest("Electronics", "electronics", "Description");

        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(req);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        CategoryRequest req = new CategoryRequest("", "electronics", "Description");

        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(req);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Electronics", "electronics_uk", "electronics--sllug", "electronic$"})
    void shouldFailWhenSlugHasInvalidFormat(String invalidSlug) {
        CategoryRequest req = new CategoryRequest("Electronics", invalidSlug, "Description");

        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(req);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("slug");
    }

    @Test
    void shouldFailWhenDescriptionExceedsMaxLength() {
        String longDescription = "a".repeat(101);
        CategoryRequest req = new CategoryRequest("Electronics", "electronics", longDescription);

        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(req);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("description");
    }
}
