package io.github.shanastasia06.productcatalogservice.common.domain;

import io.github.shanastasia06.productcatalogservice.common.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class PageQueryTest {
    private final int MAX_SIZE = PageQuery.MAX_SIZE;

    @Test
    void shouldCreateValidPageQuery() {
        PageQuery pageQuery = new PageQuery(1, 20);

        assertThat(pageQuery).isNotNull();
        assertThat(pageQuery.page()).isEqualTo(1);
        assertThat(pageQuery.size()).isEqualTo(20);
    }

    @Test
    void shouldThrowWithNegativePage() {
        assertThatThrownBy(() -> new PageQuery(-1, 20))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldThrowWithZeroSize() {
        assertThatThrownBy(() -> new PageQuery(1, 0))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldCreateWithMaxSize() {
        PageQuery pageQuery = new PageQuery(1, MAX_SIZE);

        assertThat(pageQuery).isNotNull();
        assertThat(pageQuery.size()).isEqualTo(MAX_SIZE);
    }

    @Test
    void shouldCreateWithMinSize() {
        PageQuery pageQuery = new PageQuery(1, 1);

        assertThat(pageQuery).isNotNull();
        assertThat(pageQuery.size()).isEqualTo(1);
    }

    @Test
    void shouldThrowWithSizeGreaterThanMaxSize() {
        assertThatThrownBy(() -> new PageQuery(1, MAX_SIZE + 1))
                .isInstanceOf(DomainValidationException.class);
    }
}
