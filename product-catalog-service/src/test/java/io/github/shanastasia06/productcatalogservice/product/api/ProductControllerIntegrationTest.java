package io.github.shanastasia06.productcatalogservice.product.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.contains;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;

import io.github.shanastasia06.productcatalogservice.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/*
 * manual setup to add filters in the future
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ProductControllerIntegrationTest {

    private static final String PRODUCTS = "/api/v1/products";
    private static final String CATEGORIES = "/api/v1/categories";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MongoTemplate mongoTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        // remove documents but keep collections
        mongoTemplate.getCollectionNames()
                .forEach(name -> mongoTemplate.remove(new Query(), name));
    }

    @Nested
    @DisplayName("Create")
    class Create {

        @Test
        @DisplayName("returns 201 with Location header and DRAFT body")
        void created() throws Exception {
            UUID categoryId = createCategory("Electronics");
            String body = Body.valid(categoryId)
                    .withAttributes("{\"color\":\"black\",\"weight\":2}")
                    .json();

            MvcResult result = mockMvc.perform(post(PRODUCTS).contentType(APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION))
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.sku").value("LAPTOP-001"))
                    .andExpect(jsonPath("$.name").value("Laptop"))
                    .andExpect(jsonPath("$.price.amount").value("799.99"))
                    .andExpect(jsonPath("$.price.currency").value("USD"))
                    .andExpect(jsonPath("$.categoryId").value(categoryId.toString()))
                    .andExpect(jsonPath("$.attributes.color").value("black"))
                    .andExpect(jsonPath("$.createdAt", instanceOf(String.class)))
                    .andExpect(jsonPath("$.updatedAt", instanceOf(String.class)))
                    .andReturn();

            String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
            assertThat(result.getResponse().getHeader(HttpHeaders.LOCATION))
                    .endsWith(PRODUCTS + "/" + id);
        }

        @Test
        @DisplayName("accepts a zero price")
        void zeroPriceAccepted() throws Exception {
            UUID categoryId = createCategory("Electronics");

            postProduct(Body.valid(categoryId).withAmount("0.00"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.price.amount").value("0.00"));
        }

        @Test
        @DisplayName("missing attributes become an empty object")
        void missingAttributes() throws Exception {
            UUID categoryId = createCategory("Electronics");

            postProduct(Body.valid(categoryId))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.attributes", anEmptyMap()));
        }

        @Test
        @DisplayName("duplicate SKU gives 409 SKU_ALREADY_EXISTS")
        void duplicateSku() throws Exception {
            UUID categoryId = createCategory("Electronics");
            createProduct(Body.valid(categoryId));

            postProduct(Body.valid(categoryId))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("SKU_ALREADY_EXISTS"));
        }

        @Test
        @DisplayName("unknown category gives 422 CATEGORY_NOT_FOUND")
        void unknownCategory() throws Exception {
            postProduct(Body.valid(UUID.randomUUID()))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
        }

        @Test
        @DisplayName("invalid body gives 400 with details for price.amount, sku and name")
        void invalidBody() throws Exception {
            UUID categoryId = createCategory("Electronics");
            Body invalid = Body.valid(categoryId)
                    .withAmount("abc")        // bad amount
                    .withSku("lower-case")    // lowercase SKU
                    .withName("   ");         // blank name

            postProduct(invalid)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(content().string(allOf(
                            containsString("price.amount"),
                            containsString("sku"),
                            containsString("name"))));
        }

        @Test
        @DisplayName("unsupported currency (EUR) gives 400")
        void unsupportedCurrency() throws Exception {
            UUID categoryId = createCategory("Electronics");

            postProduct(Body.valid(categoryId).withCurrency("EUR"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("broken JSON gives 400")
        void brokenJson() throws Exception {
            mockMvc.perform(post(PRODUCTS).contentType(APPLICATION_JSON).content("{\"sku\": "))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Read")
    class Read {

        @Test
        @DisplayName("GET by id gives 200 and the right body")
        void getById() throws Exception {
            UUID categoryId = createCategory("Electronics");
            UUID id = createProduct(Body.valid(categoryId));

            mockMvc.perform(get(PRODUCTS + "/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.sku").value("LAPTOP-001"))
                    .andExpect(jsonPath("$.name").value("Laptop"))
                    .andExpect(jsonPath("$.price.amount").value("799.99"))
                    .andExpect(jsonPath("$.categoryId").value(categoryId.toString()))
                    .andExpect(jsonPath("$.status").value("DRAFT"));
        }

        @Test
        @DisplayName("unknown id gives 404 PRODUCT_NOT_FOUND")
        void unknownId() throws Exception {
            mockMvc.perform(get(PRODUCTS + "/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("malformed id ('abc') gives 400")
        void malformedId() throws Exception {
            mockMvc.perform(get(PRODUCTS + "/abc"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("List")
    class ListProducts {

        private UUID categoryA;
        private UUID categoryB;

        // creates (oldest to newest)
        @BeforeEach
        void fixtures() throws Exception {
            categoryA = createCategory("Category A");
            categoryB = createCategory("Category B");
            createProduct(Body.valid(categoryA).withSku("P-A-10").withAmount("10.00"));
            pause();
            createProduct(Body.valid(categoryA).withSku("P-A-50").withAmount("50.00"));
            pause();
            createProduct(Body.valid(categoryB).withSku("P-B-100").withAmount("100.00"));
        }

        @Test
        @DisplayName("no filters: default page, newest first")
        void noFilters() throws Exception {
            mockMvc.perform(get(PRODUCTS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.content[*].sku", contains("P-B-100", "P-A-50", "P-A-10")));
        }

        @Test
        @DisplayName("filter by category")
        void byCategory() throws Exception {
            mockMvc.perform(get(PRODUCTS).param("categoryId", categoryA.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].sku", contains("P-A-50", "P-A-10")));
        }

        @Test
        @DisplayName("filter by minPrice")
        void byMinPrice() throws Exception {
            mockMvc.perform(get(PRODUCTS).param("minPrice", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].sku", contains("P-B-100", "P-A-50")));
        }

        @Test
        @DisplayName("filter by maxPrice")
        void byMaxPrice() throws Exception {
            mockMvc.perform(get(PRODUCTS).param("maxPrice", "60"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].sku", contains("P-A-50", "P-A-10")));
        }

        @Test
        @DisplayName("combined filters")
        void combined() throws Exception {
            mockMvc.perform(get(PRODUCTS)
                            .param("categoryId", categoryA.toString())
                            .param("minPrice", "20")
                            .param("maxPrice", "60"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].sku").value("P-A-50"));
        }

        @Test
        @DisplayName("minPrice greater than maxPrice gives 400")
        void minGreaterThanMax() throws Exception {
            mockMvc.perform(get(PRODUCTS).param("minPrice", "100").param("maxPrice", "10"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("paging fields: totalElements and totalPages")
        void paging() throws Exception {
            mockMvc.perform(get(PRODUCTS).param("page", "0").param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.content", hasSize(2)));

            mockMvc.perform(get(PRODUCTS).param("page", "1").param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(1))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].sku").value("P-A-10"));
        }

        @Test
        @DisplayName("size=101 gives 400")
        void sizeTooLarge() throws Exception {
            mockMvc.perform(get(PRODUCTS).param("size", "101"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("minPrice=abc gives 400")
        void minPriceNotANumber() throws Exception {
            mockMvc.perform(get(PRODUCTS).param("minPrice", "abc"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Update")
    class Update {

        @Test
        @DisplayName("gives 200 with new values")
        void updates() throws Exception {
            UUID categoryId = createCategory("Electronics");
            UUID otherCategoryId = createCategory("Computers");
            UUID id = createProduct(Body.valid(categoryId));

            Body changed = Body.valid(otherCategoryId)
                    .withSku("LAPTOP-002")
                    .withName("Laptop Pro")
                    .withAmount("999.50");

            putProduct(id, changed)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.sku").value("LAPTOP-002"))
                    .andExpect(jsonPath("$.name").value("Laptop Pro"))
                    .andExpect(jsonPath("$.price.amount").value("999.50"))
                    .andExpect(jsonPath("$.categoryId").value(otherCategoryId.toString()));
        }

        @Test
        @DisplayName("the same SKU is allowed on the same product")
        void sameSkuOnSameProduct() throws Exception {
            UUID categoryId = createCategory("Electronics");
            UUID id = createProduct(Body.valid(categoryId));

            putProduct(id, Body.valid(categoryId).withName("Renamed"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sku").value("LAPTOP-001"))
                    .andExpect(jsonPath("$.name").value("Renamed"));
        }

        @Test
        @DisplayName("another product's SKU gives 409 SKU_ALREADY_EXISTS")
        void anotherProductsSku() throws Exception {
            UUID categoryId = createCategory("Electronics");
            createProduct(Body.valid(categoryId).withSku("FIRST-001"));
            UUID second = createProduct(Body.valid(categoryId).withSku("SECOND-001"));

            putProduct(second, Body.valid(categoryId).withSku("FIRST-001"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("SKU_ALREADY_EXISTS"));
        }

        @Test
        @DisplayName("archived product gives 409 INVALID_PRODUCT_STATE")
        void archivedProduct() throws Exception {
            UUID categoryId = createCategory("Electronics");
            UUID id = createProduct(Body.valid(categoryId));
            activate(id).andExpect(status().isOk());
            archive(id).andExpect(status().isOk());

            putProduct(id, Body.valid(categoryId).withName("Too late"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_STATE"));
        }

        @Test
        @DisplayName("ACTIVE product with price 0.00 gives 409")
        void activeWithZeroPrice() throws Exception {
            UUID categoryId = createCategory("Electronics");
            UUID id = createProduct(Body.valid(categoryId));
            activate(id).andExpect(status().isOk());

            putProduct(id, Body.valid(categoryId).withAmount("0.00"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("unknown category gives 422")
        void unknownCategory() throws Exception {
            UUID categoryId = createCategory("Electronics");
            UUID id = createProduct(Body.valid(categoryId));

            putProduct(id, Body.valid(UUID.randomUUID()))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("Lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("activate then archive (happy path)")
        void activateThenArchive() throws Exception {
            UUID categoryId = createCategory("Electronics");
            UUID id = createProduct(Body.valid(categoryId));

            activate(id)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            archive(id)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ARCHIVED"));
        }

        @Test
        @DisplayName("activate with price 0.00 gives 409")
        void activateWithZeroPrice() throws Exception {
            UUID categoryId = createCategory("Electronics");
            UUID id = createProduct(Body.valid(categoryId).withAmount("0.00"));

            activate(id).andExpect(status().isConflict());
        }

        @Test
        @DisplayName("activate twice gives 409")
        void activateTwice() throws Exception {
            UUID categoryId = createCategory("Electronics");
            UUID id = createProduct(Body.valid(categoryId));
            activate(id).andExpect(status().isOk());

            activate(id).andExpect(status().isConflict());
        }

        @Test
        @DisplayName("delete DRAFT gives 204, and then 404")
        void deleteDraft() throws Exception {
            UUID categoryId = createCategory("Electronics");
            UUID id = createProduct(Body.valid(categoryId));

            mockMvc.perform(delete(PRODUCTS + "/{id}", id))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get(PRODUCTS + "/{id}", id))
                    .andExpect(status().isNotFound());
            mockMvc.perform(delete(PRODUCTS + "/{id}", id))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("delete ACTIVE gives 409")
        void deleteActive() throws Exception {
            UUID categoryId = createCategory("Electronics");
            UUID id = createProduct(Body.valid(categoryId));
            activate(id).andExpect(status().isOk());

            mockMvc.perform(delete(PRODUCTS + "/{id}", id))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("Category rule")
    class CategoryRule {

        @Test
        @DisplayName("category with a product gives 409 CATEGORY_IN_USE; after deleting the product, 204")
        void categoryInUse() throws Exception {
            UUID categoryId = createCategory("Electronics");
            UUID productId = createProduct(Body.valid(categoryId));

            mockMvc.perform(delete(CATEGORIES + "/{id}", categoryId))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("CATEGORY_IN_USE"));

            mockMvc.perform(delete(PRODUCTS + "/{id}", productId))
                    .andExpect(status().isNoContent());

            mockMvc.perform(delete(CATEGORIES + "/{id}", categoryId))
                    .andExpect(status().isNoContent());
        }
    }


    // HELPERS
    private ResultActions postProduct(Body body) throws Exception {
        return mockMvc.perform(post(PRODUCTS).contentType(APPLICATION_JSON).content(body.json()));
    }

    private ResultActions putProduct(UUID id, Body body) throws Exception {
        return mockMvc.perform(put(PRODUCTS + "/{id}", id).contentType(APPLICATION_JSON).content(body.json()));
    }

    private ResultActions activate(UUID id) throws Exception {
        return mockMvc.perform(post(PRODUCTS + "/{id}/activate", id));
    }

    private ResultActions archive(UUID id) throws Exception {
        return mockMvc.perform(post(PRODUCTS + "/{id}/archive", id));
    }

    /** Creates a product through the API, asserts 201 and returns its id. */
    private UUID createProduct(Body body) throws Exception {
        MvcResult result = postProduct(body)
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
    }

    /**
     * Creates a category through the API, asserts 201 and returns its id.
     * The slug is derived from the name ("Category A" -> "category-a") to satisfy the
     * ^[a-z0-9]+(-[a-z0-9]+)*$ pattern, so use names made of letters, digits and spaces only.
     */
    private UUID createCategory(String name) throws Exception {
        String slug = name.toLowerCase().trim().replaceAll("\\s+", "-");
        String body = "{\"name\":\"%s\",\"slug\":\"%s\",\"description\":\"Test category\"}"
                .formatted(name, slug);

        MvcResult result = mockMvc.perform(post(CATEGORIES)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
    }

    /** Ensures distinct createdAt values (Mongo stores millisecond precision) for the "newest first" check. */
    private static void pause() throws InterruptedException {
        Thread.sleep(5);
    }

    /** Immutable request-body builder, so each test only states what differs from a valid request. */
    private record Body(String sku, String name, String amount, String currency, UUID categoryId, String attributes) {

        static Body valid(UUID categoryId) {
            return new Body("LAPTOP-001", "Laptop", "799.99", "USD", categoryId, null);
        }

        Body withSku(String value) {
            return new Body(value, name, amount, currency, categoryId, attributes);
        }

        Body withName(String value) {
            return new Body(sku, value, amount, currency, categoryId, attributes);
        }

        Body withAmount(String value) {
            return new Body(sku, name, value, currency, categoryId, attributes);
        }

        Body withCurrency(String value) {
            return new Body(sku, name, amount, value, categoryId, attributes);
        }

        Body withAttributes(String json) {
            return new Body(sku, name, amount, currency, categoryId, json);
        }

        String json() {
            String attributesPart = attributes == null ? "" : ",\"attributes\":" + attributes;
            return """
                    {"sku":"%s","name":"%s","description":"Test description",\
                    "price":{"amount":"%s","currency":"%s"},\
                    "categoryId":"%s"%s}""".formatted(sku, name, amount, currency, categoryId, attributesPart);
        }
    }
}