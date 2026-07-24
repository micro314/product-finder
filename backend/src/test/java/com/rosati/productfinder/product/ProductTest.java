package com.rosati.productfinder.product;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ProductTest {
    @Test
    void normalizesOptionalValuesAndDefensivelyCopiesAttributes() {
        var attributes = new HashMap<String, Object>();
        attributes.put("color", "blue");
        var product = new Product("catalog", "42", "RTX 5070", "NVIDIA", "GeForce RTX 5070",
                12, "GDDR7", null, null, null, null, attributes);
        attributes.clear();

        assertThat(product.description()).isEmpty();
        assertThat(product.currency()).isEmpty();
        assertThat(product.attributes()).containsEntry("color", "blue");
    }

    @Test
    void rejectsMissingIdentityFields() {
        assertThatIllegalArgumentException().isThrownBy(() -> product(" ", "42", "RTX 5070", "NVIDIA", "RTX 5070", 12, "GDDR7"));
        assertThatIllegalArgumentException().isThrownBy(() -> product("source", null, "RTX 5070", "NVIDIA", "RTX 5070", 12, "GDDR7"));
        assertThatIllegalArgumentException().isThrownBy(() -> product("source", "42", "", "NVIDIA", "RTX 5070", 12, "GDDR7"));
        assertThatIllegalArgumentException().isThrownBy(() -> product("source", "42", "RTX 5070", "", "RTX 5070", 12, "GDDR7"));
        assertThatIllegalArgumentException().isThrownBy(() -> product("source", "42", "RTX 5070", "NVIDIA", "", 12, "GDDR7"));
        assertThatIllegalArgumentException().isThrownBy(() -> product("source", "42", "RTX 5070", "NVIDIA", "RTX 5070", 0, "GDDR7"));
        assertThatIllegalArgumentException().isThrownBy(() -> product("source", "42", "RTX 5070", "NVIDIA", "RTX 5070", 12, ""));
    }

    private Product product(String source, String id, String name, String manufacturer, String chipset,
                            int memorySizeGb, String memoryType) {
        return new Product(source, id, name, manufacturer, chipset, memorySizeGb, memoryType,
                null, null, null, null, null);
    }
}
