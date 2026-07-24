package com.rosati.productfinder.product;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ProductQueryTest {
    @Test
    void trimsAValidQuery() {
        assertThat(new ProductQuery("  chair  ", 10).text()).isEqualTo("chair");
    }

    @Test
    void rejectsBlankTextAndLimitsOutsideBounds() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ProductQuery(" ", 1));
        assertThatIllegalArgumentException().isThrownBy(() -> new ProductQuery("chair", 0));
        assertThatIllegalArgumentException().isThrownBy(() -> new ProductQuery("chair", 101));
    }
}
