package io.github.shanastasia06.productcatalogservice.product.service;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSearchQuery(
        UUID categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        int page,
        int size
) {}
