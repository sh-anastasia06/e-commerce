package io.github.shanastasia06.productcatalogservice.product.api;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        MoneyResponse price,
        UUID categoryId,
        String status,
        Map<String, Object> attributes,
        Instant createdAt,
        Instant updatedAt
) { }
