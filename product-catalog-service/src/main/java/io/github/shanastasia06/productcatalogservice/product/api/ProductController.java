package io.github.shanastasia06.productcatalogservice.product.api;

import io.github.shanastasia06.productcatalogservice.common.domain.PageResult;
import io.github.shanastasia06.productcatalogservice.product.domain.Product;
import io.github.shanastasia06.productcatalogservice.product.service.ProductSearchQuery;
import io.github.shanastasia06.productcatalogservice.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;
    private final ProductApiMapper mapper;

    public ProductController(ProductService productService, ProductApiMapper mapper) {
        this.productService = productService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        Product product = productService.create(mapper.toCommand(request));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(product.getId().value())
                .toUri();

        return ResponseEntity.created(location).body(mapper.toResponse(product));
    }

    @GetMapping
    public ResponseEntity<ProductPageResponse> search(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResult<Product> result = productService.search(
                new ProductSearchQuery(categoryId, minPrice, maxPrice, page, size)
        );
        return ResponseEntity.ok(mapper.toPageResponse(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        Product product = productService.getById(id);
        return ResponseEntity.ok(mapper.toResponse(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        Product updated = productService.update(id, mapper.toCommand(request));
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ProductResponse> activate(@PathVariable UUID id) {
        Product activated = productService.activate(id);
        return ResponseEntity.ok(mapper.toResponse(activated));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ProductResponse> archive(@PathVariable UUID id) {
        Product archived = productService.archive(id);
        return ResponseEntity.ok(mapper.toResponse(archived));
    }
}
