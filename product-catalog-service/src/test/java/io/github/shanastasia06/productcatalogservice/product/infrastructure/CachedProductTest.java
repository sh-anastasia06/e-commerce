package io.github.shanastasia06.productcatalogservice.product.infrastructure;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.product.domain.*;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CachedProductTest {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void roundTripsProductThroughCachedProductAndJsonPreservingAllFields() {
        Product original = Product.restore(
                new ProductId(UUID.randomUUID()),
                new Sku("SKU-98765"),
                new ProductName("Mechanical Keyboard"),
                "Wireless mechanical keyboard with tactile switches",
                new Money(new BigDecimal("149.99"), Currency.getInstance("EUR")),
                new CategoryId(UUID.randomUUID()),
                new Attributes(Map.of(
                        "color", "Graphite",
                        "wireless", true,
                        "keyCount", 87,
                        "weightKg", 0.85
                )),
                ProductStatus.ACTIVE,
                Instant.parse("2026-11-15T10:15:30.123456789Z"),
                Instant.parse("2026-11-15T14:22:45.987654321Z"),
                7L
        );

        CachedProduct toCache = CachedProduct.from(original);
        String json = jsonMapper.writeValueAsString(toCache);
        CachedProduct fromCache = jsonMapper.readValue(json, CachedProduct.class);
        Product restored = fromCache.toDomain();

        assertAll(
                () -> assertEquals(original.getId(), restored.getId()),
                () -> assertEquals(original.getSku(), restored.getSku()),
                () -> assertEquals(original.getName(), restored.getName()),
                () -> assertEquals(original.getDescription(), restored.getDescription()),
                () -> assertEquals(original.getPrice(), restored.getPrice()),
                () -> assertEquals(original.getCategoryId(), restored.getCategoryId()),
                () -> assertEquals(original.getAttributes(), restored.getAttributes()),
                () -> assertEquals(original.getStatus(), restored.getStatus()),
                () -> assertEquals(original.getCreatedAt(), restored.getCreatedAt()),
                () -> assertEquals(original.getUpdatedAt(), restored.getUpdatedAt()),
                () -> assertEquals(original.getVersion(), restored.getVersion())
        );
    }
}