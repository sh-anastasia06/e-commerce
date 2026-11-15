package io.github.shanastasia06.productcatalogservice.common.exception;

import java.util.UUID;

public class ConcurrentUpdateException extends DomainException {
    public ConcurrentUpdateException(UUID id) {
        super("Product <" + id.toString() + "> was modified by someone else, reload and retry");
    }
}
