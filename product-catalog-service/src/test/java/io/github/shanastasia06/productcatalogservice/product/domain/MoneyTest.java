package io.github.shanastasia06.productcatalogservice.product.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class MoneyTest {
    private final Currency CURRENCY_USD = Currency.getInstance("USD");
    private final Currency CURRENCY_EUR = Currency.getInstance("EUR");

    @Test
    void shouldCreateWithValidData() {
        BigDecimal amount = new BigDecimal("10.99");
        Money money = new Money(amount, CURRENCY_USD);

        assertThat(money).isNotNull();
        assertThat(money.amount()).isEqualTo(amount);
        assertThat(money.currency()).isEqualTo(CURRENCY_USD);
    }

    @Test
    void shouldNormalizeAmount() {
        BigDecimal amount1 = new BigDecimal("10");
        Money money1 = new Money(amount1, CURRENCY_USD);

        BigDecimal amount2 = new BigDecimal("10.5");
        Money money2 = new Money(amount2, CURRENCY_USD);

        assertThat(money1.amount()).isEqualTo(new BigDecimal("10.00"));
        assertThat(money2.amount()).isEqualTo(new BigDecimal("10.50"));
    }

    @Test
    void shouldEqualsWithEqualData() {
        Money money1 = new Money(new BigDecimal("10.00"), CURRENCY_USD);
        Money money2 = new Money(new BigDecimal("10.00"), CURRENCY_USD);

        assertThat(money1).isEqualTo(money2);
    }

    @Test
    void shouldAcceptZeroAmount() {
        Money money = new Money(new BigDecimal("0"), CURRENCY_USD);

        assertThat(money).isNotNull();
        assertThat(money.amount()).isEqualTo("0.00");
    }

    @Test
    void shouldThrowWithNegativeAmount() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-1"), CURRENCY_USD))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldThrowWithAmountScaleGreaterThan2() {
        assertThatThrownBy(() -> new Money(new BigDecimal("10.001"), CURRENCY_USD))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldAcceptExtraZeroes() {
        Money money = new Money(new BigDecimal("10.000"), CURRENCY_USD);

        assertThat(money).isNotNull();
        assertThat(money.amount()).isEqualTo("10.00");
    }

    @Test
    void shouldThrowWithNullAmount() {
        assertThatThrownBy(() -> new Money(null, CURRENCY_USD))
            .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldThrowWithNullCurrency() {
        assertThatThrownBy(() -> new Money(new BigDecimal("10.00"), null))
            .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldNotEqualSameAmountInDifferentCurrencies() {
        Money moneyUsd = new Money(new BigDecimal("10.00"), CURRENCY_USD);
        Money moneyEur = new Money(new BigDecimal("10.00"), CURRENCY_EUR);

        assertThat(moneyUsd).isNotEqualTo(moneyEur);
    }

    @Test
    void shouldBePositiveWithPositiveAmount() {
        Money money = new Money(new BigDecimal("10.00"), CURRENCY_USD);
        assertThat(money.isPositive()).isTrue();
    }

    @Test
    void shouldNotBePositiveWithZeroAmount() {
        Money money = new Money(new BigDecimal("0.00"), CURRENCY_USD);
        assertThat(money.isPositive()).isFalse();
    }
}
