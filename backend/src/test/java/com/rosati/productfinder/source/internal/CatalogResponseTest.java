package com.rosati.productfinder.source.internal;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CatalogResponseTest {
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void readsRequiredAndOptionalCatalogValues() throws Exception {
        var item = json.readTree("{\"id\":\"42\",\"price\":12.50,\"url\":\"https://catalog.test/42\",\"missing\":null}");
        assertThat(CatalogResponse.required(item, "/id", "test")).isEqualTo("42");
        assertThat(CatalogResponse.text(item, "/missing")).isEmpty();
        assertThat(CatalogResponse.decimal(item, "/price")).isEqualByComparingTo(new BigDecimal("12.50"));
        assertThat(CatalogResponse.integer(json.readTree("{\"memory\":12}"), "/memory")).isEqualTo(12);
        assertThat(CatalogResponse.decimal(item, "/unknown")).isNull();
        assertThat(CatalogResponse.uri(item, "/url")).hasToString("https://catalog.test/42");
        assertThat(CatalogResponse.uri(item, "/unknown")).isNull();
    }

    @Test
    void validatesCollectionsAndRequiredFields() throws Exception {
        var response = json.readTree("{\"items\":[]}");
        assertThat(CatalogResponse.requiredArray(response, "/items", "test")).isEmpty();
        assertThatIllegalArgumentException().isThrownBy(() -> CatalogResponse.requiredArray(response, "/other", "test"));
        assertThatIllegalArgumentException().isThrownBy(() -> CatalogResponse.requiredArray(null, "/items", "test"));
        assertThatIllegalArgumentException().isThrownBy(() -> CatalogResponse.required(response, "/id", "test"));
    }
}
