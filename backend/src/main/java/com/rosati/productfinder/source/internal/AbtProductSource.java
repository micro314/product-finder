package com.rosati.productfinder.source.internal;

import com.rosati.productfinder.product.Product;
import com.rosati.productfinder.product.ProductQuery;
import com.rosati.productfinder.product.ProductSource;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rosati.productfinder.source.internal.CatalogResponse.decimal;
import static com.rosati.productfinder.source.internal.CatalogResponse.integer;
import static com.rosati.productfinder.source.internal.CatalogResponse.optionalInteger;
import static com.rosati.productfinder.source.internal.CatalogResponse.optionalText;
import static com.rosati.productfinder.source.internal.CatalogResponse.required;
import static com.rosati.productfinder.source.internal.CatalogResponse.requiredArray;
import static com.rosati.productfinder.source.internal.CatalogResponse.text;
import static com.rosati.productfinder.source.internal.CatalogResponse.uri;

/** Adapter for Abt's document-style graphics-card search API. */
class AbtProductSource implements ProductSource {
    private static final int DEFAULT_FULL_REFRESH_LIMIT = 10_000;

    private final RestClient client;
    private final int fullRefreshLimit;

    AbtProductSource(RestClient client) {
        this(client, DEFAULT_FULL_REFRESH_LIMIT);
    }

    AbtProductSource(RestClient client, int fullRefreshLimit) {
        this.client = client;
        this.fullRefreshLimit = fullRefreshLimit;
    }

    @Override
    public String name() {
        return "Abt";
    }

    @Override
    public List<Product> search(ProductQuery query) {
        return fetch(query.text(), query.limit());
    }

    @Override
    public List<Product> allProducts() {
        return fetch("*", fullRefreshLimit);
    }

    private List<Product> fetch(String query, int rows) {
        JsonNode response = client.get().uri(uri -> uri.path("/resources/search")
                        .queryParam("query", query).queryParam("rows", rows)
                        .queryParam("category", "graphics-cards").build())
                .retrieve().body(JsonNode.class);
        JsonNode data = requiredArray(response, "/response/docs", name());
        var results = new ArrayList<Product>();
        for (JsonNode item : data) {
            results.add(new Product(name(), required(item, "/id", name()), required(item, "/productName", name()),
                    required(item, "/brandName", name()), required(item, "/gpuChipset", name()), optionalText(item, "/gpuManufacturer"),
                    integer(item, "/vramGb"), required(item, "/vramType", name()), optionalInteger(item, "/boostClockMhz"),
                    text(item, "/description"), decimal(item, "/salePrice"),
                    text(item, "/currency"), uri(item, "/productPage"),
                    Map.of("stockStatus", text(item, "/stockStatus"))));
        }
        return List.copyOf(results);
    }

}
