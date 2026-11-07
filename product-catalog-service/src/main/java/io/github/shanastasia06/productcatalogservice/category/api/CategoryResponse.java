package io.github.shanastasia06.productcatalogservice.category.api;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String description
) { }
