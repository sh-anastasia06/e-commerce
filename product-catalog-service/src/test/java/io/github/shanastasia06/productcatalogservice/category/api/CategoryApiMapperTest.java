package io.github.shanastasia06.productcatalogservice.category.api;

import io.github.shanastasia06.productcatalogservice.category.domain.Category;
import io.github.shanastasia06.productcatalogservice.category.domain.CategoryName;
import io.github.shanastasia06.productcatalogservice.category.domain.Slug;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CategoryApiMapperTest {
    private final CategoryApiMapper mapper = new CategoryApiMapperImpl();

    @Test
    void shouldMapCategoryToResponse() {
        Category category = Category.create(
                new CategoryName("Electronics"),
                new Slug("electronics"),
                "description"
        );

        CategoryResponse response = mapper.toResponse(category);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(category.getId().value());
        assertThat(response.slug()).isEqualTo(category.getSlug().value());
        assertThat(response.name()).isEqualTo(category.getName().value());
        assertThat(response.description()).isEqualTo(category.getDescription());
    }

    @Test
    void shouldHandleNullDescription() {
        Category category = Category.create(
                new CategoryName("Electronics"),
                new Slug("electronics"),
                null
        );

        CategoryResponse response = mapper.toResponse(category);

        assertThat(response.description()).isNull();
    }
}
