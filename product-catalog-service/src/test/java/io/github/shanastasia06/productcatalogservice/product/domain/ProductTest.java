package io.github.shanastasia06.productcatalogservice.product.domain;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import io.github.shanastasia06.productcatalogservice.product.exception.InvalidProductStateException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;

public class ProductTest {
    private final Sku sku = new Sku("PROD-123");
    private final ProductName name = new ProductName("Test Product");
    private final Money price = new Money(new BigDecimal("99.99"), Currency.getInstance("USD"));
    private final CategoryId categoryId = new CategoryId(UUID.randomUUID());
    private final Instant now = Instant.now();

    @Test
    void create_withNormalizedDescription_succeeds() {
        Product product = Product.create(sku, name, "  Valid description with spaces  ", price, categoryId, Attributes.empty(), now);

        assertThat(product).isNotNull();
        assertThat(product.getId()).isNotNull();
        assertThat(product.getSku()).isEqualTo(sku);
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getDescription()).isEqualTo("Valid description with spaces");
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getCategoryId()).isEqualTo(categoryId);
        assertThat(product.getAttributes()).isEqualTo(Attributes.empty());
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(product.getCreatedAt()).isEqualTo(now);
        assertThat(product.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void create_withNullOrBlankDescription_succeeds() {
        Product productNull = Product.create(sku, name, null, price, categoryId, Attributes.empty(), now);
        Product productBlank = Product.create(sku, name, "     ", price, categoryId, Attributes.empty(), now);

        assertThat(productNull.getDescription()).isNull();
        assertThat(productBlank.getDescription()).isNull();
    }

    @Test
    void create_withDescriptionExceedsMaxLength_throws() {
        String tooLongDescription = "A".repeat(Product.DESCRIPTION_MAX_LENGTH + 1);

        assertThatThrownBy(() -> Product.create(sku, name, tooLongDescription, price, categoryId, Attributes.empty(), now))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Product description must not be longer than 2000 characters");
    }

    @Test
    void create_withRequiredFieldIsNull_throws() {
        assertThatThrownBy(() -> Product.create(null, name, "Desc", price, categoryId, Attributes.empty(), now))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Product sku must not be null");
    }

    @Test
    void shouldEvaluateEqualsAndHashCodeOnlyById() {
        Product product1 = Product.create(sku, name, "Desc 1", price, categoryId, Attributes.empty(), now);
        Product product2 = Product.create(sku, name, "Desc 2", price, categoryId, Attributes.empty(), now);

        assertThat(product1).isEqualTo(product1);
        assertThat(product1.hashCode()).isEqualTo(product1.hashCode());

        assertThat(product1).isNotEqualTo(product2);
    }

    @Test
    void restore_withExistingProduct_succeeds() {
        ProductId id = new ProductId(UUID.randomUUID());
        Sku sku = new Sku("PROD-999");
        ProductName name = new ProductName("Restored Product");
        Money price = new Money(new BigDecimal("150.00"), Currency.getInstance("USD"));
        CategoryId categoryId = new CategoryId(UUID.randomUUID());
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-05T12:00:00Z");
        Long version = 3L;

        Product product = Product.restore(
                id, sku, name, "  Restored description  ",
                price, categoryId, Attributes.empty(), ProductStatus.ACTIVE, createdAt, updatedAt, version
        );

        assertThat(product).isNotNull();
        assertThat(product.getId()).isEqualTo(id);
        assertThat(product.getSku()).isEqualTo(sku);
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getDescription()).isEqualTo("Restored description");
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getCategoryId()).isEqualTo(categoryId);
        assertThat(product.getAttributes()).isEqualTo(Attributes.empty());
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.getCreatedAt()).isEqualTo(createdAt);
        assertThat(product.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(product.getVersion()).isEqualTo(version);
    }

    @Test
    void restore_WithNullId_throws() {
        Sku sku = new Sku("PROD-999");
        ProductName name = new ProductName("Restored Product");
        Money price = new Money(new BigDecimal("150.00"), Currency.getInstance("USD"));
        CategoryId categoryId = new CategoryId(UUID.randomUUID());
        Instant now = Instant.now();

        assertThatThrownBy(() -> Product.restore(
                null, sku, name, "desc", price, categoryId, Attributes.empty(),
                ProductStatus.ACTIVE, now, now, 0L
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Product id must not be null");
    }

    @Test
    void activate_withValidInstant_changesStatusToActive() {
        Product product = ProductFixtures.draftProduct();
        Instant activationTime = now.plusSeconds(3600);

        product.activate(activationTime);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.getUpdatedAt()).isEqualTo(activationTime);
    }

    @Test
    void activate_withNullInstant_throwsNullPointerException() {
        Product product = ProductFixtures.draftProduct();

        assertThatThrownBy(() -> product.activate(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("now must not be null");
    }

    @Test
    void activate_withActiveStatus_throwsInvalidProductStateException() {
        Product product = ProductFixtures.draftProduct();
        product.activate(now);

        assertThatThrownBy(() -> product.activate(now))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("cannot be activated from status ACTIVE");
    }

    @Test
    void activate_withZeroPrice_throwsInvalidProductStateException() {
        Money zeroPrice = new Money(BigDecimal.ZERO, Currency.getInstance("USD"));
        Product product = Product.create(sku, name, "Desc", zeroPrice, categoryId, Attributes.empty(), now);

        assertThatThrownBy(() -> product.activate(now))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("cannot be activated with a zero price");
    }

    @Test
    void archive_withValidInstant_changesStatusToArchived() {
        Product product = ProductFixtures.draftProduct();
        Instant archiveTime = now.plusSeconds(3600);

        product.archive(archiveTime);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
        assertThat(product.getUpdatedAt()).isEqualTo(archiveTime);
    }

    @Test
    void archive_withNullInstant_throwsNullPointerException() {
        Product product = ProductFixtures.draftProduct();

        assertThatThrownBy(() -> product.archive(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("now must not be null");
    }

    @Test
    void archive_withArchivedStatus_throwsInvalidProductStateException() {
        Product product = ProductFixtures.draftProduct();
        product.archive(now);

        assertThatThrownBy(() -> product.archive(now.plusSeconds(10)))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("cannot be archived from status ARCHIVED");
    }

    @Test
    void ensureCanBeDeleted_withDraftStatus_doesNothing() {
        Product product = ProductFixtures.draftProduct();

        assertThatCode(product::ensureCanBeDeleted).doesNotThrowAnyException();
    }

    @Test
    void ensureCanBeDeleted_withActiveStatus_throwsInvalidProductStateException() {
        Product product = ProductFixtures.draftProduct();
        product.activate(now);

        assertThatThrownBy(product::ensureCanBeDeleted)
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("cannot be deleted from status ACTIVE, archive it instead");
    }

    @Test
    void ensureCanBeDeleted_withArchivedStatus_throwsInvalidProductStateException() {
        Product product = ProductFixtures.draftProduct();
        product.archive(now);

        assertThatThrownBy(product::ensureCanBeDeleted)
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("cannot be deleted from status ARCHIVED, archive it instead");
    }

    @Test
    void lifecycle_fullWorkflow_executesSuccessfullyAndRestrictsInvalidTransitions() {
        Product product = ProductFixtures.draftProduct();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);

        Instant activateTime = now.plusSeconds(10);
        product.activate(activateTime);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.getUpdatedAt()).isEqualTo(activateTime);

        Instant archiveTime = now.plusSeconds(20);
        product.archive(archiveTime);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
        assertThat(product.getUpdatedAt()).isEqualTo(archiveTime);

        assertThatThrownBy(() -> product.activate(now.plusSeconds(30)))
                .isInstanceOf(InvalidProductStateException.class);

        assertThatThrownBy(() -> product.archive(now.plusSeconds(30)))
                .isInstanceOf(InvalidProductStateException.class);
    }

    @Test
    void create_withAttributes_executesSuccessfully() {
        Attributes attributes = new Attributes(Map.of(
                "ramGb", 16,
                "color", "silver",
                "waterproof", true,
                "weight", new BigDecimal("1.25"))
        );
        Product product = Product.create(
                sku,
                name,
                "Description",
                price,
                categoryId,
                attributes,
                now
        );
        assertThat(product).isNotNull();
        assertThat(product.getSku()).isEqualTo(sku);
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getCategoryId()).isEqualTo(categoryId);
        assertThat(product.getAttributes()).isEqualTo(attributes);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
    }

    @Test
    void update_validParametersInDraftStatus_updatesAllFields() {
        Product product = ProductFixtures.draftProduct();

        Sku newSku = new Sku("NEW-SKU-123");
        ProductName newName = new ProductName("New Name");
        Money newPrice = new Money(new BigDecimal("149.99"), Currency.getInstance("USD"));
        CategoryId newCategoryId = new CategoryId(UUID.randomUUID());
        Instant updateTime = now.plusSeconds(60);

        product.update(newSku, newName, "  New Desc  ", newPrice, newCategoryId, Attributes.empty(), updateTime);

        assertThat(product.getSku()).isEqualTo(newSku);
        assertThat(product.getName()).isEqualTo(newName);
        assertThat(product.getDescription()).isEqualTo("New Desc");
        assertThat(product.getPrice()).isEqualTo(newPrice);
        assertThat(product.getCategoryId()).isEqualTo(newCategoryId);
        assertThat(product.getAttributes()).isEqualTo(Attributes.empty());
        assertThat(product.getUpdatedAt()).isEqualTo(updateTime);
    }

    @Test
    void update_withArchivedStatus_throwsInvalidProductStateException() {
        Product product = ProductFixtures.draftProduct();
        product.archive(now);

        assertThatThrownBy(() -> product.update(sku, name, "Desc", price, categoryId, Attributes.empty(), now.plusSeconds(10)))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("is archived and cannot be modified");
    }

    @Test
    void update_withActiveStatusWithZeroPrice_throwsInvalidProductStateException() {
        Product product = ProductFixtures.draftProduct();
        product.activate(now);

        Money zeroPrice = new Money(BigDecimal.ZERO, Currency.getInstance("USD"));

        assertThatThrownBy(() -> product.update(sku, name, "Desc", zeroPrice, categoryId, Attributes.empty(), now.plusSeconds(10)))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("is active and must have a price greater than zero");
    }

    @Test
    void update_withNullSku_throwsNullPointerException() {
        Product product = ProductFixtures.draftProduct();

        assertThatThrownBy(() -> product.update(null, name, "Desc", price, categoryId, Attributes.empty(), now))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Product sku must not be null");
    }
}
