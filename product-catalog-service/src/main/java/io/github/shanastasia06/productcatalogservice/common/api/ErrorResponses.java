package io.github.shanastasia06.productcatalogservice.common.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

public final class ErrorResponses {

    private ErrorResponses() {}

    public static ResponseEntity<ErrorResponse> build(HttpStatus status, ErrorCode code,
                                                      String message, List<ErrorDetail> details) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), code.name(), message, Instant.now(), details));
    }
}
