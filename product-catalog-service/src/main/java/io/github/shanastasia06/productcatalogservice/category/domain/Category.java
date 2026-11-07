package io.github.shanastasia06.productcatalogservice.category.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Category {
    public static final int DESCRIPTION_MAX_LENGTH = 500;

    @EqualsAndHashCode.Include
    private final CategoryId id;
    private CategoryName name;
    private Slug slug;
    private String description;

    private Category(CategoryId id, CategoryName name, Slug slug, String description) {
        this.id = Objects.requireNonNull(id, "Category id must not be null");
        this.name = Objects.requireNonNull(name, "Category name must not be null");
        this.slug = Objects.requireNonNull(slug, "Category slug must not be null");
        this.description = description;
    }

    public static Category create(CategoryName name, Slug slug, String description) {
        return new Category(CategoryId.generate(), name, slug, normalizeDescription(description));
    }

    public static Category restore(CategoryId id, CategoryName name, Slug slug, String description) {
        return new Category(id, name, slug, description);
    }

    public void update(CategoryName name, Slug slug, String description) {
        Objects.requireNonNull(name, "Category name must not be null");
        Objects.requireNonNull(slug, "Category slug must not be null");
        String normalizedDescription = normalizeDescription(description);

        this.name = name;
        this.slug = slug;
        this.description = normalizedDescription;
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String stripped = description.strip();
        if (stripped.isEmpty()) {
            return null;
        }
        if (stripped.length() > DESCRIPTION_MAX_LENGTH) {
            throw new DomainValidationException(
                    "Category description must not be longer than " + DESCRIPTION_MAX_LENGTH + " characters"
            );
        }

        return stripped;
    }
}
