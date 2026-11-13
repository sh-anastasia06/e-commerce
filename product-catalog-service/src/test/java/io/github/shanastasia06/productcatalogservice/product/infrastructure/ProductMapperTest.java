package io.github.shanastasia06.productcatalogservice.product.infrastructure;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.product.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ProductMapperTest {
    private final ProductMapper mapper = new ProductMapperImpl();
    private final Instant fixedInstant = Instant.parse("2026-11-08T12:00:00Z");

    @Test
    void toDocument_shouldCopyAllFields_includingSplitPriceAndDraftStatus() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Product product = Product.restore(
                new ProductId(productId),
                new Sku("SKU-001"),
                new ProductName("Test Product"),
                "Description text",
                new Money(new BigDecimal("10.50"), Currency.getInstance("USD")),
                new CategoryId(categoryId),
                Attributes.empty(),
                ProductStatus.DRAFT,
                fixedInstant,
                fixedInstant,
                null
        );

        ProductDocument document = mapper.toDocument(product);

        assertThat(document).isNotNull();
        assertThat(document.getId()).isEqualTo(productId);
        assertThat(document.getSku()).isEqualTo("SKU-001");
        assertThat(document.getName()).isEqualTo("Test Product");
        assertThat(document.getDescription()).isEqualTo("Description text");
        assertThat(document.getPriceAmount()).isEqualByComparingTo(new BigDecimal("10.50"));
        assertThat(document.getPriceCurrency()).isEqualTo("USD");
        assertThat(document.getCategoryId()).isEqualTo(categoryId);
        assertThat(document.getStatus()).isEqualTo("DRAFT");
        assertThat(document.getCreatedAt()).isEqualTo(fixedInstant);
        assertThat(document.getVersion()).isNull();
    }

    @Test
    void roundTrip_shouldPreserveAllFields_forNewProduct() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Product original = Product.restore(
                new ProductId(productId),
                new Sku("SKU-NEW"),
                new ProductName("New Product"),
                "Some description",
                new Money(new BigDecimal("99.99"), Currency.getInstance("EUR")),
                new CategoryId(categoryId),
                Attributes.empty(),
                ProductStatus.DRAFT,
                fixedInstant,
                fixedInstant,
                null
        );

        ProductDocument document = mapper.toDocument(original);
        Product roundTripped = mapper.toDomain(document);

        assertThat(roundTripped).isNotNull();
        assertThat(roundTripped.getId().value()).isEqualTo(original.getId().value());
        assertThat(roundTripped.getSku().value()).isEqualTo(original.getSku().value());
        assertThat(roundTripped.getName().value()).isEqualTo(original.getName().value());
        assertThat(roundTripped.getDescription()).isEqualTo(original.getDescription());
        assertThat(roundTripped.getPrice().amount()).isEqualByComparingTo(original.getPrice().amount());
        assertThat(roundTripped.getPrice().currency()).isEqualTo(original.getPrice().currency());
        assertThat(roundTripped.getStatus()).isEqualTo(original.getStatus());
        assertThat(roundTripped.getVersion()).isNull();
    }

    @Test
    void roundTrip_shouldPreserveArchivedStatusAndVersion() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Product original = Product.restore(
                new ProductId(productId),
                new Sku("SKU-ARCHIVED"),
                new ProductName("Archived Product"),
                "Description",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(categoryId),
                Attributes.empty(),
                ProductStatus.ARCHIVED,
                fixedInstant,
                fixedInstant,
                5L
        );

        ProductDocument document = mapper.toDocument(original);
        Product roundTripped = mapper.toDomain(document);

        assertThat(roundTripped.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
        assertThat(roundTripped.getVersion()).isEqualTo(5L);
    }

    @Test
    void shouldSurviveNullDescription() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Product original = Product.restore(
                new ProductId(productId),
                new Sku("SKU-NODESC"),
                new ProductName("No Description Product"),
                null,
                new Money(new BigDecimal("10.00"), Currency.getInstance("USD")),
                new CategoryId(categoryId),
                Attributes.empty(),
                ProductStatus.ACTIVE,
                fixedInstant,
                fixedInstant,
                1L
        );

        ProductDocument document = mapper.toDocument(original);
        assertThat(document.getDescription()).isNull();

        Product roundTripped = mapper.toDomain(document);
        assertThat(roundTripped.getDescription()).isNull();
    }

    @Test
    void toDomain_shouldHandleBigDecimalScale_forPriceAmount() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        ProductDocument document = new ProductDocument();
        document.setId(productId);
        document.setSku("SKU-SCALE");
        document.setName("Scale Test");
        document.setDescription("Desc");
        document.setPriceAmount(new BigDecimal("10.5"));
        document.setPriceCurrency("USD");
        document.setCategoryId(categoryId);
        document.setStatus("ACTIVE");
        document.setCreatedAt(fixedInstant);
        document.setUpdatedAt(fixedInstant);
        document.setVersion(1L);

        Product product = mapper.toDomain(document);

        assertThat(product.getPrice().amount()).isEqualByComparingTo(new BigDecimal("10.50"));
    }

    @Test
    void toDomain_shouldThrowException_whenStatusIsUnknown() {
        ProductDocument document = new ProductDocument();
        document.setId(UUID.randomUUID());
        document.setSku("SKU-ERR");
        document.setName("Error Product");
        document.setPriceAmount(BigDecimal.TEN);
        document.setPriceCurrency("USD");
        document.setCategoryId(UUID.randomUUID());
        document.setStatus("UNKNOWN");
        document.setCreatedAt(fixedInstant);
        document.setUpdatedAt(fixedInstant);
        document.setVersion(1L);

        assertThatThrownBy(() -> mapper.toDomain(document))
                .isInstanceOf(IllegalArgumentException.class);
    }
}