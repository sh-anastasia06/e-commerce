package io.github.shanastasia06.productcatalogservice.product.infrastructure;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface SpringDataProductRepository extends MongoRepository<ProductDocument, UUID> {
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, UUID id);
    boolean existsByCategoryId(UUID id);
}
