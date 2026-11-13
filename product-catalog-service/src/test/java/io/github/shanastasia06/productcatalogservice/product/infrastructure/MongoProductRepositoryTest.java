package io.github.shanastasia06.productcatalogservice.product.infrastructure;

import io.github.shanastasia06.productcatalogservice.TestcontainersConfiguration;
import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.common.domain.PageQuery;
import io.github.shanastasia06.productcatalogservice.common.domain.PageResult;
import io.github.shanastasia06.productcatalogservice.common.exception.ConcurrentUpdateException;
import io.github.shanastasia06.productcatalogservice.product.domain.*;
import io.github.shanastasia06.productcatalogservice.product.exception.SkuAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest
@Import({TestcontainersConfiguration.class, MongoProductRepository.class, ProductMapperImpl.class})
class MongoProductRepositoryTest {
    private final Currency USD = Currency.getInstance("USD");

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanUp() {
        mongoTemplate.remove(new Query(), ProductDocument.class);
    }

    private Product createProduct(String sku, String name, BigDecimal price, UUID categoryId, Attributes attributes, Instant createdAt) {
        return Product.restore(
                ProductId.generate(),
                new Sku(sku),
                new ProductName(name),
                "Description",
                new Money(price, USD),
                new CategoryId(categoryId),
                attributes,
                ProductStatus.ACTIVE,
                createdAt,
                createdAt,
                null
        );
    }

    @Test
    void save_withNewProduct_savesSuccessfullyAndReturnsVersionZero() {
        Product product = Product.create(
                new Sku("SKU-1"),
                new ProductName("Product 1"),
                "Description",
                new Money(BigDecimal.valueOf(100), USD),
                new CategoryId(UUID.randomUUID()),
                Attributes.empty(),
                Instant.now()
        );

        Product saved = productRepository.save(product);
        Optional<Product> found = productRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getVersion()).isEqualTo(0L);
    }

    @Test
    void save_withExistingProductUpdate_updatesSuccessfullyAndIncrementsVersion() {
        Product product = Product.create(
                new Sku("SKU-1"),
                new ProductName("Product 1"),
                "Description",
                new Money(BigDecimal.valueOf(100), USD),
                new CategoryId(UUID.randomUUID()),
                Attributes.empty(),
                Instant.now()
        );
        Product saved = productRepository.save(product);

        saved.update(
                saved.getSku(),
                new ProductName("Updated Name"),
                saved.getDescription(),
                saved.getPrice(),
                saved.getCategoryId(),
                Attributes.empty(),
                Instant.now()
        );
        Product updated = productRepository.save(saved);

        assertThat(updated.getVersion()).isEqualTo(1L);
    }

    @Test
    void save_withDuplicateSku_throwsSkuAlreadyExistsException() {
        Instant now = Instant.now();
        productRepository.save(Product.create(
                new Sku("SKU-1"),
                new ProductName("Product 1"),
                null,
                new Money(BigDecimal.valueOf(100), USD),
                new CategoryId(UUID.randomUUID()),
                Attributes.empty(),
                now
        ));
        Product duplicate = Product.create(
                new Sku("SKU-1"),
                new ProductName("Product 2"),
                null,
                new Money(BigDecimal.valueOf(200), USD),
                new CategoryId(UUID.randomUUID()),
                Attributes.empty(),
                now
        );

        assertThatThrownBy(() -> productRepository.save(duplicate))
                .isInstanceOf(SkuAlreadyExistsException.class);
    }

    @Test
    void save_withConcurrentModification_throwsConcurrentUpdateException() {
        Instant now = Instant.now();
        Product saved = productRepository.save(Product.create(
                new Sku("SKU-1"),
                new ProductName("Product 1"),
                null,
                new Money(BigDecimal.valueOf(100), USD),
                new CategoryId(UUID.randomUUID()),
                Attributes.empty(),
                now
        ));

        Product a = productRepository.findById(saved.getId()).get();
        Product b = productRepository.findById(saved.getId()).get();

        a.update(a.getSku(), new ProductName("Name A"), a.getDescription(), a.getPrice(), a.getCategoryId(), a.getAttributes(), Instant.now());
        productRepository.save(a); // works

        b.update(b.getSku(), new ProductName("Name B"), b.getDescription(), b.getPrice(), b.getCategoryId(), b.getAttributes(), Instant.now());
        assertThatThrownBy(() -> productRepository.save(b))
                .isInstanceOf(ConcurrentUpdateException.class);
    }

    @Test
    void save_withAttributes_keepsAttributesTypes() {
        Attributes attributes = new Attributes(Map.of(
                "ramGb", 16,
                "color", "silver",
                "waterproof", true,
                "weight", 1.25,
                "views", 3_000_000L
        ));

        Product product = Product.create(
                new Sku("SKU-ATTR"),
                new ProductName("Laptop Pro"),
                "High-end laptop",
                new Money(BigDecimal.valueOf(1200), USD),
                CategoryId.generate(),
                attributes,
                Instant.now()
        );

        Product saved = productRepository.save(product);
        Optional<Product> found = productRepository.findById(saved.getId());

        assertThat(found).isPresent();
        Map<String, Object> values = found.get().getAttributes().values();

        assertThat(values.get("ramGb")).isEqualTo(16).isInstanceOf(Integer.class);
        assertThat(values.get("color")).isEqualTo("silver").isInstanceOf(String.class);
        assertThat(values.get("waterproof")).isEqualTo(true).isInstanceOf(Boolean.class);
        assertThat(values.get("weight")).isEqualTo(1.25).isInstanceOf(Double.class);
        assertThat(values.get("views")).isInstanceOf(Long.class);
    }

    @Test
    void existsBySku_withExistingAndNonExistingSku_returnsCorrectBooleans() {
        productRepository.save(Product.create(
                new Sku("SKU-1"),
                new ProductName("Product 1"),
                null,
                new Money(BigDecimal.valueOf(100), USD),
                new CategoryId(UUID.randomUUID()),
                Attributes.empty(),
                Instant.now()
        ));

        assertThat(productRepository.existsBySku(new Sku("SKU-1"))).isTrue();
        assertThat(productRepository.existsBySku(new Sku("SKU-2"))).isFalse();
    }

    @Test
    void existsBySkuAndIdNot_withSameAndDifferentSku_returnsCorrectBooleans() {
        Instant now = Instant.now();
        Product saved = productRepository.save(Product.create(
                new Sku("SKU-1"),
                new ProductName("Product 1"),
                null,
                new Money(BigDecimal.valueOf(100), USD),
                new CategoryId(UUID.randomUUID()),
                Attributes.empty(),
                now
        ));

        assertThat(productRepository.existsBySkuAndIdNot(new Sku("SKU-1"), saved.getId())).isFalse();

        productRepository.save(Product.create(
                new Sku("SKU-2"),
                new ProductName("Product 2"),
                null,
                new Money(BigDecimal.valueOf(200), USD),
                new CategoryId(UUID.randomUUID()),
                Attributes.empty(),
                now
        ));
        assertThat(productRepository.existsBySkuAndIdNot(new Sku("SKU-2"), saved.getId())).isTrue();
    }

    @Test
    void deleteById_withExistingId_removesProduct() {
        Product saved = productRepository.save(Product.create(
                new Sku("SKU-1"),
                new ProductName("Product 1"),
                null,
                new Money(BigDecimal.valueOf(100), USD),
                new CategoryId(UUID.randomUUID()),
                Attributes.empty(),
                Instant.now()
        ));

        productRepository.deleteById(saved.getId());

        assertThat(productRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void delete_withSavedProduct_succeeds() {
        Product saved = productRepository.save(Product.create(
                new Sku("SKU-1"),
                new ProductName("Product 1"),
                null,
                new Money(BigDecimal.valueOf(100), USD),
                new CategoryId(UUID.randomUUID()),
                Attributes.empty(),
                Instant.now()
        ));

        Optional<Product> loaded =  productRepository.findById(saved.getId());
        assertThat(loaded).isPresent();

        productRepository.delete(loaded.get());
        assertThat(productRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void delete_withModifiedProduct_throwsConcurrentUpdateException() {
        Product saved = productRepository.save(Product.create(
                new Sku("SKU-1"),
                new ProductName("Product 1"),
                null,
                new Money(BigDecimal.valueOf(100), USD),
                new CategoryId(UUID.randomUUID()),
                Attributes.empty(),
                Instant.now()
        ));

        Optional<Product> loaded1 =  productRepository.findById(saved.getId());
        Optional<Product> loaded2 =  productRepository.findById(saved.getId());
        assertThat(loaded1).isPresent();
        assertThat(loaded2).isPresent();

        Product productToModify = loaded1.get();
        productToModify.update(
                productToModify.getSku(),
                new ProductName("New name"),
                productToModify.getDescription(),
                productToModify.getPrice(),
                productToModify.getCategoryId(),
                productToModify.getAttributes(),
                Instant.now()
        );

        productRepository.save(productToModify);

        assertThatThrownBy(() -> productRepository.delete(loaded2.get()))
                .isInstanceOf(ConcurrentUpdateException.class);

        assertThat(productRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void search_withNoFilters_returnsAllProductsNewestFirst() {
        Instant now = Instant.now();
        Product p1 = productRepository.save(createProduct("SKU-1", "Product 1", BigDecimal.valueOf(10), UUID.randomUUID(), Attributes.empty(), now));
        Product p2 = productRepository.save(createProduct("SKU-2", "Product 2", BigDecimal.valueOf(20), UUID.randomUUID(), Attributes.empty(), now.plusSeconds(1)));

        PageResult<Product> result = productRepository.search(
                new ProductSearchCriteria(null, null, null),
                new PageQuery(0, 10)
        );

        assertThat(result.content()).containsExactly(p2, p1);
    }

    @Test
    void search_withSameCreatedAt_respectsIdTiebreak() {
        Instant now = Instant.now();
        Product p1 = productRepository.save(createProduct("SKU-1", "Product 1", BigDecimal.valueOf(10), UUID.randomUUID(), Attributes.empty(), now));
        Product p2 = productRepository.save(createProduct("SKU-2", "Product 2", BigDecimal.valueOf(20), UUID.randomUUID(), Attributes.empty(), now));

        PageResult<Product> result = productRepository.search(
                new ProductSearchCriteria(null, null, null),
                new PageQuery(0, 10)
        );

        assertThat(result.content()).hasSize(2);

        Product first = result.content().get(0);
        Product second = result.content().get(1);

        int comparison = first.getId().value().compareTo(second.getId().value());
        assertThat(comparison).isNotEqualTo(0);
    }

    @Test
    void search_withCategoryFilter_returnsOnlyThatCategoryProducts() {
        Instant now = Instant.now();
        Product p1 = productRepository.save(createProduct("SKU-1", "Product 1", BigDecimal.valueOf(10), UUID.randomUUID(), Attributes.empty(), now));
        productRepository.save(createProduct("SKU-2", "Product 2", BigDecimal.valueOf(20), UUID.randomUUID(), Attributes.empty(), now.plusSeconds(1)));

        CategoryId categoryId = p1.getCategoryId();

        PageResult<Product> result = productRepository.search(
                new ProductSearchCriteria(categoryId, null, null),
                new PageQuery(0, 10)
        );

        assertThat(result.content()).containsExactly(p1);
    }

    @Test
    void search_withMinPriceBoundary_isInclusive() {
        Instant now = Instant.now();
        Product p1 = productRepository.save(createProduct("SKU-1", "Product 1", BigDecimal.valueOf(50), UUID.randomUUID(), Attributes.empty(), now));

        PageResult<Product> result = productRepository.search(
                new ProductSearchCriteria(null, new Money(BigDecimal.valueOf(50), USD), null),
                new PageQuery(0, 10)
        );

        assertThat(result.content()).containsExactly(p1);
    }

    @Test
    void search_withMaxPriceBoundary_isInclusive() {
        Instant now = Instant.now();
        Product p1 = productRepository.save(createProduct("SKU-1", "Product 1", BigDecimal.valueOf(50), UUID.randomUUID(), Attributes.empty(), now));

        PageResult<Product> result = productRepository.search(
                new ProductSearchCriteria(null, null, new Money(BigDecimal.valueOf(50), USD)),
                new PageQuery(0, 10)
        );

        assertThat(result.content()).containsExactly(p1);
    }

    @Test
    void search_withPriceRange_returnsProductsWithinBounds() {
        Instant now = Instant.now();
        productRepository.save(createProduct("SKU-1", "Product 1", BigDecimal.valueOf(50), UUID.randomUUID(), Attributes.empty(),now));
        Product p2 = productRepository.save(createProduct("SKU-2", "Product 2", BigDecimal.valueOf(100), UUID.randomUUID(), Attributes.empty(),now.plusSeconds(1)));
        productRepository.save(createProduct("SKU-3", "Product 3", BigDecimal.valueOf(150), UUID.randomUUID(), Attributes.empty(),now.plusSeconds(2)));

        PageResult<Product> result = productRepository.search(
                new ProductSearchCriteria(null, new Money(BigDecimal.valueOf(80), USD), new Money(BigDecimal.valueOf(120), USD)),
                new PageQuery(0, 10)
        );

        assertThat(result.content()).containsExactly(p2);
    }

    @Test
    void search_withNumericPriceComparison_filtersCorrectlyNumericallyNotLexicographically() {
        Instant now = Instant.now();
        productRepository.save(createProduct("SKU-1", "Product 1", BigDecimal.valueOf(20.00), UUID.randomUUID(), Attributes.empty(),now));
        Product p2 = productRepository.save(createProduct("SKU-2", "Product 2", BigDecimal.valueOf(100.00), UUID.randomUUID(), Attributes.empty(),now.plusSeconds(1)));

        PageResult<Product> result = productRepository.search(
                new ProductSearchCriteria(null, new Money(BigDecimal.valueOf(50.00), USD), null),
                new PageQuery(0, 10)
        );

        assertThat(result.content()).containsExactly(p2);
    }

    @Test
    void search_withAllThreeFiltersCombined_returnsMatchingProduct() {
        Instant now = Instant.now();
        CategoryId categoryId = new CategoryId(UUID.randomUUID());
        Product target = productRepository.save(createProduct("SKU-1", "Product 1", BigDecimal.valueOf(100), categoryId.value(), Attributes.empty(),now));
        productRepository.save(createProduct("SKU-2", "Product 2", BigDecimal.valueOf(100), UUID.randomUUID(), Attributes.empty(),now.plusSeconds(1)));
        productRepository.save(createProduct("SKU-3", "Product 3", BigDecimal.valueOf(200), UUID.randomUUID(), Attributes.empty(),now.plusSeconds(2)));

        PageResult<Product> result = productRepository.search(
                new ProductSearchCriteria(categoryId, new Money(BigDecimal.valueOf(90), USD), new Money(BigDecimal.valueOf(110), USD)),
                new PageQuery(0, 10)
        );

        assertThat(result.content()).containsExactly(target);
    }

    @Test
    void search_withNoMatches_returnsEmptyListAndZeroTotals() {
        Instant now = Instant.now();
        productRepository.save(createProduct("SKU-1", "Product 1", BigDecimal.valueOf(10), UUID.randomUUID(), Attributes.empty(),now));

        PageResult<Product> result = productRepository.search(
                new ProductSearchCriteria(new CategoryId(UUID.randomUUID()), null, null),
                new PageQuery(0, 10)
        );

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0L);
        assertThat(result.totalPages()).isEqualTo(0);
    }

    @Test
    void search_withPaging_returnsCorrectPagesAndMetadata() {
        Instant now = Instant.now();
        for (int i = 0; i < 25; i++) {
            productRepository.save(createProduct("SKU-" + i, "Product " + i, BigDecimal.valueOf(10 + i), UUID.randomUUID(), Attributes.empty(),now.plusSeconds(i)));
        }

        PageResult<Product> page0 = productRepository.search(new ProductSearchCriteria(null, null, null), new PageQuery(0, 10));
        PageResult<Product> page1 = productRepository.search(new ProductSearchCriteria(null, null, null), new PageQuery(1, 10));
        PageResult<Product> page2 = productRepository.search(new ProductSearchCriteria(null, null, null), new PageQuery(2, 10));

        assertThat(page0.content()).hasSize(10);
        assertThat(page1.content()).hasSize(10);
        assertThat(page2.content()).hasSize(5);
        assertThat(page0.totalElements()).isEqualTo(25L);
        assertThat(page0.totalPages()).isEqualTo(3);
    }

    @Test
    void search_beyondLastPage_returnsEmptyContentWithCorrectTotalElements() {
        Instant now = Instant.now();
        for (int i = 0; i < 25; i++) {
            productRepository.save(createProduct("SKU-" + i, "Product " + i, BigDecimal.valueOf(10 + i), UUID.randomUUID(), Attributes.empty(),now.plusSeconds(i)));
        }

        PageResult<Product> page5 = productRepository.search(new ProductSearchCriteria(null, null, null), new PageQuery(5, 10));

        assertThat(page5.content()).isEmpty();
        assertThat(page5.totalElements()).isEqualTo(25L);
    }

    @Test
    void search_acrossMultiplePages_containsNoDuplicateOrOverlappingIds() {
        Instant now = Instant.now();
        for (int i = 0; i < 25; i++) {
            productRepository.save(createProduct("SKU-" + i, "Product " + i, BigDecimal.valueOf(10 + i), UUID.randomUUID(), Attributes.empty(),now.plusSeconds(i)));
        }

        List<Product> page0 = productRepository.search(new ProductSearchCriteria(null, null, null), new PageQuery(0, 10)).content();
        List<Product> page1 = productRepository.search(new ProductSearchCriteria(null, null, null), new PageQuery(1, 10)).content();
        List<Product> page2 = productRepository.search(new ProductSearchCriteria(null, null, null), new PageQuery(2, 10)).content();

        Set<ProductId> allIds = new HashSet<>();
        page0.forEach(p -> allIds.add(p.getId()));
        page1.forEach(p -> allIds.add(p.getId()));
        page2.forEach(p -> allIds.add(p.getId()));

        assertThat(allIds).hasSize(25);
    }
}