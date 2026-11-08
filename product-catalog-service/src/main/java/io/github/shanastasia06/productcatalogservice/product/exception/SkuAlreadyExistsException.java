package io.github.shanastasia06.productcatalogservice.product.exception;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainException;
import io.github.shanastasia06.productcatalogservice.product.domain.Sku;

public class SkuAlreadyExistsException extends DomainException {
    public SkuAlreadyExistsException(Sku sku) {
        super("Product with SKU '" + sku.value() + "' already exists");
    }
}
