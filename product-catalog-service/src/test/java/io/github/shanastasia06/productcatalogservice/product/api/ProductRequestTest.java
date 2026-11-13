package io.github.shanastasia06.productcatalogservice.product.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        if (factory != null) {
            factory.close();
        }
    }

    private ProductRequest createValidRequest() {
        return new ProductRequest(
                "SKU-12345",
                "Wireless Mechanical Keyboard",
                "Ergonomic mechanical keyboard with RGB backlighting.",
                new MoneyRequest("99.99", "USD"),
                UUID.randomUUID(),
                Map.of("color", "black", "switches", "brown")
        );
    }

    @Test
    @DisplayName("A completely valid product request should have no validation violations")
    void validRequest_hasNoViolations() {
        ProductRequest request = createValidRequest();
        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "AB", "sku-123", "SKU 123", "SKU--123"})
    @DisplayName("SKU invalid values should trigger violation(s) on 'sku'")
    void sku_invalidValues(String invalidSku) {
        ProductRequest request = new ProductRequest(
                invalidSku,
                "Valid Name",
                "Valid description",
                new MoneyRequest("10.00", "USD"),
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).allSatisfy(v ->
                assertThat(v.getPropertyPath().toString()).isEqualTo("sku")
        );
    }

    @Test
    @DisplayName("SKU exceeding 64 characters should trigger violation on 'sku'")
    void sku_tooLong() {
        String longSku = "A".repeat(65);
        ProductRequest request = new ProductRequest(
                longSku,
                "Valid Name",
                "Valid description",
                new MoneyRequest("10.00", "USD"),
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).anySatisfy(v ->
                assertThat(v.getPropertyPath().toString()).isEqualTo("sku")
        );
    }

    @Test
    @DisplayName("SKU at boundary limits (3 and 64 chars) should be valid")
    void sku_boundaryValuesValid() {
        ProductRequest request1 = new ProductRequest("ABC", "Name", null, new MoneyRequest("10.00", "USD"), UUID.randomUUID(), null);
        ProductRequest request2 = new ProductRequest("A".repeat(64), "Name", null, new MoneyRequest("10.00", "USD"), UUID.randomUUID(), null);

        assertThat(validator.validate(request1)).isEmpty();
        assertThat(validator.validate(request2)).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Blank or null name should trigger violation on 'name'")
    void name_blankOrNull(String invalidName) {
        ProductRequest request = new ProductRequest(
                "SKU-001",
                invalidName,
                "Valid description",
                new MoneyRequest("10.00", "USD"),
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).anySatisfy(v ->
                assertThat(v.getPropertyPath().toString()).isEqualTo("name")
        );
    }

    @Test
    @DisplayName("Name exceeding 200 characters should trigger violation on 'name'")
    void name_tooLong() {
        String longName = "N".repeat(201);
        ProductRequest request = new ProductRequest(
                "SKU-001",
                longName,
                "Valid description",
                new MoneyRequest("10.00", "USD"),
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).anySatisfy(v ->
                assertThat(v.getPropertyPath().toString()).isEqualTo("name")
        );
    }

    @Test
    @DisplayName("Name with exactly 200 characters should be valid")
    void name_maxValidLength() {
        String validName = "N".repeat(200);
        ProductRequest request = new ProductRequest(
                "SKU-001",
                validName,
                "Valid description",
                new MoneyRequest("10.00", "USD"),
                UUID.randomUUID(),
                null
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("Description exceeding 2000 characters should trigger violation on 'description'")
    void description_tooLong() {
        String longDesc = "D".repeat(2001);
        ProductRequest request = new ProductRequest(
                "SKU-001",
                "Valid Name",
                longDesc,
                new MoneyRequest("10.00", "USD"),
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).anySatisfy(v ->
                assertThat(v.getPropertyPath().toString()).isEqualTo("description")
        );
    }

    @Test
    @DisplayName("Description with exactly 2000 characters should be valid")
    void description_maxValidLength() {
        String validDesc = "D".repeat(2000);
        ProductRequest request = new ProductRequest(
                "SKU-001",
                "Valid Name",
                validDesc,
                new MoneyRequest("10.00", "USD"),
                UUID.randomUUID(),
                null
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("Null price should trigger violation on 'price'")
    void price_null() {
        ProductRequest request = new ProductRequest(
                "SKU-001",
                "Valid Name",
                "Valid description",
                null,
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).anySatisfy(v ->
                assertThat(v.getPropertyPath().toString()).isEqualTo("price")
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc", "10.001", "-1", "1e3"})
    @DisplayName("Invalid price amounts should trigger violation on 'price.amount'")
    void priceAmount_invalidValues(String invalidAmount) {
        ProductRequest request = new ProductRequest(
                "SKU-001",
                "Valid Name",
                "Valid description",
                new MoneyRequest(invalidAmount, "USD"),
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).anySatisfy(v ->
                assertThat(v.getPropertyPath().toString()).isEqualTo("price.amount")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"10", "10.5", "10.99"})
    @DisplayName("Valid price amounts ('10', '10.5') should pass validation")
    void priceAmount_validValues(String validAmount) {
        ProductRequest request = new ProductRequest(
                "SKU-001",
                "Valid Name",
                "Valid description",
                new MoneyRequest(validAmount, "USD"),
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);
        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().contains("price.amount"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"usd", "US", "USDC", "123"})
    @DisplayName("Invalid currencies should trigger violation on 'price.currency'")
    void currency_invalidValues(String invalidCurrency) {
        ProductRequest request = new ProductRequest(
                "SKU-001",
                "Valid Name",
                "Valid description",
                new MoneyRequest("10.00", invalidCurrency),
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).anySatisfy(v ->
                assertThat(v.getPropertyPath().toString()).isEqualTo("price.currency")
        );
    }

    @Test
    @DisplayName("Null categoryId should trigger violation on 'categoryId'")
    void categoryId_null() {
        ProductRequest request = new ProductRequest(
                "SKU-001",
                "Valid Name",
                "Valid description",
                new MoneyRequest("10.00", "USD"),
                null,
                null
        );

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).anySatisfy(v ->
                assertThat(v.getPropertyPath().toString()).isEqualTo("categoryId")
        );
    }

    @Test
    @DisplayName("Attributes map with more than 50 entries should trigger violation on 'attributes'")
    void attributes_tooManyEntries() {
        Map<String, Object> tooManyAttributes = new HashMap<>();
        for (int i = 0; i < 51; i++) {
            tooManyAttributes.put("key" + i, "value" + i);
        }

        ProductRequest request = new ProductRequest(
                "SKU-001",
                "Valid Name",
                "Valid description",
                new MoneyRequest("10.00", "USD"),
                UUID.randomUUID(),
                tooManyAttributes
        );

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).anySatisfy(v ->
                assertThat(v.getPropertyPath().toString()).isEqualTo("attributes")
        );
    }

    @Test
    @DisplayName("Attributes map with exactly 50 entries should be valid")
    void attributes_maxValidEntries() {
        Map<String, Object> validAttributes = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            validAttributes.put("key" + i, "value" + i);
        }

        ProductRequest request = new ProductRequest(
                "SKU-001",
                "Valid Name",
                "Valid description",
                new MoneyRequest("10.00", "USD"),
                UUID.randomUUID(),
                validAttributes
        );

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);
        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("attributes"));
    }
}