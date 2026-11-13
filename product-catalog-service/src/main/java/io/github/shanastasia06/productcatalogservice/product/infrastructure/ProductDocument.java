package io.github.shanastasia06.productcatalogservice.product.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Document(collection = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {
    @Id
    private UUID id;

    @Indexed(unique = true)
    private String sku;

    private String name;
    private String description;
    private BigDecimal priceAmount;
    private String priceCurrency;

    @Indexed
    private UUID categoryId;

    private Map<String, Object> attributes;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    @Version
    private Long version;
}
