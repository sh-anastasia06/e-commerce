package io.github.shanastasia06.userservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
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

