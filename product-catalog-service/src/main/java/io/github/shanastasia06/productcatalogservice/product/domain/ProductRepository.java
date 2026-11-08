package io.github.shanastasia06.productcatalogservice.product.domain;

import io.github.shanastasia06.productcatalogservice.common.domain.PageQuery;
import io.github.shanastasia06.productcatalogservice.common.domain.PageResult;

import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(ProductId id);
    boolean existsBySku(Sku sku);
    boolean existsBySkuAndIdNot(Sku sku, ProductId id);
    void deleteById(ProductId id);
    PageResult<Product> search(ProductSearchCriteria criteria, PageQuery pageQuery);
}
