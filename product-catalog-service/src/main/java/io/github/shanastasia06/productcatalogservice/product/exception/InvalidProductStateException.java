package io.github.shanastasia06.productcatalogservice.product.exception;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainException;

public class InvalidProductStateException extends DomainException {
    public InvalidProductStateException(String message) {
        super(message);
    }
}
