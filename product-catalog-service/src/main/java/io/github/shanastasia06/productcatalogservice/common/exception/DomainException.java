package io.github.shanastasia06.productcatalogservice.common.exception;

public abstract class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
