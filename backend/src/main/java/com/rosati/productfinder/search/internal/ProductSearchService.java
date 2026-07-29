package com.rosati.productfinder.search.internal;

import com.rosati.productfinder.cache.CatalogCacheSearchService;
import com.rosati.productfinder.product.Product;
import com.rosati.productfinder.product.ProductQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
class ProductSearchService {
    private final CatalogCacheSearchService cache;

    ProductSearchService(CatalogCacheSearchService cache) {
        this.cache = cache;
    }

    SearchResponse search(ProductQuery query) {
        List<Product> products;
        var failures = new java.util.ArrayList<SourceFailure>();
        try {
            products = new ArrayList<>(cache.search(query));
        } catch (RuntimeException exception) {
            products = new ArrayList<>();
            failures.add(new SourceFailure("catalog cache", safeMessage(exception)));
        }

        products.sort(Comparator.comparing(Product::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Product::source)
                .thenComparing(Product::externalId));
        return new SearchResponse(products, failures);
    }

    private String safeMessage(RuntimeException exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }
}
