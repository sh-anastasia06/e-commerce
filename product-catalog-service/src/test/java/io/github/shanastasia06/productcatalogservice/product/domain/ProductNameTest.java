package io.github.shanastasia06.productcatalogservice.product.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ProductNameTest {
    @Test
    void shouldCreateWithValidName() {
        ProductName name = new ProductName("Wireless Mouse");

        assertThat(name).isNotNull();
        assertThat(name.value()).isEqualTo("Wireless Mouse");
    }

    @Test
    void shouldStripWhitespace() {
        ProductName name = new ProductName("  Gaming Keyboard  ");

        assertThat(name.value()).isEqualTo("Gaming Keyboard");
    }

    @Test
    void shouldThrowWhenNull() {
        assertThatThrownBy(() -> new ProductName(null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Product name must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void shouldThrowWhenEmptyOrBlank(String blankValue) {
        assertThatThrownBy(() -> new ProductName(blankValue))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldAcceptMaxLength() {
        String validName = "A".repeat(200);
        ProductName name = new ProductName(validName);

        assertThat(name.value()).hasSize(200);
    }

    @Test
    void shouldThrowWhenExceedsMaxLength() {
        String invalidName = "A".repeat(201);

        assertThatThrownBy(() -> new ProductName(invalidName))
                .isInstanceOf(DomainValidationException.class);
    }
}
