package io.github.shanastasia06.productcatalogservice.product.service;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.common.domain.PageQuery;
import io.github.shanastasia06.productcatalogservice.common.domain.PageResult;
import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import io.github.shanastasia06.productcatalogservice.product.domain.*;
import io.github.shanastasia06.productcatalogservice.product.exception.CategoryReferenceNotFoundException;
import io.github.shanastasia06.productcatalogservice.product.exception.ProductNotFoundException;
import io.github.shanastasia06.productcatalogservice.product.exception.SkuAlreadyExistsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryExistenceChecker categoryExistenceChecker;
    private final Clock clock;
    private final Currency allowedCurrency;

    public ProductService(ProductRepository productRepository, CategoryExistenceChecker categoryExistenceChecker,
                          Clock clock, @Value("${catalog.currency}") Currency allowedCurrency) {
        this.productRepository = productRepository;
        this.categoryExistenceChecker = categoryExistenceChecker;
        this.clock = clock;
        this.allowedCurrency = allowedCurrency;
    }

    public Product create(ProductCommand command) {
        Sku sku = new Sku(command.sku());
        ProductName name = new ProductName(command.name());
        Money price = toMoney(command.priceAmount(), command.priceCurrency());
        CategoryId categoryId = new CategoryId(command.categoryId());
        Attributes attributes = new Attributes(command.attributes());

        if (!categoryExistenceChecker.exists(categoryId)) {
            throw new CategoryReferenceNotFoundException(categoryId);
        }
        if (productRepository.existsBySku(sku)) {
            throw new SkuAlreadyExistsException(sku);
        }

        Product product = Product.create(
                sku, name, command.description(), price, categoryId, attributes, Instant.now(clock)
        );
        return productRepository.save(product);
    }

    public Product getById(UUID id) {
        return findOrThrow(new ProductId(id));
    }

    public PageResult<Product> search(ProductSearchQuery query) {
        PageQuery pageQuery = new PageQuery(query.page(), query.size());

        Money minPrice = query.minPrice() != null ? new Money(query.minPrice(), allowedCurrency) : null;
        Money maxPrice = query.maxPrice() != null ? new Money(query.maxPrice(), allowedCurrency) : null;

        CategoryId categoryId = query.categoryId() != null ? new CategoryId(query.categoryId()) : null;

        ProductSearchCriteria criteria = new ProductSearchCriteria(categoryId, minPrice, maxPrice);

        return productRepository.search(criteria, pageQuery);
    }

    public Product update(UUID id, ProductCommand command) {
        Sku sku = new Sku(command.sku());
        ProductName name = new ProductName(command.name());
        Money price = toMoney(command.priceAmount(), command.priceCurrency());
        CategoryId categoryId = new CategoryId(command.categoryId());
        Attributes attributes = new Attributes(command.attributes());
        String description = command.description().strip();

        Product product = findOrThrow(new ProductId(id));
        product.ensureCanBeEdited();

        if (!categoryExistenceChecker.exists(categoryId)) {
            throw new CategoryReferenceNotFoundException(categoryId);
        }
        if (productRepository.existsBySkuAndIdNot(sku, product.getId())) {
            throw new SkuAlreadyExistsException(sku);
        }

        product.update(sku, name, description, price, categoryId, attributes, Instant.now(clock));
        return productRepository.save(product);
    }

    public Product activate(UUID id) {
        Product product = findOrThrow(new ProductId(id));
        product.activate(Instant.now(clock));
        return productRepository.save(product);
    }

    public Product archive(UUID id) {
        Product product = findOrThrow(new ProductId(id));
        product.archive(Instant.now(clock));
        return productRepository.save(product);
    }

    public void delete(UUID id) {
        Product product = findOrThrow(new ProductId(id));
        product.ensureCanBeDeleted();
        productRepository.delete(product);
    }

    private Money toMoney(BigDecimal amount, String currencyCode) {
        if (currencyCode == null) {
            throw new DomainValidationException("Currency code must not be null");
        }
        Currency currency;
        try {
            currency = Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            throw new DomainValidationException("Invalid currency code '" + currencyCode + "'");
        }

        if (!currency.equals(allowedCurrency)) {
            throw new DomainValidationException("Only '" + allowedCurrency + "' allowed currencies are supported");
        }
        return new Money(amount, currency);
    }

    private Product findOrThrow(ProductId id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
