package io.github.shanastasia06.productcatalogservice.product.infrastructure;

import io.github.shanastasia06.productcatalogservice.product.domain.Product;
import io.github.shanastasia06.productcatalogservice.product.domain.ProductId;
import io.github.shanastasia06.productcatalogservice.product.service.ProductCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class RedisProductCache implements ProductCache {

    private static final String KEY_PREFIX = "catalog:product:v1:";

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;
    private final Duration baseTtl;
    private final double jitter;

    public RedisProductCache(StringRedisTemplate redisTemplate, JsonMapper jsonMapper,
                             @Value("${catalog.cache.product-ttl}") Duration baseTtl,
                             @Value("${catalog.cache.ttl-jitter}") double jitter) {
        if (baseTtl == null || baseTtl.isNegative() || baseTtl.isZero()) {
            throw new IllegalArgumentException("catalog.cache.product-ttl must be positive");
        }
        if (Double.isNaN(jitter) || jitter < 0.0 || jitter > 1.0) {
            throw new IllegalArgumentException("catalog.cache.ttl-jitter must be between 0 and 1");
        }
        this.redisTemplate = redisTemplate;
        this.jsonMapper = jsonMapper;
        this.baseTtl = baseTtl;
        this.jitter = jitter;
    }

    @Override
    public Optional<Product> get(ProductId id) {
        try {
            String json = redisTemplate.opsForValue().get(key(id));
            if (json == null) {
                return Optional.empty();
            }
            CachedProduct cachedProduct = jsonMapper.readValue(json, CachedProduct.class);
            return Optional.of(cachedProduct.toDomain());
        } catch (RuntimeException e) {
            log.warn("Cache get failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(Product product) {
        try {
            CachedProduct cached = CachedProduct.from(product);
            String json = jsonMapper.writeValueAsString(cached);
            Duration ttl = ttlWithJitter(baseTtl, jitter, ThreadLocalRandom.current().nextDouble());
            redisTemplate.opsForValue().set(key(product.getId()), json, ttl);
        } catch (RuntimeException e) {
            log.warn("Cache put failed: {}", e.getMessage());
        }
    }

    @Override
    public void evict(ProductId id) {
        try {
            redisTemplate.delete(key(id));
        } catch (RuntimeException e) {
            log.warn("Cache evict failed: {}", e.getMessage());
        }
    }

    private String key(ProductId id) {
        return KEY_PREFIX + id.value();
    }

    static Duration ttlWithJitter(Duration base, double jitter, double random) {
        long extraMillis = (long) (base.toMillis() * jitter * random);
        return base.plusMillis(extraMillis);
    }
}
