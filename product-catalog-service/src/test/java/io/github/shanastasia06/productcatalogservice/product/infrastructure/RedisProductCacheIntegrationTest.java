package io.github.shanastasia06.productcatalogservice.product.infrastructure;

import io.github.shanastasia06.productcatalogservice.category.domain.CategoryId;
import io.github.shanastasia06.productcatalogservice.product.domain.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class RedisProductCacheIntegrationTest {

    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    static {
        REDIS.start();
    }

    private LettuceConnectionFactory connectionFactory;
    private StringRedisTemplate template;
    private RedisProductCache cache;

    @BeforeEach
    void setUp() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();

        template = new StringRedisTemplate(connectionFactory);
        cache = new RedisProductCache(
                template,
                JsonMapper.builder().build(),
                Duration.ofMinutes(10),
                0.2
        );
    }

    @AfterEach
    void tearDown() {
        connectionFactory.destroy();
    }

    @Test
    void putThenGet_returnsProductWithAllFieldsPreserved() {
        Product original = randomProduct();

        cache.put(original);
        Optional<Product> result = cache.get(original.getId());

        assertTrue(result.isPresent());
        Product cached = result.get();
        assertEquals(original.getId(), cached.getId());
        assertEquals(original.getSku(), cached.getSku());
        assertEquals(original.getName(), cached.getName());
        assertEquals(original.getDescription(), cached.getDescription());
        assertEquals(original.getPrice(), cached.getPrice());
        assertEquals(original.getCategoryId(), cached.getCategoryId());
        assertEquals(original.getAttributes(), cached.getAttributes());
        assertEquals(original.getCreatedAt(), cached.getCreatedAt());
    }

    @Test
    void get_unknownId_returnsEmpty() {
        ProductId unknownId = new ProductId(UUID.randomUUID());

        Optional<Product> result = cache.get(unknownId);

        assertTrue(result.isEmpty());
    }

    @Test
    void putThenEvictThenGet_returnsEmpty() {
        Product product = randomProduct();
        cache.put(product);

        cache.evict(product.getId());

        assertTrue(cache.get(product.getId()).isEmpty());
    }

    @Test
    void put_setsTtlWithJitterInRedis() {
        Product product = randomProduct();
        String expectedKey = "catalog:product:v1:" + product.getId().value();

        cache.put(product);

        Long seconds = template.getExpire(expectedKey, TimeUnit.SECONDS);
        assertNotNull(seconds);
        assertTrue(seconds >= 599 && seconds <= 720, "Expected TTL between 600 and 720s, got: " + seconds);
    }

    @Test
    void get_badOrIncompatibleData_returnsEmptyWithoutThrowing() {
        ProductId corruptId = new ProductId(UUID.randomUUID());
        String corruptKey = "catalog:product:v1:" + corruptId.value();
        template.opsForValue().set(corruptKey, "{not json");

        assertEquals(Optional.empty(), cache.get(corruptId));

        ProductId incompatibleId = new ProductId(UUID.randomUUID());
        String incompatibleKey = "catalog:product:v1:" + incompatibleId.value();
        template.opsForValue().set(incompatibleKey, "{\"id\":\"x\"}");

        assertEquals(Optional.empty(), cache.get(incompatibleId));
    }

    @Test
    void operations_whenRedisIsDown_failOpenWithoutThrowing() {
        var deadFactory = new LettuceConnectionFactory("localhost", 1);
        deadFactory.afterPropertiesSet();
        try {
            var deadTemplate = new StringRedisTemplate(deadFactory);
            var downCache = new RedisProductCache(
                    deadTemplate,
                    JsonMapper.builder().build(),
                    Duration.ofMinutes(10),
                    0.2
            );
            Product product = randomProduct();

            assertEquals(Optional.empty(), downCache.get(product.getId()));
            assertDoesNotThrow(() -> downCache.put(product));
            assertDoesNotThrow(() -> downCache.evict(product.getId()));
        } finally {
            deadFactory.destroy();
        }
    }

    private Product randomProduct() {
        return Product.create(
                new Sku("PROD-DRAFT"),
                new ProductName("Headphones"),
                "Wireless headphones",
                new Money(new BigDecimal("299.99"), Currency.getInstance("USD")),
                CategoryId.generate(),
                Attributes.empty(),
                Instant.now().truncatedTo(ChronoUnit.MILLIS)
        );
    }
}
