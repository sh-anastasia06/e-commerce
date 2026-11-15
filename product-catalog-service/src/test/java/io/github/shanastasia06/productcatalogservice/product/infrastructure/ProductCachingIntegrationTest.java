package io.github.shanastasia06.productcatalogservice.product.infrastructure;

import io.github.shanastasia06.productcatalogservice.TestcontainersConfiguration;
import io.github.shanastasia06.productcatalogservice.category.api.CategoryRequest;
import io.github.shanastasia06.productcatalogservice.product.api.MoneyRequest;
import io.github.shanastasia06.productcatalogservice.product.api.ProductRequest;
import io.github.shanastasia06.productcatalogservice.product.domain.ProductId;
import io.github.shanastasia06.productcatalogservice.product.domain.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class ProductCachingIntegrationTest {
    private static final String KEY_PREFIX = "catalog:product:v1:";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoSpyBean
    private ProductRepository repository;

    @Autowired
    private StringRedisTemplate redis;

    @Test
    void secondGet_comesFromCache() throws Exception {
        UUID categoryId = createCategory();
        String sku = uniqueSku();
        UUID productId = createProduct(categoryId, sku, "Wireless Headphones");

        clearInvocations(repository);

        String firstResponse = mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secondResponse = mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(secondResponse).isEqualTo(firstResponse);
        verify(repository, times(1)).findById(new ProductId(productId));
        assertThat(redis.hasKey(KEY_PREFIX + productId)).isTrue();
    }

    @Test
    void update_evictsCacheAndSubsequentGetReturnsNewName() throws Exception {
        UUID categoryId = createCategory();
        String sku = uniqueSku();
        UUID productId = createProduct(categoryId, sku, "Original Name");

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk());
        assertThat(redis.hasKey(KEY_PREFIX + productId)).isTrue();

        String updatePayload = jsonMapper.writeValueAsString(new ProductRequest(
                sku,
                "Updated Name",
                "Updated description",
                new MoneyRequest("199.99", "USD"),
                categoryId,
                Map.of("color", "black")
        ));

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk());

        assertThat(redis.hasKey(KEY_PREFIX + productId)).isFalse();

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void delete_evictsCacheAndSubsequentGetReturnsNotFound() throws Exception {
        UUID categoryId = createCategory();
        String sku = uniqueSku();
        UUID productId = createProduct(categoryId, sku, "Disposable Product");

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk());
        assertThat(redis.hasKey(KEY_PREFIX + productId)).isTrue();

        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isNoContent());

        assertThat(redis.hasKey(KEY_PREFIX + productId)).isFalse();

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("PRODUCT_NOT_FOUND")));
    }

    private String uniqueSku() {
        return "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private UUID createCategory() throws Exception {
        String suffix = UUID.randomUUID().toString();
        CategoryRequest request = new CategoryRequest(
                "Category " + suffix,
                "cat-" + suffix,
                "Test category description"
        );

        String response = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = jsonMapper.readTree(response);
        return UUID.fromString(node.get("id").asText());
    }

    private UUID createProduct(UUID categoryId, String sku, String name) throws Exception {
        String payload = jsonMapper.writeValueAsString(new ProductRequest(
                sku,
                name,
                "Test description",
                new MoneyRequest("149.99", "USD"),
                categoryId,
                Map.of("color", "silver")
        ));
        String response = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = jsonMapper.readTree(response);
        return UUID.fromString(node.get("id").asText());
    }
}
