package io.github.shanastasia06.productcatalogservice.product.service;

import io.github.shanastasia06.productcatalogservice.common.domain.PageQuery;
import io.github.shanastasia06.productcatalogservice.common.domain.PageResult;
import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import io.github.shanastasia06.productcatalogservice.product.domain.*;
import io.github.shanastasia06.productcatalogservice.product.exception.CategoryReferenceNotFoundException;
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
        CreateProductCommand command = new CreateProductCommand(
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
        CreateProductCommand command = new CreateProductCommand(
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
        CreateProductCommand command = new CreateProductCommand(
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
        CreateProductCommand command = new CreateProductCommand(
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
        CreateProductCommand invalidSkuCmd = new CreateProductCommand(
                "", "Test Product", "Desc", new BigDecimal("10.00"), "USD", VALID_CATEGORY_ID, null
        );
        assertThatThrownBy(() -> productService.create(invalidSkuCmd))
                .isInstanceOf(DomainValidationException.class);

        CreateProductCommand blankNameCmd = new CreateProductCommand(
                "SKU-123", "   ", "Desc", new BigDecimal("10.00"), "USD", VALID_CATEGORY_ID, null
        );
        assertThatThrownBy(() -> productService.create(blankNameCmd))
                .isInstanceOf(DomainValidationException.class);

        verifyNoInteractions(productRepository, categoryExistenceChecker);
    }

    @Test
    void create_unsupportedOrInvalidCurrency_throwsDomainValidation() {
        CreateProductCommand eurCmd = new CreateProductCommand(
                "SKU-123", "Test", "Desc", new BigDecimal("10.00"), "EUR", VALID_CATEGORY_ID, null
        );
        assertThatThrownBy(() -> productService.create(eurCmd)).isInstanceOf(DomainValidationException.class);

        CreateProductCommand unknownCmd = new CreateProductCommand(
                "SKU-123", "Test", "Desc", new BigDecimal("10.00"), "XYZ", VALID_CATEGORY_ID, null
        );
        assertThatThrownBy(() -> productService.create(unknownCmd)).isInstanceOf(DomainValidationException.class);

        CreateProductCommand nullCurrCmd = new CreateProductCommand(
                "SKU-123", "Test", "Desc", new BigDecimal("10.00"), null, VALID_CATEGORY_ID, null
        );
        assertThatThrownBy(() -> productService.create(nullCurrCmd)).isInstanceOf(DomainValidationException.class);

        verifyNoInteractions(productRepository, categoryExistenceChecker);
    }

    @Test
    void create_negativeAmount_throwsDomainValidation() {
        CreateProductCommand negativeCmd = new CreateProductCommand(
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
}