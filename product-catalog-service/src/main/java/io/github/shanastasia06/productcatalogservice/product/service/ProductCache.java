package io.github.shanastasia06.productcatalogservice.product.service;

import io.github.shanastasia06.productcatalogservice.product.domain.Product;
import io.github.shanastasia06.productcatalogservice.product.domain.ProductId;

import java.util.Optional;

public interface ProductCache {
    Optional<Product> get(ProductId id);
    void put(Product product);
    void evict(ProductId id);
}
