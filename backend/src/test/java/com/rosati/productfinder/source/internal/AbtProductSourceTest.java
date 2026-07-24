package com.rosati.productfinder.source.internal;

import com.rosati.productfinder.product.ProductQuery;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AbtProductSourceTest {
    @Test
    void queriesAbtAndMapsItsGraphicsCardShape() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://abt.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://abt.test/resources/search?query=arc%20b580&rows=2&category=graphics-cards"))
                .andRespond(withSuccess("""
                        {"response":{"docs":[{"id":"A-7","productName":"Intel Arc B580 Limited Edition",
                        "brandName":"Intel","gpuChipset":"Arc B580","vramGb":12,"vramType":"GDDR6",
                        "description":"Battlemage graphics card","salePrice":249.99,"currency":"USD",
                        "productPage":"https://abt.test/product/A-7","stockStatus":"Available"}]}}
                        """, APPLICATION_JSON));

        var source = new AbtProductSource(builder.build());
        var product = source.search(new ProductQuery("arc b580", 2)).getFirst();

        assertThat(source.name()).isEqualTo("Abt");
        assertThat(product.name()).isEqualTo("Intel Arc B580 Limited Edition");
        assertThat(product.manufacturer()).isEqualTo("Intel");
        assertThat(product.productUrl()).hasToString("https://abt.test/product/A-7");
        assertThat(product.attributes()).containsEntry("stockStatus", "Available");
        server.verify();
    }
}
