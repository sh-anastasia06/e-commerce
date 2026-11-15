package io.github.shanastasia06.productcatalogservice;

import io.github.shanastasia06.productcatalogservice.common.api.ErrorCode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ContractDriftTest {
    @Test
    void contractListsEveryErrorCode() throws IOException {
        String contract = Files.readString(Path.of("docs/openapi.yaml"));

        for (ErrorCode code : ErrorCode.values()) {
            assertThat(contract)
                    .as("openapi.yaml must list error code %s in the code enum", code)
                    .contains("- " + code.name());
        }
    }
}
