package com.rosati.productfinder.product;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductFiltersTest {
    @Test
    void supportsNameFilter() {
        var filters = new ProductFilters("  RTX 4090  ", List.of(), List.of(), List.of(), List.of(), List.of(),
                null, null, null, null, null, null);
        assertThat(filters.name()).isEqualTo("RTX 4090");
        assertThat(filters.isEmpty()).isFalse();
    }

    @Test
    void normalizesBlankNameToNull() {
        var filters = new ProductFilters("   ", List.of(), List.of(), List.of(), List.of(), List.of(),
                null, null, null, null, null, null);
        assertThat(filters.name()).isNull();
        assertThat(filters.isEmpty()).isTrue();
    }
}
