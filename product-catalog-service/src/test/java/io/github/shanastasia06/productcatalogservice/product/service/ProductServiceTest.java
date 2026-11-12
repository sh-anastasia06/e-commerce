package io.github.shanastasia06.productcatalogservice.product.service;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.common.domain.PageQuery;
import io.github.shanastasia06.productcatalogservice.common.domain.PageResult;
import io.github.shanastasia06.productcatalogservice.common.exception.ConcurrentUpdateException;
import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import io.github.shanastasia06.productcatalogservice.product.domain.*;
import io.github.shanastasia06.productcatalogservice.product.exception.CategoryReferenceNotFoundException;
import io.github.shanastasia06.productcatalogservice.product.exception.InvalidProductStateException;
import io.github.shanastasia06.productcatalogservice.product.exception.ProductNotFoundException;
import io.github.shanastasia06.productcatalogservice.product.exception.SkuAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ProductServiceTest {
    private ProductRepository productRepository;
    private CategoryExistenceChecker categoryExistenceChecker;
    private ProductService productService;
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-09T12:00:00Z"), ZoneOffset.UTC);
    private static final Instant CREATED_AT_INSTANT = Instant.parse("2026-11-01T10:00:00Z");

    private static final UUID VALID_CATEGORY_ID = UUID.randomUUID();
    private static final UUID VALID_PRODUCT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        categoryExistenceChecker = mock(CategoryExistenceChecker.class);
        productService = new ProductService(
                productRepository,
                categoryExistenceChecker,
                clock,
                Currency.getInstance("USD")
        );
    }


    @Test
    void create_validCommand_savesAndReturnsProduct() {
        ProductCommand command = new ProductCommand(
                "SKU-123",
                "Test Product",
                "Description",
                new BigDecimal("99.99"),
                "USD",
                VALID_CATEGORY_ID,
                Map.of("color", "red")
        );

        when(categoryExistenceChecker.exists(any())).thenReturn(true);
        when(productRepository.existsBySku(any())).thenReturn(false);
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.create(command);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product savedProduct = captor.getValue();

        assertThat(savedProduct.getCreatedAt()).isEqualTo(Instant.parse("2026-11-09T12:00:00Z"));
        assertThat(savedProduct.getSku().value()).isEqualTo("SKU-123");
        assertThat(savedProduct.getName().value()).isEqualTo("Test Product");
        assertThat(savedProduct.getPrice().amount()).isEqualByComparingTo("99.99");
        assertThat(savedProduct.getPrice().currency()).isEqualTo(Currency.getInstance("USD"));
        assertThat(savedProduct.getCategoryId().value()).isEqualTo(VALID_CATEGORY_ID);
        assertThat(savedProduct.getAttributes().values()).containsEntry("color", "red");
    }

    @Test
    void create_nullAttributes_savesWithEmptyAttributes() {
        ProductCommand command = new ProductCommand(
                "SKU-123", "Test Product", "Desc", new BigDecimal("10.00"), "USD", VALID_CATEGORY_ID, null
        );

        when(categoryExistenceChecker.exists(any())).thenReturn(true);
        when(productRepository.existsBySku(any())).thenReturn(false);
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.create(command);

        assertThat(result.getAttributes()).isNotNull();
        assertThat(result.getAttributes().values()).isEmpty();
    }

    @Test
    void create_missingCategory_throwsCategoryNotFound() {
        ProductCommand command = new ProductCommand(
                "SKU-123", "Test Product", "Desc", new BigDecimal("10.00"), "USD", VALID_CATEGORY_ID, null
        );

        when(categoryExistenceChecker.exists(any())).thenReturn(false);

        assertThatThrownBy(() -> productService.create(command))
                .isInstanceOf(CategoryReferenceNotFoundException.class);

        verify(categoryExistenceChecker).exists(any());
        verifyNoMoreInteractions(categoryExistenceChecker);
        verifyNoInteractions(productRepository);
    }

    @Test
    void create_existingSku_throwsSkuAlreadyExists() {
        ProductCommand command = new ProductCommand(
                "SKU-123", "Test Product", "Desc", new BigDecimal("10.00"), "USD", VALID_CATEGORY_ID, null
        );

        when(categoryExistenceChecker.exists(any())).thenReturn(true);
        when(productRepository.existsBySku(any())).thenReturn(true);

        assertThatThrownBy(() -> productService.create(command))
                .isInstanceOf(SkuAlreadyExistsException.class);

        verify(productRepository, never()).save(any());
        verify(productRepository).existsBySku(any());
    }

    @Test
    void create_invalidSkuOrBlankName_throwsDomainValidation() {
        ProductCommand invalidSkuCmd = new ProductCommand(
                "", "Test Product", "Desc", new BigDecimal("10.00"), "USD", VALID_CATEGORY_ID, null
        );
        assertThatThrownBy(() -> productService.create(invalidSkuCmd))
                .isInstanceOf(DomainValidationException.class);

        ProductCommand blankNameCmd = new ProductCommand(
                "SKU-123", "   ", "Desc", new BigDecimal("10.00"), "USD", VALID_CATEGORY_ID, null
        );
        assertThatThrownBy(() -> productService.create(blankNameCmd))
                .isInstanceOf(DomainValidationException.class);

        verifyNoInteractions(productRepository, categoryExistenceChecker);
    }

    @Test
    void create_unsupportedOrInvalidCurrency_throwsDomainValidation() {
        ProductCommand eurCmd = new ProductCommand(
                "SKU-123", "Test", "Desc", new BigDecimal("10.00"), "EUR", VALID_CATEGORY_ID, null
        );
        assertThatThrownBy(() -> productService.create(eurCmd)).isInstanceOf(DomainValidationException.class);

        ProductCommand unknownCmd = new ProductCommand(
                "SKU-123", "Test", "Desc", new BigDecimal("10.00"), "XYZ", VALID_CATEGORY_ID, null
        );
        assertThatThrownBy(() -> productService.create(unknownCmd)).isInstanceOf(DomainValidationException.class);

        ProductCommand nullCurrCmd = new ProductCommand(
                "SKU-123", "Test", "Desc", new BigDecimal("10.00"), null, VALID_CATEGORY_ID, null
        );
        assertThatThrownBy(() -> productService.create(nullCurrCmd)).isInstanceOf(DomainValidationException.class);

        verifyNoInteractions(productRepository, categoryExistenceChecker);
    }

    @Test
    void create_negativeAmount_throwsDomainValidation() {
        ProductCommand negativeCmd = new ProductCommand(
                "SKU-123", "Test", "Desc", new BigDecimal("-5.00"), "USD", VALID_CATEGORY_ID, null
        );

        assertThatThrownBy(() -> productService.create(negativeCmd))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void getById_existingId_returnsProduct() {
        Product mockProduct = mock(Product.class);
        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(mockProduct));

        Product result = productService.getById(VALID_PRODUCT_ID);

        assertThat(result).isSameAs(mockProduct);
        verify(productRepository).findById(argThat(id -> id.value().equals(VALID_PRODUCT_ID)));
    }

    @Test
    void getById_missingId_throwsProductNotFoundException() {
        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(VALID_PRODUCT_ID))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void search_nullFiltersAndDefaultPage_passesNullCriteriaAndPageQuery() {
        ProductSearchQuery query = new ProductSearchQuery(null, null, null, 0, 20);
        PageResult<Product> mockResult = mock(PageResult.class);

        ArgumentCaptor<ProductSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(ProductSearchCriteria.class);
        ArgumentCaptor<PageQuery> pageQueryCaptor = ArgumentCaptor.forClass(PageQuery.class);

        when(productRepository.search(any(), any())).thenReturn(mockResult);

        PageResult<Product> result = productService.search(query);

        assertThat(result).isSameAs(mockResult);
        verify(productRepository).search(criteriaCaptor.capture(), pageQueryCaptor.capture());

        ProductSearchCriteria capturedCriteria = criteriaCaptor.getValue();
        assertThat(capturedCriteria.categoryId()).isNull();
        assertThat(capturedCriteria.minPrice()).isNull();
        assertThat(capturedCriteria.maxPrice()).isNull();

        PageQuery capturedPageQuery = pageQueryCaptor.getValue();
        assertThat(capturedPageQuery.page()).isEqualTo(0);
        assertThat(capturedPageQuery.size()).isEqualTo(20);
    }

    @Test
    void search_priceFilters_convertsToMoneyInConfiguredCurrency() {
        ProductSearchQuery query = new ProductSearchQuery(
                null, new BigDecimal("10.00"), new BigDecimal("100.00"), 0, 20
        );
        ArgumentCaptor<ProductSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(ProductSearchCriteria.class);

        when(productRepository.search(any(), any())).thenReturn(mock(PageResult.class));

        productService.search(query);

        verify(productRepository).search(criteriaCaptor.capture(), any());
        ProductSearchCriteria criteria = criteriaCaptor.getValue();

        assertThat(criteria.minPrice()).isEqualTo(
                new Money(new BigDecimal("10.00"), Currency.getInstance("USD"))
        );
        assertThat(criteria.maxPrice()).isEqualTo(
                new Money(new BigDecimal("100.00"), Currency.getInstance("USD"))
        );
    }

    @Test
    void search_validCategoryId_convertsToCategoryId() {
        ProductSearchQuery query = new ProductSearchQuery(VALID_CATEGORY_ID, null, null, 0, 20);
        ArgumentCaptor<ProductSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(ProductSearchCriteria.class);

        when(productRepository.search(any(), any())).thenReturn(mock(PageResult.class));

        productService.search(query);

        verify(productRepository).search(criteriaCaptor.capture(), any());
        assertThat(criteriaCaptor.getValue().categoryId().value()).isEqualTo(VALID_CATEGORY_ID);
    }

    @Test
    void search_validQuery_returnsRepositoryResult() {
        ProductSearchQuery query = new ProductSearchQuery(
                VALID_CATEGORY_ID, new BigDecimal("10.00"), new BigDecimal("50.00"), 1, 10
        );
        PageResult<Product> expectedResult = mock(PageResult.class);

        when(productRepository.search(any(), any())).thenReturn(expectedResult);

        PageResult<Product> actualResult = productService.search(query);

        assertThat(actualResult).isSameAs(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 20",
            "0, 0",
            "0, 101"
    })
    void search_invalidPageOrSize_throwsDomainValidation(int page, int size) {
        ProductSearchQuery query = new ProductSearchQuery(null, null, null, page, size);

        assertThatThrownBy(() -> productService.search(query))
                .isInstanceOf(DomainValidationException.class);

        verifyNoInteractions(productRepository);
    }

    @Test
    void search_minPriceGreaterThanMaxPrice_throwsDomainValidation() {
        ProductSearchQuery query = new ProductSearchQuery(
                null, new BigDecimal("100.00"), new BigDecimal("10.00"), 0, 20
        );

        assertThatThrownBy(() -> productService.search(query))
                .isInstanceOf(DomainValidationException.class);

        verifyNoInteractions(productRepository);
    }

    @Test
    void update_validCommand_updatesAndReturnsProduct() {
        Product existingProduct = Product.create(
                new Sku("OLD-SKU"),
                new ProductName("Old Name"),
                "Old Description",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );

        ProductCommand command = new ProductCommand(
                "NEW-SKU",
                "New Name",
                "New Description",
                new BigDecimal("150.00"),
                "USD",
                VALID_CATEGORY_ID,
                Map.of("size", "XL")
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(existingProduct));
        when(categoryExistenceChecker.exists(any())).thenReturn(true);
        when(productRepository.existsBySkuAndIdNot(any(), any())).thenReturn(false);
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product updatedProduct = productService.update(VALID_PRODUCT_ID, command);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();

        assertThat(saved.getId()).isEqualTo(existingProduct.getId());
        assertThat(saved.getStatus()).isEqualTo(existingProduct.getStatus());
        assertThat(saved.getCreatedAt()).isEqualTo(CREATED_AT_INSTANT);
        assertThat(saved.getUpdatedAt()).isEqualTo(Instant.parse("2026-11-09T12:00:00Z"));
        assertThat(saved.getSku().value()).isEqualTo("NEW-SKU");
        assertThat(saved.getName().value()).isEqualTo("New Name");
        assertThat(saved.getDescription()).isEqualTo("New Description");
        assertThat(saved.getPrice().amount()).isEqualByComparingTo("150.00");
        assertThat(saved.getCategoryId().value()).isEqualTo(VALID_CATEGORY_ID);
        assertThat(saved.getAttributes().values()).containsEntry("size", "XL");
    }

    @Test
    void update_productNotFound_throwsProductNotFoundException() {
        ProductCommand command = new ProductCommand(
                "SKU-123", "Name", "Desc", new BigDecimal("100.00"), "USD", VALID_CATEGORY_ID, null
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(VALID_PRODUCT_ID, command))
                .isInstanceOf(ProductNotFoundException.class);

        verify(categoryExistenceChecker, never()).exists(any());
        verify(productRepository, never()).existsBySkuAndIdNot(any(), any());
    }

    @Test
    void update_missingCategory_throwsCategoryReferenceNotFoundException() {
        Product existingProduct = mock(Product.class);
        when(existingProduct.getId()).thenReturn(new ProductId(VALID_PRODUCT_ID));

        ProductCommand command = new ProductCommand(
                "SKU-123", "Name", "Desc", new BigDecimal("100.00"), "USD", VALID_CATEGORY_ID, null
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(existingProduct));
        when(categoryExistenceChecker.exists(any())).thenReturn(false);

        assertThatThrownBy(() -> productService.update(VALID_PRODUCT_ID, command))
                .isInstanceOf(CategoryReferenceNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void update_skuUsedByAnotherProduct_throwsSkuAlreadyExistsException() {
        Product existingProduct = mock(Product.class);
        ProductId productId = new ProductId(VALID_PRODUCT_ID);
        when(existingProduct.getId()).thenReturn(productId);

        ProductCommand command = new ProductCommand(
                "TAKEN-SKU", "Name", "Desc", new BigDecimal("100.00"), "USD", VALID_CATEGORY_ID, null
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(existingProduct));
        when(categoryExistenceChecker.exists(any())).thenReturn(true);
        when(productRepository.existsBySkuAndIdNot(any(Sku.class), eq(productId))).thenReturn(true);

        assertThatThrownBy(() -> productService.update(VALID_PRODUCT_ID, command))
                .isInstanceOf(SkuAlreadyExistsException.class);

        verify(productRepository, never()).save(any());
        verify(productRepository).existsBySkuAndIdNot(any(Sku.class), eq(productId));
    }

    @Test
    void update_keepingOwnSku_succeeds() {
        Product existingProduct = Product.create(
                new Sku("MY-SKU"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );

        ProductCommand command = new ProductCommand(
                "MY-SKU", "New Name", "Desc", new BigDecimal("100.00"), "USD", VALID_CATEGORY_ID, null
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(existingProduct));
        when(categoryExistenceChecker.exists(any())).thenReturn(true);
        when(productRepository.existsBySkuAndIdNot(any(), any())).thenReturn(false);
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.update(VALID_PRODUCT_ID, command);

        assertThat(result).isNotNull();
        verify(productRepository).save(any());
    }

    @Test
    void update_draftProduct_succeeds() {
        Product draftProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );

        ProductCommand command = new ProductCommand(
                "SKU-123", "New Name", "Desc", new BigDecimal("100.00"), "USD", VALID_CATEGORY_ID, null
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(draftProduct));
        when(categoryExistenceChecker.exists(any())).thenReturn(true);
        when(productRepository.existsBySkuAndIdNot(any(), any())).thenReturn(false);
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.update(VALID_PRODUCT_ID, command);

        assertThat(result).isNotNull();
    }

    @Test
    void update_activeProduct_succeeds() {
        Product activeProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );
        activeProduct.activate(Instant.now());

        ProductCommand command = new ProductCommand(
                "SKU-123", "New Name", "Desc", new BigDecimal("100.00"), "USD", VALID_CATEGORY_ID, null
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(activeProduct));
        when(categoryExistenceChecker.exists(any())).thenReturn(true);
        when(productRepository.existsBySkuAndIdNot(any(), any())).thenReturn(false);
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.update(VALID_PRODUCT_ID, command);

        assertThat(result).isNotNull();
    }

    @Test
    void update_archivedProduct_throwsInvalidProductStateException() {
        Product archivedProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );
        archivedProduct.archive(Instant.now());

        ProductCommand command = new ProductCommand(
                "SKU-123", "New Name", "Desc", new BigDecimal("100.00"), "USD", VALID_CATEGORY_ID, null
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(archivedProduct));
        when(categoryExistenceChecker.exists(any())).thenReturn(true);
        when(productRepository.existsBySkuAndIdNot(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> productService.update(VALID_PRODUCT_ID, command))
                .isInstanceOf(InvalidProductStateException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void update_archivedProductWithNonexistentCategory_throwsInvalidProductStateExceptionAndNeverCallsCategoryChecker() {
        Product archivedProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );
        archivedProduct.archive(Instant.now());

        UUID nonexistentCategoryId = UUID.randomUUID();
        ProductCommand command = new ProductCommand(
                "SKU-123", "New Name", "Desc", new BigDecimal("100.00"), "USD", nonexistentCategoryId, null
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(archivedProduct));

        assertThatThrownBy(() -> productService.update(VALID_PRODUCT_ID, command))
                .isInstanceOf(InvalidProductStateException.class);

        verify(categoryExistenceChecker, never()).exists(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void update_activeProductWithZeroPrice_throwsInvalidProductStateException() {
        Product activeProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );

        activeProduct.activate(Instant.now());

        ProductCommand command = new ProductCommand(
                "SKU-123", "New Name", "Desc", BigDecimal.ZERO, "USD", VALID_CATEGORY_ID, null
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(activeProduct));
        when(categoryExistenceChecker.exists(any())).thenReturn(true);
        when(productRepository.existsBySkuAndIdNot(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> productService.update(VALID_PRODUCT_ID, command))
                .isInstanceOf(InvalidProductStateException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void update_invalidSkuOrCurrencyEur_throwsDomainValidationExceptionWithoutFindById() {
        ProductCommand invalidSkuCommand = new ProductCommand(
                "", "Name", "Desc", new BigDecimal("100.00"), "USD", VALID_CATEGORY_ID, null
        );
        assertThatThrownBy(() -> productService.update(VALID_PRODUCT_ID, invalidSkuCommand))
                .isInstanceOf(DomainValidationException.class);

        ProductCommand eurCurrencyCommand = new ProductCommand(
                "SKU-123", "Name", "Desc", new BigDecimal("100.00"), "EUR", VALID_CATEGORY_ID, null
        );
        assertThatThrownBy(() -> productService.update(VALID_PRODUCT_ID, eurCurrencyCommand))
                .isInstanceOf(DomainValidationException.class);

        verify(productRepository, never()).findById(any());
    }

    @Test
    void update_concurrentUpdate_propagatesConcurrentUpdateException() {
        Product existingProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );

        ProductCommand command = new ProductCommand(
                "SKU-123", "New Name", "Desc", new BigDecimal("100.00"), "USD", VALID_CATEGORY_ID, null
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(existingProduct));
        when(categoryExistenceChecker.exists(any())).thenReturn(true);
        when(productRepository.existsBySkuAndIdNot(any(), any())).thenReturn(false);
        when(productRepository.save(any())).thenThrow(new ConcurrentUpdateException(existingProduct.getId()));

        assertThatThrownBy(() -> productService.update(VALID_PRODUCT_ID, command))
                .isInstanceOf(ConcurrentUpdateException.class);
    }

    @Test
    void activate_draftWithPositivePrice_becomesActiveAndReturnsSaved() {
        Product draftProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(draftProduct));
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.activate(VALID_PRODUCT_ID);

        assertThat(result.getUpdatedAt()).isEqualTo(Instant.parse("2026-11-09T12:00:00Z"));
        assertThat(result).isSameAs(draftProduct);
        verify(productRepository).save(draftProduct);
    }

    @Test
    void activate_notFound_throwsProductNotFoundException() {
        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.activate(VALID_PRODUCT_ID))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void activate_zeroPrice_throwsInvalidProductStateException() {
        Product draftProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(BigDecimal.ZERO, Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(draftProduct));

        assertThatThrownBy(() -> productService.activate(VALID_PRODUCT_ID))
                .isInstanceOf(InvalidProductStateException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void activate_alreadyActive_throwsInvalidProductStateException() {
        Product activeProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );
        activeProduct.activate(Instant.now());

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(activeProduct));

        assertThatThrownBy(() -> productService.activate(VALID_PRODUCT_ID))
                .isInstanceOf(InvalidProductStateException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void activate_alreadyArchived_throwsInvalidProductStateException() {
        Product archivedProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );
        archivedProduct.archive(Instant.now());

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(archivedProduct));

        assertThatThrownBy(() -> productService.activate(VALID_PRODUCT_ID))
                .isInstanceOf(InvalidProductStateException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void activate_saveConcurrentUpdate_propagatesConcurrentUpdateException() {
        Product draftProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(draftProduct));
        when(productRepository.save(any())).thenThrow(new ConcurrentUpdateException(draftProduct.getId()));

        assertThatThrownBy(() -> productService.activate(VALID_PRODUCT_ID))
                .isInstanceOf(ConcurrentUpdateException.class);
    }

    @Test
    void archive_draftProduct_succeeds() {
        Product draftProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(draftProduct));
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.archive(VALID_PRODUCT_ID);

        assertThat(result).isNotNull();
        verify(productRepository).save(draftProduct);
    }

    @Test
    void archive_activeProduct_succeeds() {
        Product activeProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );
        activeProduct.activate(Instant.now());

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(activeProduct));
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.archive(VALID_PRODUCT_ID);

        assertThat(result).isNotNull();
        verify(productRepository).save(activeProduct);
    }

    @Test
    void archive_alreadyArchived_throwsInvalidProductStateException() {
        Product archivedProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );
        archivedProduct.archive(Instant.now());

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(archivedProduct));

        assertThatThrownBy(() -> productService.archive(VALID_PRODUCT_ID))
                .isInstanceOf(InvalidProductStateException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void archive_notFound_throwsProductNotFoundException() {
        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.archive(VALID_PRODUCT_ID))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void delete_draftProduct_deletesById() {
        Product draftProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(draftProduct));

        productService.delete(VALID_PRODUCT_ID);

        verify(productRepository).delete(draftProduct);
    }

    @Test
    void delete_activeProduct_throwsInvalidProductStateException() {
        Product activeProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );
        activeProduct.activate(Instant.now());

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(activeProduct));

        assertThatThrownBy(() -> productService.delete(VALID_PRODUCT_ID))
                .isInstanceOf(InvalidProductStateException.class);

        verify(productRepository, never()).delete(any());
    }

    @Test
    void delete_archivedProduct_throwsInvalidProductStateException() {
        Product archivedProduct = Product.create(
                new Sku("SKU-123"),
                new ProductName("Name"),
                "Desc",
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD")),
                new CategoryId(VALID_CATEGORY_ID),
                new Attributes(Map.of()),
                CREATED_AT_INSTANT
        );
        archivedProduct.archive(Instant.now());

        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(archivedProduct));

        assertThatThrownBy(() -> productService.delete(VALID_PRODUCT_ID))
                .isInstanceOf(InvalidProductStateException.class);

        verify(productRepository, never()).delete(any());
    }

    @Test
    void delete_notFound_throwsProductNotFoundException() {
        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(VALID_PRODUCT_ID))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).delete(any());
    }
}