package com.rosati.productfinder.source.internal;

import com.rosati.productfinder.product.Product;
import com.rosati.productfinder.product.ProductQuery;
import com.rosati.productfinder.product.ProductSource;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rosati.productfinder.source.internal.CatalogResponse.decimal;
import static com.rosati.productfinder.source.internal.CatalogResponse.integer;
import static com.rosati.productfinder.source.internal.CatalogResponse.required;
import static com.rosati.productfinder.source.internal.CatalogResponse.requiredArray;
import static com.rosati.productfinder.source.internal.CatalogResponse.text;
import static com.rosati.productfinder.source.internal.CatalogResponse.uri;

/** Adapter for B&H Photo Video's POST-based graphics-card search API. */
class BhPhotoVideoProductSource implements ProductSource {
    private final RestClient client;

    BhPhotoVideoProductSource(RestClient client) {
        this.client = client;
    }

    @Override
    public String name() {
        return "BHPhotoVideo";
    }

    @Override
    public List<Product> search(ProductQuery query) {
        var request = Map.of("searchTerm", query.text(), "maxResults", query.limit(),
                "category", "graphics-cards");
        JsonNode response = client.post().uri("/api/products/search").contentType(MediaType.APPLICATION_JSON)
                .body(request).retrieve().body(JsonNode.class);
        JsonNode matches = requiredArray(response, "/results", name());
        var results = new ArrayList<Product>();
        for (JsonNode item : matches) {
            results.add(new Product(name(), required(item, "/sku", name()), required(item, "/name", name()),
                    required(item, "/manufacturer", name()), required(item, "/graphicsProcessor", name()),
                    integer(item, "/videoMemory/capacityGb"), required(item, "/videoMemory/type", name()),
                    text(item, "/description"), decimal(item, "/price/value"),
                    text(item, "/price/currency"), uri(item, "/url"),
                    Map.of("availability", text(item, "/availability"))));
        }
        return List.copyOf(results);
    }

}
