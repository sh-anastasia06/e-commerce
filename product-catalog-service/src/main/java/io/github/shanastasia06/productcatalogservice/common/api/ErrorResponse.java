package io.github.shanastasia06.productcatalogservice.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String code,
        String message,
        Instant timestamp,
        List<ErrorDetail> details
) {
    public ErrorResponse(int status, String code, String message) {
        this(status, code, message, Instant.now(), null);
    }
}
