package io.github.shanastasia06.productcatalogservice.product.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AttributesTest {
    @Test
    void shouldCreateEmptyAttributesOnNullOrEmptyMap() {
        assertThat(new Attributes(null).values()).isEmpty();
        assertThat(Attributes.empty().values()).isEmpty();
    }

    @Test
    void shouldAcceptValidAttributesAndNormalizeKeys() {
        Map<String, Object> input = Map.of(
                "  key1  ", "value",
                "key2", true,
                "key3", 100,
                "key4", 12.34
        );

        Attributes attrs = new Attributes(input);

        assertThat(attrs.values())
                .containsEntry("key1", "value")
                .containsEntry("key2", true)
                .containsEntry("key3", 100)
                .containsEntry("key4", 12.34);
    }

    @Test
    void shouldThrowWhenExceedsMaxEntries() {
        Map<String, Object> input = new HashMap<>();
        for (int i = 0; i <= Attributes.MAX_ENTRIES; i++) {
            input.put("key" + i, "val");
        }

        assertThatThrownBy(() -> new Attributes(input))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("must not contain more than 50 entries");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t"})
    void shouldRejectBlankKeys(String blankKey) {
        assertThatThrownBy(() -> new Attributes(Map.of(blankKey, "val")))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("key must not be blank");
    }

    @Test
    void shouldRejectKeyTooLong() {
        String longKey = "a".repeat(Attributes.MAX_KEY_LENGTH + 1);
        assertThatThrownBy(() -> new Attributes(Map.of(longKey, "val")))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("must not exceed 50 characters");
    }

    @ParameterizedTest
    @ValueSource(strings = {"key.name", "key$name", ".key", "key$"})
    void shouldRejectKeysWithForbiddenChars(String invalidKey) {
        assertThatThrownBy(() -> new Attributes(Map.of(invalidKey, "val")))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldRejectNullValues() {
        Map<String, Object> input = new HashMap<>();
        input.put("key", null);

        assertThatThrownBy(() -> new Attributes(input))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("value must not be null");
    }

    @Test
    void shouldRejectStringValueTooLong() {
        String longVal = "a".repeat(Attributes.MAX_STRING_VALUE_LENGTH + 1);
        assertThatThrownBy(() -> new Attributes(Map.of("key", longVal)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("value must not exceed 50 characters");
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void shouldRejectNonFiniteNumbers(double invalidNumber) {
        assertThatThrownBy(() -> new Attributes(Map.of("key", invalidNumber)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("must be a finite number");
    }

    @Test
    void shouldRejectUnsupportedTypes() {
        assertThatThrownBy(() -> new Attributes(Map.of("key", List.of(1, 2))))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("has unsupported type");
    }

    @Test
    void shouldPreserveOrderAndBeUnmodifiable() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("second", 2);
        input.put("first", 1);

        Attributes attrs = new Attributes(input);

        assertThat(attrs.values().keySet()).containsExactly("second", "first");
        assertThatThrownBy(() -> attrs.values().put("third", 3))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
