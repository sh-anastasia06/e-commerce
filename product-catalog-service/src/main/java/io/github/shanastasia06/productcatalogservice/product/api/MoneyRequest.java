package io.github.shanastasia06.productcatalogservice.product.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record MoneyRequest(
        @NotNull
        @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$",
                message = "Amount must be a decimal with at most 2 fraction digits")
        String amount,

        @NotNull
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter uppercase ISO code")
        String currency
) { }
