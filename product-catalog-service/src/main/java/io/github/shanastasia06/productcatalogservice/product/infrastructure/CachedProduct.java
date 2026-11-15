package io.github.shanastasia06.productcatalogservice.product.infrastructure;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.product.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;

record CachedProduct(
        String id,
        String sku,
        String name,
        String description,
        String priceAmount,
        String priceCurrency,
        String categoryId,
        Map<String, Object> attributes,
        String status,
        String createdAt,
        String updatedAt,
        Long version
) {
    static CachedProduct from(Product product) {
        return new CachedProduct(
                product.getId().value().toString(),
                product.getSku().value(),
                product.getName().value(),
                product.getDescription(),
                product.getPrice().amount().toPlainString(),
                product.getPrice().currency().getCurrencyCode(),
                product.getCategoryId().value().toString(),
                product.getAttributes().values(),
                product.getStatus().name(),
                product.getCreatedAt().toString(),
                product.getUpdatedAt().toString(),
                product.getVersion()
        );
    }

    Product toDomain() {
        return Product.restore(
                new ProductId(UUID.fromString(id)),
                new Sku(sku),
                new ProductName(name),
                description,
                new Money(new BigDecimal(priceAmount), Currency.getInstance(priceCurrency)),
                new CategoryId(UUID.fromString(categoryId)),
                new Attributes(attributes),
                ProductStatus.valueOf(status),
                Instant.parse(createdAt),
                Instant.parse(updatedAt),
                version
        );
    }
}
