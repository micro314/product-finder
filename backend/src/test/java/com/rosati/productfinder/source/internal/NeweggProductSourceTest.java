package com.rosati.productfinder.source.internal;

import com.rosati.productfinder.product.ProductQuery;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class NeweggProductSourceTest {
    @Test
    void queriesNeweggAndMapsItsGraphicsCardShape() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://newegg.test")
                .defaultHeader("X-Newegg-Api-Key", "secret");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo("https://newegg.test/api/search?keyword=rtx%205070&pageSize=4"))
                .andExpect(method(GET)).andExpect(header("X-Newegg-Api-Key", "secret"))
                .andRespond(withSuccess("""
                        {"items":[{"itemNumber":"N-1","title":"ASUS GeForce RTX 5070 Prime","brand":"ASUS",
                        "gpuModel":"GeForce RTX 5070","memory":{"sizeGb":12,"type":"GDDR7"},"clock":{"boostMhz":2500},
                        "description":"Triple-fan graphics card","pricing":{"current":649.99,"currency":"USD"},
                        "productUrl":"https://newegg.test/p/N-1","inStock":true}]}
                        """, APPLICATION_JSON));

        var source = new NeweggProductSource(builder.build());
        var product = source.search(new ProductQuery("rtx 5070", 4)).getFirst();

        assertThat(source.name()).isEqualTo("Newegg");
        assertThat(product.externalId()).isEqualTo("N-1");
        assertThat(product.manufacturer()).isEqualTo("ASUS");
        assertThat(product.chipset()).isEqualTo("GeForce RTX 5070");
        assertThat(product.memorySizeGb()).isEqualTo(12);
        assertThat(product.memoryType()).isEqualTo("GDDR7");
        assertThat(product.boostClockMhz()).isEqualTo(2500);
        assertThat(product.price()).isEqualByComparingTo(new BigDecimal("649.99"));
        assertThat(product.attributes()).containsEntry("inStock", true);
        server.verify();
    }
}
