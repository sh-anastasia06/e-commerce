package io.github.shanastasia06.productcatalogservice.common.exception;

import io.github.shanastasia06.productcatalogservice.product.domain.ProductId;

public class ConcurrentUpdateException extends DomainException {
    public ConcurrentUpdateException(ProductId id) {
        super("Product <" + id.value().toString() + "> was modified by someone else, reload and retry");
    }
}
