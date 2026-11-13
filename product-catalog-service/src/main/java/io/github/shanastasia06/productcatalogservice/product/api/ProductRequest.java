package io.github.shanastasia06.productcatalogservice.product.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record ProductRequest(
        @NotBlank
        @Size(min = 3, max = 64)
        @Pattern(regexp = "^[A-Z0-9]+(-[A-Z0-9]+)*$",
                message = "SKU must contain only uppercase letters, digits and single hyphens")
        String sku,

        @NotBlank
        @Size(max = 200)
        String name,

        @Size(max = 2000)
        String description,

        @NotNull
        @Valid
        MoneyRequest price,

        @NotNull
        UUID categoryId,

        @Size(max = 50)
        Map<String, Object> attributes
) { }
