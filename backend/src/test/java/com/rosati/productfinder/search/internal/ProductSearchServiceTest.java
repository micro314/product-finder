package com.rosati.productfinder.search.internal;

import com.rosati.productfinder.cache.CatalogCacheSearchService;
import com.rosati.productfinder.product.Product;
import com.rosati.productfinder.product.ProductQuery;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductSearchServiceTest {
    @Test
    void returnsSortedProductsFromTheCatalogCache() {
        var cache = mock(CatalogCacheSearchService.class);
        when(cache.search(new ProductQuery("rtx 5070", 20))).thenReturn(List.of(
                product("ZOTAC GeForce RTX 5070", "2"), product("ASUS GeForce RTX 5070", "1")));

        var response = new ProductSearchService(cache).search(new ProductQuery("rtx 5070", 20));

        assertThat(response.products()).extracting(Product::name)
                .containsExactly("ASUS GeForce RTX 5070", "ZOTAC GeForce RTX 5070");
        assertThat(response.failures()).isEmpty();
    }

    @Test
    void isolatesCacheFailures() {
        var cache = mock(CatalogCacheSearchService.class);
        when(cache.search(new ProductQuery("rtx 5070", 20))).thenThrow(new IllegalStateException());

        var response = new ProductSearchService(cache).search(new ProductQuery("rtx 5070", 20));

        assertThat(response.products()).isEmpty();
        assertThat(response.failures()).singleElement().satisfies(failure -> {
            assertThat(failure.source()).isEqualTo("catalog cache");
            assertThat(failure.message()).isEqualTo("IllegalStateException");
        });
    }

    private Product product(String name, String id) {
        return new Product("catalog cache", id, name, "NVIDIA", "GeForce RTX 5070", 12, "GDDR7",
                null, null, null, null, null);
    }
}
