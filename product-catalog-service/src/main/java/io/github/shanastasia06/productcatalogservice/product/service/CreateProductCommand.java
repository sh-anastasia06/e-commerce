package io.github.shanastasia06.productcatalogservice.product.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record CreateProductCommand(
        String sku,
        String name,
        String description,
        BigDecimal priceAmount,
        String priceCurrency,
        UUID categoryId,
        Map<String, Object> attributes
) { }
