package io.github.shanastasia06.productcatalogservice.product.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ProductIdTest {
    @Test
    void shouldCreateWithValidUuid() {
        UUID uuid = UUID.randomUUID();
        ProductId productId = new ProductId(uuid);

        assertThat(productId).isNotNull();
        assertThat(productId.value()).isEqualTo(uuid);
    }

    @Test
    void shouldThrowWhenUuidIsNull() {
        assertThatThrownBy(() -> new ProductId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Product id must not be null");
    }

    @Test
    void shouldGenerateValidProductId() {
        ProductId productId = ProductId.generate();

        assertThat(productId).isNotNull();
        assertThat(productId.value()).isNotNull();
    }

    @Test
    void shouldReturnStringRepresentation() {
        UUID uuid = UUID.randomUUID();
        ProductId productId = new ProductId(uuid);

        assertThat(productId.toString()).isEqualTo(uuid.toString());
    }

    @Test
    void shouldBeEqualWhenUuidsMatch() {
        UUID uuid = UUID.randomUUID();
        ProductId id1 = new ProductId(uuid);
        ProductId id2 = new ProductId(uuid);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}
