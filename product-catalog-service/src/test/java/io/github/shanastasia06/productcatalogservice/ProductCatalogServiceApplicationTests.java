package io.github.shanastasia06.productcatalogservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ProductCatalogServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
