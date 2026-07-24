package com.rosati.productfinder.search.internal;

import com.rosati.productfinder.product.Product;
import com.rosati.productfinder.product.ProductQuery;
import com.rosati.productfinder.product.ProductSource;
import com.rosati.productfinder.product.ProductSources;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
class ProductSearchService {
    private final ProductSources sources;

    ProductSearchService(ProductSources sources) {
        this.sources = sources;
    }

    SearchResponse search(ProductQuery query) {
        var products = new ArrayList<Product>();
        var failures = new ArrayList<SourceFailure>();

        for (ProductSource source : sources.all()) {
            try {
                List<Product> matches = source.search(query);
                if (matches != null) {
                    products.addAll(matches);
                }
            } catch (RuntimeException exception) {
                failures.add(new SourceFailure(source.name(), safeMessage(exception)));
            }
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
