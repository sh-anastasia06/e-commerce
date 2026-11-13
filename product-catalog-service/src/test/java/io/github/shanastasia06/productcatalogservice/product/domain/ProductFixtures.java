package io.github.shanastasia06.productcatalogservice.product.domain;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;

public class ProductFixtures {
    static final Currency USD = Currency.getInstance("USD");

    static Product draftProduct() {
        return Product.create(
                new Sku("PROD-DRAFT"),
                new ProductName("Headphones"),
                "Wireless headphones",
                new Money(new BigDecimal("299.99"), USD),
                CategoryId.generate(),
                Attributes.empty(),
                Instant.now()
        );
    }

    static Product activeProduct() {
        Product product = Product.create(
                new Sku("PROD-ACTIVE"),
                new ProductName("Laptop"),
                "Light laptop",
                new Money(new BigDecimal("1500.00"), USD),
                CategoryId.generate(),
                Attributes.empty(),
                Instant.now()
        );
        product.activate(Instant.now());
        return product;
    }

    static Product productWithAttributes() {
        return Product.create(
                new Sku("sku-attr"),
                new ProductName("Smartwatch"),
                "Waterproof smartwatch",
                new Money(new BigDecimal("199.99"), USD),
                CategoryId.generate(),
                new Attributes(Map.of(
                        "color", "Black",
                        "waterproof", true,
                        "warranty_years", 2
                )),
                Instant.now()
        );
    }
}
