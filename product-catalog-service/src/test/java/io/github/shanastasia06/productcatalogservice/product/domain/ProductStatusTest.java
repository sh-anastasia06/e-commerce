package io.github.shanastasia06.productcatalogservice.product.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ProductStatusTest {
    @ParameterizedTest
    @CsvSource({
            "DRAFT, true, true, true, true",
            "ACTIVE, true, false, false, true",
            "ARCHIVED, false, false, false, false"
    })
    void shouldEvaluateStatusPermissions(
            ProductStatus status,
            boolean canEdit,
            boolean canDelete,
            boolean canActivate,
            boolean canArchive
    ) {
        assertThat(status.canBeEdited()).isEqualTo(canEdit);
        assertThat(status.canBeDeleted()).isEqualTo(canDelete);
        assertThat(status.canBeActivated()).isEqualTo(canActivate);
        assertThat(status.canBeArchived()).isEqualTo(canArchive);
    }
}
