package io.github.shanastasia06.productcatalogservice.product.infrastructure;

import io.github.shanastasia06.productcatalogservice.common.domain.PageQuery;
import io.github.shanastasia06.productcatalogservice.common.domain.PageResult;
import io.github.shanastasia06.productcatalogservice.common.exception.ConcurrentUpdateException;
import io.github.shanastasia06.productcatalogservice.product.domain.*;
import io.github.shanastasia06.productcatalogservice.product.exception.SkuAlreadyExistsException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
class MongoProductRepository implements ProductRepository {
    private final SpringDataProductRepository springDataRepository;
    private final ProductMapper mapper;
    private final MongoTemplate mongoTemplate;

    public MongoProductRepository(SpringDataProductRepository springDataRepository, ProductMapper mapper, MongoTemplate mongoTemplate) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Product save(Product product) {
        try {
            ProductDocument saved = springDataRepository.save(mapper.toDocument(product));
            return mapper.toDomain(saved);
        } catch (DuplicateKeyException e) {
            throw new SkuAlreadyExistsException(product.getSku());
        } catch (OptimisticLockingFailureException e) {
            throw new ConcurrentUpdateException(product.getId());
        }
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return springDataRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsBySku(Sku sku) {
        return springDataRepository.existsBySku(sku.value());
    }

    @Override
    public boolean existsBySkuAndIdNot(Sku sku, ProductId id) {
        return springDataRepository.existsBySkuAndIdNot(sku.value(), id.value());
    }

    @Override
    public void deleteById(ProductId id) {
        springDataRepository.deleteById(id.value());
    }

    // optimistic lock on delete
    @Override
    public void delete(Product product) {
        try {
            springDataRepository.delete(mapper.toDocument(product));
        } catch (OptimisticLockingFailureException e) {
            throw new ConcurrentUpdateException(product.getId());
        }
    }

    @Override
    public PageResult<Product> search(ProductSearchCriteria criteria, PageQuery pageQuery) {
        Query filter = buildFilter(criteria);

        // total number of matching docs
        long total = mongoTemplate.count(filter, ProductDocument.class);

        // id is a tie-break: makes sure that products with equal createdAt will be sorted
        Query pageQueryMongo = Query.of(filter)
                .with(Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.ASC, "id")))
                .skip((long) pageQuery.page() * pageQuery.size())
                .limit(pageQuery.size());

        List<Product> content = mongoTemplate.find(pageQueryMongo, ProductDocument.class).stream()
                .map(mapper::toDomain)
                .toList();

        return new PageResult<>(content, pageQuery.page(), pageQuery.size(), total);
    }

    // assembles only present conditions
    private Query buildFilter(ProductSearchCriteria criteria) {
        List<Criteria> conditions = new ArrayList<>();

        if (criteria.categoryId() != null) {
            conditions.add(Criteria.where("categoryId").is(criteria.categoryId().value()));
        }
        if (criteria.minPrice() != null) {
            conditions.add(Criteria.where("priceAmount").gte(criteria.minPrice().amount()));
        }
        if (criteria.maxPrice() != null) {
            conditions.add(Criteria.where("priceAmount").lte(criteria.maxPrice().amount()));
        }

        Query query = new Query();
        if (!conditions.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(conditions));
        }

        return query;
    }
}
