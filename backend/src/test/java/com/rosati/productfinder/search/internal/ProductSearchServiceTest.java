package com.rosati.productfinder.search.internal;

import com.rosati.productfinder.product.Product;
import com.rosati.productfinder.product.ProductQuery;
import com.rosati.productfinder.product.ProductSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductSearchServiceTest {
    @Test
    void combinesSortsAndIsolatesSourceFailures() {
        ProductSource successful = source("working", List.of(
                product("working", "2", "ZOTAC GeForce RTX 5070"),
                product("working", "1", "ASUS GeForce RTX 5070")), null);
        ProductSource failing = source("broken", null, new IllegalStateException("offline"));
        var service = new ProductSearchService(() -> List.of(successful, failing));

        var response = service.search(new ProductQuery("rtx 5070", 20));

        assertThat(response.products()).extracting(Product::name)
                .containsExactly("ASUS GeForce RTX 5070", "ZOTAC GeForce RTX 5070");
        assertThat(response.failures()).singleElement().satisfies(failure -> {
            assertThat(failure.source()).isEqualTo("broken");
            assertThat(failure.message()).isEqualTo("offline");
        });
    }

    @Test
    void toleratesNullResultsAndExceptionsWithoutMessages() {
        var service = new ProductSearchService(() -> List.of(source("empty", null, null),
                source("bad", null, new IllegalStateException())));
        var response = service.search(new ProductQuery("rtx 5070", 20));
        assertThat(response.products()).isEmpty();
        assertThat(response.failures().getFirst().message()).isEqualTo("IllegalStateException");
    }

    private ProductSource source(String name, List<Product> products, RuntimeException failure) {
        return new ProductSource() {
            public String name() { return name; }
            public List<Product> search(ProductQuery query) { if (failure != null) throw failure; return products; }
        };
    }

    private Product product(String source, String id, String name) {
        return new Product(source, id, name, "NVIDIA", "GeForce RTX 5070", 12, "GDDR7",
                null, null, null, null, null);
    }
}
