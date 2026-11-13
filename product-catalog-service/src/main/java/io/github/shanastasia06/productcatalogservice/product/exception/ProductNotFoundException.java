package io.github.shanastasia06.productcatalogservice.product.exception;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainException;
import io.github.shanastasia06.productcatalogservice.product.domain.ProductId;

public class ProductNotFoundException extends DomainException {
    public ProductNotFoundException(ProductId id) {
        super("Product with ID <" + id + "> was not found");
    }
}
