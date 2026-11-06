package io.github.shanastasia06.productcatalogservice.category.infrastructure;

import io.github.shanastasia06.productcatalogservice.category.domain.Category;
import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.category.domain.CategoryName;
import io.github.shanastasia06.productcatalogservice.category.domain.Slug;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = CategoryMapperImpl.class)
public class CategoryMapperTest {
    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    void shouldMapCategoryToDocument() {
        UUID uuid = UUID.randomUUID();
        CategoryId id = new CategoryId(uuid);
        CategoryName name = new CategoryName("Electronics");
        Slug slug = new Slug("electronics");
        String description = "Tech items";

        Category category = Category.restore(id, name, slug, description);

        CategoryDocument document = categoryMapper.toDocument(category);

        assertThat(document).isNotNull();
        assertThat(document.getId()).isEqualTo(uuid);
        assertThat(document.getName()).isEqualTo("Electronics");
        assertThat(document.getSlug()).isEqualTo("electronics");
        assertThat(document.getDescription()).isEqualTo("Tech items");
    }

    @Test
    void shouldMapDocumentToCategory() {
        UUID uuid = UUID.randomUUID();
        CategoryDocument document = new CategoryDocument();
        document.setId(uuid);
        document.setName("Books");
        document.setSlug("books");
        document.setDescription("Reading materials");

        Category category = categoryMapper.toDomain(document);

        assertThat(category).isNotNull();
        assertThat(category.getId().value()).isEqualTo(uuid);
        assertThat(category.getName().value()).isEqualTo("Books");
        assertThat(category.getSlug().value()).isEqualTo("books");
        assertThat(category.getDescription()).isEqualTo("Reading materials");
    }

    @Test
    void shouldMapHelperMethodsCorrectly() {
        CategoryId id = CategoryId.generate();
        CategoryName name = new CategoryName("Home");
        Slug slug = new Slug("home-goods");

        assertThat(categoryMapper.idToUuid(id)).isEqualTo(id.value());
        assertThat(categoryMapper.nameToString(name)).isEqualTo("Home");
        assertThat(categoryMapper.slugToString(slug)).isEqualTo("home-goods");
    }
}
