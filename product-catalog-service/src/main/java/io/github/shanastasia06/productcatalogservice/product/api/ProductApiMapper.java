package io.github.shanastasia06.productcatalogservice.product.api;

import io.github.shanastasia06.productcatalogservice.common.domain.PageResult;
import io.github.shanastasia06.productcatalogservice.product.domain.Money;
import io.github.shanastasia06.productcatalogservice.product.domain.Product;
import io.github.shanastasia06.productcatalogservice.product.service.ProductCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductApiMapper {
    @Mapping(target = "priceAmount", source = "price.amount")
    @Mapping(target = "priceCurrency", source = "price.currency")
    ProductCommand toCommand(ProductRequest request);

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "sku", source = "sku.value")
    @Mapping(target = "name", source = "name.value")
    @Mapping(target = "categoryId", source = "categoryId.value")
    @Mapping(target = "attributes", source = "attributes.values")
    ProductResponse toResponse(Product product);

    // serialized as string to avoid float pointing data loss
    default MoneyResponse toMoneyResponse(Money money) {
        return new MoneyResponse(money.amount().toPlainString(), money.currency().getCurrencyCode());
    }

    default ProductPageResponse toPageResponse(PageResult<Product> page) {
        List<ProductResponse> content = page.content().stream()
                .map(this::toResponse)
                .toList();
        return new ProductPageResponse(
                content, page.page(), page.size(), page.totalElements(), page.totalPages()
        );
    }
}
