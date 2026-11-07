package io.github.shanastasia06.productcatalogservice.category.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class CategoryIdTest {
    @Test
    void shouldCreateCategoryIdWhenValueIsValid() {
        UUID uuid = UUID.randomUUID();
        CategoryId categoryId = new CategoryId(uuid);
        assertThat(categoryId.value()).isEqualTo(uuid);
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        assertThatThrownBy(() -> new CategoryId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("category id must not be null");
    }

    @Test
    void shouldGenerateValidCategoryId() {
        CategoryId categoryId = CategoryId.generate();
        assertThat(categoryId).isNotNull();
        assertThat(categoryId.value()).isNotNull();
    }

    @Test
    void shouldReturnCorrectToString() {
        UUID uuid = UUID.randomUUID();
        CategoryId categoryId = new CategoryId(uuid);
        assertThat(categoryId.toString()).isEqualTo(uuid.toString());
    }

    @Test
    void shouldBeEqualsWhenValuesAreSame() {
        UUID uuid = UUID.randomUUID();
        CategoryId id1 = new CategoryId(uuid);
        CategoryId id2 = new CategoryId(uuid);
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}
