package com.rosati.productfinder.product;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ProductQueryTest {
    @Test
    void trimsAValidQuery() {
        assertThat(new ProductQuery("  rtx 5070  ", 10).text()).isEqualTo("rtx 5070");
    }

    @Test
    void rejectsBlankTextAndLimitsOutsideBounds() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ProductQuery(" ", 1));
        assertThatIllegalArgumentException().isThrownBy(() -> new ProductQuery("rtx 5070", 0));
        assertThatIllegalArgumentException().isThrownBy(() -> new ProductQuery("rtx 5070", 101));
    }
}
