package io.github.shanastasia06.productcatalogservice.category.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
                message = "Slug must contain only lowercase letters, digits and single hyphens")
        String slug,

        @Size(max = 100)
        String description
) { }
