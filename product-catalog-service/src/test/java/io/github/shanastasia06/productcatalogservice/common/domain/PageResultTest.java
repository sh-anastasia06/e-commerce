package io.github.shanastasia06.productcatalogservice.common.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class PageResultTest {
    @Test
    void totalPages_withZeroItem_returnsZero() {
        var page = new PageResult<>(List.of(), 0, 10, 0);
        assertThat(page.totalPages()).isEqualTo(0);
    }

    @Test
    void totalPages_withOneItem_returnsOne() {
        var page = new PageResult<>(List.of("item1"), 0, 10, 1);
        assertThat(page.totalPages()).isEqualTo(1);
    }

    @Test
    void totalPages_withExactlyOneFullPage_returnsOne() {
        var page = new PageResult<>(List.of("a", "b"), 0, 2, 2);
        assertThat(page.totalPages()).isEqualTo(1);
    }

    @Test
    void totalPages_withOneItemOverFullPage_returnsTwo() {
        var page = new PageResult<>(List.of("a", "b", "c"), 0, 2, 3);
        assertThat(page.totalPages()).isEqualTo(2);
    }

    @Test
    void content_shouldBeImmutable() {
        List<String> mutableList = new ArrayList<>();
        mutableList.add("item1");

        var page = new PageResult<>(mutableList, 0, 10, 1);

        assertThatThrownBy(() -> page.content().add("item2"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

}