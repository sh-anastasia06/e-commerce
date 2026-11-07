package io.github.shanastasia06.productcatalogservice.category.api;

import io.github.shanastasia06.productcatalogservice.TestcontainersConfiguration;
import io.github.shanastasia06.productcatalogservice.category.infrastructure.CategoryDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class CategoryControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    // clears collection documents
    @BeforeEach
    void cleanUp() {
        mongoTemplate.remove(new Query(), CategoryDocument.class);
    }

    @Test
    void create_withValidBody_returns201WithLocationAndBody() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                            {"name":"Electronics","slug":"electronics","description":"Phones"}
                            """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.slug").value("electronics"))
                .andExpect(jsonPath("$.description").value("Phones"));
    }

    @Test
    void create_withDuplicateSlug_returns409() throws Exception {
        String content = """
            {"name":"Electronics","slug":"electronics","description":"Phones"}
            """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(content))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(content))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("SLUG_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void create_withBeanValidationFailure_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                    {"name":"","slug":"Bad Slug"}
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[*].field", hasItems("name", "slug")));
    }

    @Test
    void create_withUnknownCategory_returns404() throws Exception {
        UUID randomUuid = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/categories/{id}", randomUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(containsString(randomUuid.toString())));
    }

    @Test
    void getAll_returns200WithList() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                    {"name":"Electronics","slug":"electronics","description":"Phones"}
                    """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[0].slug").value("electronics"));
    }

    @Test
    void getById_withValidId_returns200WithBody() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/categories")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                    {"name":"Books","slug":"books","description":"Literature"}
                    """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(responseBody, "$.id");

        mockMvc.perform(get("/api/v1/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Books"));
    }

    @Test
    void update_withValidBody_returns200WithUpdatedBody() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/categories")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                    {"name":"Old Name","slug":"old-slug","description":"Old"}
                    """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(responseBody, "$.id");

        mockMvc.perform(put("/api/v1/categories/{id}", id)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                    {"name":"New Name","slug":"new-slug","description":"New"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.slug").value("new-slug"))
                .andExpect(jsonPath("$.description").value("New"));
    }

    @Test
    void delete_withValidId_returns204AndRemovesCategory() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/categories")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                    {"name":"To Delete","slug":"to-delete","description":"Temp"}
                    """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(responseBody, "$.id");

        mockMvc.perform(delete("/api/v1/categories/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/categories/{id}", id))
                .andExpect(status().isNotFound());
    }
}
