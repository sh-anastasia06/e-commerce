package io.github.shanastasia06.productcatalogservice.category.infrastructure;

import io.github.shanastasia06.productcatalogservice.category.domain.Category;
import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.category.domain.CategoryName;
import io.github.shanastasia06.productcatalogservice.category.domain.Slug;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDocument toDocument(Category category);

    // goes through Category.restore() and couldn't be mapped automatically
    default Category toDomain(CategoryDocument document) {
        return Category.restore(
                new CategoryId(document.getId()),
                new CategoryName(document.getName()),
                new Slug(document.getSlug()),
                document.getDescription());
    }

    // helper methods to map data types
    default UUID idToUuid(CategoryId id) {
        return id.value();
    }

    default String nameToString(CategoryName name) {
        return name.value();
    }

    default String slugToString(Slug slug) {
        return slug.value();
    }
}
