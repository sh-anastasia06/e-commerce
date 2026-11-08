package io.github.shanastasia06.productcatalogservice.product.infrastructure;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.product.domain.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Currency;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "priceAmount", source = "price.amount")
    @Mapping(target = "priceCurrency", source = "price.currency.currencyCode")
    ProductDocument toDocument(Product product);

    // goes through Product.restore() and rebuilds the value object
    default Product toDomain(ProductDocument document) {
        return Product.restore(
                new ProductId(document.getId()),
                new Sku(document.getSku()),
                new ProductName(document.getName()),
                document.getDescription(),
                new Money(document.getPriceAmount(), Currency.getInstance(document.getPriceCurrency())),
                new CategoryId(document.getCategoryId()),
                ProductStatus.valueOf(document.getStatus()),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                document.getVersion()
        );
    }

    default UUID diToUUID(ProductId id) {
        return id.value();
    }

    default UUID categoryIdToUuid(CategoryId id) {
        return id.value();
    }

    default String skuToString(Sku sku) {
        return sku.value();
    }

    default String nameToString(ProductName name) {
        return name.value();
    }
}
