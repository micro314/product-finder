package com.rosati.productfinder.search.internal;

import com.rosati.productfinder.product.Product;

import java.util.List;

record SearchResponse(List<Product> products, List<SourceFailure> failures) {
    SearchResponse {
        products = products == null ? List.of() : List.copyOf(products);
        failures = failures == null ? List.of() : List.copyOf(failures);
    }
}
