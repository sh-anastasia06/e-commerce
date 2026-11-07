package io.github.shanastasia06.productcatalogservice.category.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class CategoryTest {
    @Test
    void create_setsNameSlugAndDescription() {
        CategoryName name = new CategoryName("Electronics");
        Slug slug = new Slug("electronics");
        String description = "Description";
        Category category = Category.create(name, slug, description);
        assertThat(category.getId()).isNotNull();
        assertThat(category.getName()).isEqualTo(name);
        assertThat(category.getSlug()).isEqualTo(slug);
        assertThat(category.getDescription()).isEqualTo(description);
    }

    @Test
    void create_withIdenticalDataCreatesDifferentIds() {
        CategoryName name = new CategoryName("Electronics");
        Slug slug = new Slug("electronics");
        String description = "Description";
        Category category1 = Category.create(name, slug, description);
        Category category2 = Category.create(name, slug, description);

        assertThat(category1.getId()).isNotEqualTo(category2.getId());
    }

    @Test
    void create_withNullDescription() {
        CategoryName name = new CategoryName("Electronics");
        Slug slug = new Slug("electronics");
        Category category = Category.create(name, slug, null);

        assertThat(category).isNotNull();
        assertThat(category.getDescription()).isNull();
    }

    @Test
    void create_withEmptyDescriptionCreatesNullDescription() {
        CategoryName name = new CategoryName("Electronics");
        Slug slug = new Slug("electronics");
        Category category = Category.create(name, slug, " ");
        assertThat(category.getDescription()).isNull();
    }

    @Test
    void create_descriptionWithSpacesIsStripped() {
        CategoryName name = new CategoryName("Electronics");
        Slug slug = new Slug("electronics");
        String description = "  Description    ";
        Category category = Category.create(name, slug, description);

        assertThat(category.getDescription()).isEqualTo("Description");
    }

    @Test
    void create_withDescriptionOfMaxLength_succeeds() {
        CategoryName name = new CategoryName("Electronics");
        Slug slug = new Slug("electronics");
        String description = "a".repeat(500);
        Category category = Category.create(name, slug, description);

        assertThat(category).isNotNull();
        assertThat(category.getDescription()).isEqualTo("a".repeat(500));
    }

    @Test
    void create_withDescriptionLengthGreaterThan500ThrowsException() {
        CategoryName name = new CategoryName("Electronics");
        Slug slug = new Slug("electronics");
        String description = "a".repeat(501);

        assertThatThrownBy(() -> Category.create(name, slug, description))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void restore_keepsGivenId() {
        CategoryName name = new CategoryName("Electronics");
        Slug slug = new Slug("electronics");
        String description = "Description";
        Category category = Category.create(name, slug, description);
        CategoryId id = category.getId();
        Category restored = Category.restore(id, name, slug, description);

        assertThat(restored.getId()).isEqualTo(id);
    }

    @Test
    void restore_keepsData() {
        CategoryName name = new CategoryName("Electronics");
        Slug slug = new Slug("electronics");
        String description = "Description";
        Category category = Category.create(name, slug, description);
        CategoryId id = category.getId();
        Category restored = Category.restore(id, name, slug, description);

        assertThat(restored.getName()).isEqualTo(name);
        assertThat(restored.getSlug()).isEqualTo(slug);
        assertThat(restored.getDescription()).isEqualTo(description);
    }

    @Test
    void restore_restoredWithTheSameIdAreEqual() {
        Category category = Category.create(
                new CategoryName("Electronics"),
                new Slug("electronics"),
                "description"
        );
        CategoryId id = category.getId();
        Category restored1 = Category.restore(id, category.getName(), category.getSlug(), category.getDescription());
        Category restored2 = Category.restore(id, category.getName(), category.getSlug(), category.getDescription());

        assertThat(restored1).isEqualTo(restored2);
    }

    @Test
    void restore_withNullIdThrowsException() {
        assertThatThrownBy(() -> Category.restore(
                null,
                new CategoryName("Electronics"),
                new Slug("electronics"),
                "description")
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void update_withNullCategoryName_throwsException() {
        CategoryName name = new CategoryName("Electronics");
        Slug slug = new Slug("electronics");
        String description = "Description";
        Category category = Category.create(name, slug, description);
        assertThatThrownBy(() -> category.update(null, slug, description))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void update_withNullSlug_throwsException() {
        CategoryName name = new CategoryName("Electronics");
        Slug slug = new Slug("electronics");
        String description = "Description";
        Category category = Category.create(name, slug, description);
        assertThatThrownBy(() -> category.update(name, null, description))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void update_withNullDescription_succeeds() {
        CategoryName name = new CategoryName("Electronics");
        Slug slug = new Slug("electronics");
        String description = "Description";
        Category category = Category.create(name, slug, description);
        category.update(name, slug, null);
        assertThat(category).isNotNull();
        assertThat(category.getDescription()).isEqualTo(null);
    }

    @Test
    void update_withDescriptionOfMaxLength_succeeds() {
        CategoryName name = new CategoryName("Electronics");
        Slug slug = new Slug("electronics");
        String description = "Description";
        Category category = Category.create(name, slug, description);
        category.update(name, slug, "a".repeat(500));
    }

    @Test
    void update_withDescriptionLengthGreaterThanMaxLength_throwsException() {
        CategoryName name = new CategoryName("Electronics");
        Slug slug = new Slug("electronics");
        String description = "Description";
        Category category = Category.create(name, slug, description);
        assertThatThrownBy(() -> category.update(name, slug, "a".repeat(501)))
                .isInstanceOf(DomainValidationException.class);
    }
}
