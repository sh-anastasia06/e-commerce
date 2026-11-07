package io.github.shanastasia06.productcatalogservice.category.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class SlugTest {
    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowExceptionWhenSlugIsNullEmptyOrBlank(String value) {
        assertThatThrownBy(() -> new Slug(value))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldThrowExceptionWhenSlugLengthIsGreaterThan100() {
        String slug = "a".repeat(101);
        assertThatThrownBy(() -> new Slug(slug))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Slug value must not be longer than 100 characters");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-starts-with-hyphen",
            "ends-with-hyphen-",
            "double--hyphen",
            "UPPERCASE",
            "with space",
            "with_underscore",
            "special$char"
    })
    void shouldThrowExceptionWhenSlugIsNotValid(String invalidSlug) {
        assertThatThrownBy(() -> new Slug(invalidSlug))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Slug value is not valid");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "valid-slug",
            "slug123",
            "a",
            "12345",
            "very-long-but-valid-slug-with-many-parts"
    })
    void shouldCreateSlugWhenValueIsValid(String validSlug) {
        Slug slug = new Slug(validSlug);
        assertThat(slug.value()).isEqualTo(validSlug);
    }
}
