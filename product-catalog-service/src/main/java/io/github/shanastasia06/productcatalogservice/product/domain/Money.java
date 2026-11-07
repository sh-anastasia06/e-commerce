package io.github.shanastasia06.productcatalogservice.product.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {
    public static final int SCALE = 2;

    public Money {
        if (amount == null) {
            throw new DomainValidationException("Price amount must not be null");
        }
        if (currency == null) {
            throw new DomainValidationException("Price currency must not be null");
        }
        if (amount.signum() < 0) {
            throw new DomainValidationException("Price amount must not be negative");
        }
        if (amount.stripTrailingZeros().scale() > SCALE) {
            throw new DomainValidationException("Price amount must have at most " + SCALE + " scale");
        }
        amount = amount.setScale(SCALE, RoundingMode.UNNECESSARY);
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }
}
