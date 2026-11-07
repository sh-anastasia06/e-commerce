package io.github.shanastasia06.productcatalogservice.category.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDocument {
    @Id
    private UUID id;

    private String name;

    @Indexed(unique = true)
    private String slug;

    private String description;
}
