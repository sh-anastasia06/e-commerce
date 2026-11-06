package io.github.shanastasia06.productcatalogservice.category.infrastructure;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface SpringDataCategoryRepository extends MongoRepository<CategoryDocument, UUID> {
    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);
}
