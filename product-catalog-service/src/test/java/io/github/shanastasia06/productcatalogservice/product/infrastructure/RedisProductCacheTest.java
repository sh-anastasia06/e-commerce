package io.github.shanastasia06.productcatalogservice.product.infrastructure;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisProductCacheTest {
    private static final Duration BASE_TTL = Duration.ofMinutes(10);
    private static final double JITTER = 0.2;

    @Test
    void ttlWithJitter_zeroRandom_returnsExactBase() {
        Duration result = RedisProductCache.ttlWithJitter(BASE_TTL, JITTER, 0.0);

        assertEquals(BASE_TTL, result);
    }

    @Test
    void ttlWithJitter_halfRandom_addsHalfOfMaxJitter() {
        Duration result = RedisProductCache.ttlWithJitter(BASE_TTL, JITTER, 0.5);

        assertEquals(Duration.ofMinutes(11), result);
    }

    @Test
    void ttlWithJitter_nearOneRandom_staysBelowUpperBound() {
        Duration maxTtl = Duration.ofMinutes(12);

        Duration result = RedisProductCache.ttlWithJitter(BASE_TTL, JITTER, 0.999999);

        assertTrue(result.compareTo(maxTtl) < 0);
    }
}