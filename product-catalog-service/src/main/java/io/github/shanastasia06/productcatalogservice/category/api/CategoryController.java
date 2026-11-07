package io.github.shanastasia06.productcatalogservice.category.api;

import io.github.shanastasia06.productcatalogservice.category.domain.Category;
import io.github.shanastasia06.productcatalogservice.category.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryApiMapper categoryApiMapper;

    public CategoryController(CategoryService categoryService, CategoryApiMapper categoryApiMapper) {
        this.categoryService = categoryService;
        this.categoryApiMapper = categoryApiMapper;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        List<CategoryResponse> response = categoryService.getAll().stream()
                .map(categoryApiMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable @NotNull UUID id) {
        CategoryResponse response = categoryApiMapper.toResponse(categoryService.getById(id));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.create(
                request.name(), request.slug(), request.description()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(category.getId().value())
                .toUri();

        return ResponseEntity.created(location).body(categoryApiMapper.toResponse(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable @NotNull UUID id, @Valid @RequestBody CategoryRequest request) {
        Category updatedCategory = categoryService.update(id, request.name(), request.slug(), request.description());
        return  ResponseEntity.ok(categoryApiMapper.toResponse(updatedCategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NotNull UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
