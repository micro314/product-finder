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
import static com.rosati.productfinder.source.internal.CatalogResponse.required;
import static com.rosati.productfinder.source.internal.CatalogResponse.requiredArray;
import static com.rosati.productfinder.source.internal.CatalogResponse.text;
import static com.rosati.productfinder.source.internal.CatalogResponse.uri;

/** Adapter for Newegg's GET-based graphics-card search API. */
class NeweggProductSource implements ProductSource {
    private static final int DEFAULT_FULL_REFRESH_LIMIT = 10_000;

    private final RestClient client;
    private final int fullRefreshLimit;

    NeweggProductSource(RestClient client) {
        this(client, DEFAULT_FULL_REFRESH_LIMIT);
    }

    NeweggProductSource(RestClient client, int fullRefreshLimit) {
        this.client = client;
        this.fullRefreshLimit = fullRefreshLimit;
    }

    @Override
    public String name() {
        return "Newegg";
    }

    @Override
    public List<Product> search(ProductQuery query) {
        return fetch(query.text(), query.limit());
    }

    @Override
    public List<Product> allProducts() {
        return fetch("*", fullRefreshLimit);
    }

    private List<Product> fetch(String keyword, int pageSize) {
        JsonNode response = client.get().uri(uri -> uri.path("/api/search")
                        .queryParam("keyword", keyword).queryParam("pageSize", pageSize).build())
                .retrieve().body(JsonNode.class);
        JsonNode products = requiredArray(response, "/items", name());
        var results = new ArrayList<Product>();
        for (JsonNode item : products) {
            results.add(new Product(name(), required(item, "/itemNumber", name()), required(item, "/title", name()),
                    required(item, "/brand", name()), required(item, "/gpuModel", name()),
                    integer(item, "/memory/sizeGb"), required(item, "/memory/type", name()), optionalInteger(item, "/clock/boostMhz"),
                    text(item, "/description"), decimal(item, "/pricing/current"),
                    text(item, "/pricing/currency"), uri(item, "/productUrl"),
                    Map.of("inStock", item.at("/inStock").asBoolean())));
        }
        return List.copyOf(results);
    }

}
