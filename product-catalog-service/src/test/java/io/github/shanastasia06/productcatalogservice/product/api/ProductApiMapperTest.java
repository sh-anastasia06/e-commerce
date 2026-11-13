package io.github.shanastasia06.productcatalogservice.product.api;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.common.domain.PageResult;
import io.github.shanastasia06.productcatalogservice.product.domain.*;

import io.github.shanastasia06.productcatalogservice.product.service.ProductCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class ProductApiMapperTest {
    private ProductApiMapper mapper;
    private final String USD = "USD";

    @BeforeEach
    void setUp() {
        // MapStruct generates the Impl class at compile-time
        mapper = new ProductApiMapperImpl();
    }

    @Test
    @DisplayName("toCommand should correctly map ProductRequest to ProductCommand with BigDecimal price amount")
    void toCommand_mapsSuccessfully() {
        MoneyRequest moneyRequest = new MoneyRequest("10.50", USD);
        ProductRequest request = new ProductRequest(
                "SKU-123",
                "Test Product",
                "Test Description",
                moneyRequest,
                UUID.randomUUID(),
                Map.of("key", "value")
        );

        ProductCommand command = mapper.toCommand(request);

        assertThat(command).isNotNull();
        assertThat(command.sku()).isEqualTo("SKU-123");
        assertThat(command.name()).isEqualTo("Test Product");
        assertThat(command.description()).isEqualTo("Test Description");
        assertThat(command.priceAmount()).isEqualByComparingTo(new BigDecimal("10.50"));
        assertThat(command.priceCurrency()).isEqualTo(USD);
        assertThat(command.categoryId()).isEqualTo(request.categoryId());
        assertThat(command.attributes()).containsEntry("key", "value");
    }

    @Test
    @DisplayName("toResponse should correctly carry every field including status, price amount string, and timestamps")
    void toResponse_mapsSuccessfully() {
        UUID categoryId = UUID.randomUUID();
        Instant now = Instant.now();

        Product product = Product.create(
                new Sku("SKU-123"),
                new ProductName("Test Product"),
                "Test Description",
                new Money(new BigDecimal("10.50"), Currency.getInstance(USD)),
                new CategoryId(categoryId),
                new Attributes(Map.of()),
                now
        );

        UUID productId = product.getId().value();

        ProductResponse response = mapper.toResponse(product);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(productId);
        assertThat(response.sku()).isEqualTo("SKU-123");
        assertThat(response.name()).isEqualTo("Test Product");
        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.price().amount()).isEqualTo("10.50");
        assertThat(response.price().currency()).isEqualTo(USD);
        assertThat(response.categoryId()).isEqualTo(categoryId);
        assertThat(response.attributes()).isEmpty();
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("toPageResponse should copy pagination metadata and map every element in content")
    void toPageResponse_mapsSuccessfully() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Instant now = Instant.now();

        Product product = Product.create(
                new Sku("SKU-123"),
                new ProductName("Test Product"),
                "Test Description",
                new Money(new BigDecimal("15.00"), Currency.getInstance(USD)),
                new CategoryId(categoryId),
                new Attributes(Map.of("color", "black")),
                now
        );

        product.activate(Instant.now());

        PageResult<Product> pageResult = new PageResult<>(
                List.of(product),
                0,
                10,
                1L
        );

        ProductPageResponse pageResponse = mapper.toPageResponse(pageResult);

        assertThat(pageResponse).isNotNull();
        assertThat(pageResponse.page()).isEqualTo(0);
        assertThat(pageResponse.size()).isEqualTo(10);
        assertThat(pageResponse.totalElements()).isEqualTo(1L);
        assertThat(pageResponse.totalPages()).isEqualTo(1);
        assertThat(pageResponse.content()).hasSize(1);

        ProductResponse mappedContent = pageResponse.content().get(0);
        assertThat(mappedContent.sku()).isEqualTo("SKU-123");
        assertThat(mappedContent.price().amount()).isEqualTo("15.00");
        assertThat(mappedContent.price().currency()).isEqualTo(USD);
        assertThat(mappedContent.attributes()).containsEntry("color", "black");
    }
}