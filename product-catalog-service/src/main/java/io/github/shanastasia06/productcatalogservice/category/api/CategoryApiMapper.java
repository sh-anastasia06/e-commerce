package io.github.shanastasia06.productcatalogservice.category.api;

import io.github.shanastasia06.productcatalogservice.category.domain.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
interface CategoryApiMapper {
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "name", source = "name.value")
    @Mapping(target = "slug", source = "slug.value")
    CategoryResponse toResponse(Category category);
}
