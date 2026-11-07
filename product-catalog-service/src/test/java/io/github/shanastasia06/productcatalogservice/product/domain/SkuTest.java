package io.github.shanastasia06.productcatalogservice.product.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class SkuTest {
    @Test
    void shouldCreateWithValidSku() {
        Sku sku = new Sku("PROD-123-ABC");

        assertThat(sku).isNotNull();
        assertThat(sku.value()).isEqualTo("PROD-123-ABC");
        assertThat(sku.toString()).isEqualTo("PROD-123-ABC");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowWhenValueIsNullEmpty(String invalidValue) {
        assertThatThrownBy(() -> new Sku(invalidValue))
                .isInstanceOf(DomainValidationException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"AB", "A"})
    void shouldThrowWhenTooShort(String shortValue) {
        assertThatThrownBy(() -> new Sku(shortValue))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldThrowWhenTooLong() {
        String longValue = "A".repeat(65);
        assertThatThrownBy(() -> new Sku(longValue))
                .isInstanceOf(DomainValidationException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "abc-123",
            "PROD_123",
            "PROD 123",
            "-PROD-123",
            "PROD-123-",
            "PROD--123"
    })
    void shouldThrowWhenFormatIsInvalid(String invalidFormat) {
        assertThatThrownBy(() -> new Sku(invalidFormat))
                .isInstanceOf(DomainValidationException.class);
    }
}
