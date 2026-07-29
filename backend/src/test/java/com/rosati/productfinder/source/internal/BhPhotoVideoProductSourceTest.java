package com.rosati.productfinder.source.internal;

import com.rosati.productfinder.product.ProductQuery;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BhPhotoVideoProductSourceTest {
    @Test
    void postsABhPhotoVideoQueryAndMapsItsGraphicsCardShape() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://bh.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://bh.test/api/products/search")).andExpect(method(POST))
                .andExpect(content().json("{\"searchTerm\":\"rx 9070\",\"maxResults\":3,\"category\":\"graphics-cards\"}"))
                .andRespond(withSuccess("""
                        {"results":[{"sku":"B-9","name":"Sapphire Radeon RX 9070","manufacturer":"Sapphire",
                        "graphicsProcessor":"Radeon RX 9070","videoMemory":{"capacityGb":16,"type":"GDDR6"},"clock":{"boostMhz":2520},
                        "description":"RDNA graphics card","price":{"value":"599.00","currency":"USD"},
                        "url":"https://bh.test/c/product/B-9","availability":"In Stock"}]}
                        """, APPLICATION_JSON));

        var source = new BhPhotoVideoProductSource(builder.build());
        var product = source.search(new ProductQuery("rx 9070", 3)).getFirst();

        assertThat(source.name()).isEqualTo("BHPhotoVideo");
        assertThat(product.externalId()).isEqualTo("B-9");
        assertThat(product.chipset()).isEqualTo("Radeon RX 9070");
        assertThat(product.memorySizeGb()).isEqualTo(16);
        assertThat(product.boostClockMhz()).isEqualTo(2520);
        assertThat(product.attributes()).containsEntry("availability", "In Stock");
        server.verify();
    }
}
