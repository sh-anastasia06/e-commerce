package io.github.shanastasia06.productcatalogservice.product.api;

import java.util.List;

public record ProductPageResponse(
        List<ProductResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) { }
