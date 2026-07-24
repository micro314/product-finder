package com.rosati.productfinder.cache.internal;

import com.rosati.productfinder.product.Product;
import com.rosati.productfinder.product.ProductSource;
import com.rosati.productfinder.product.ProductSources;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogCacheRefreshJobTest {

    @Test
    void cachesEveryProductFromEachSourceAndRemovesStaleEntries() {
        ProductSource source = new ProductSource() {
            @Override
            public String name() {
                return "ExampleCatalog";
            }

            @Override
            public List<Product> search(com.rosati.productfinder.product.ProductQuery query) {
                return List.of();
            }

            @Override
            public List<Product> allProducts() {
                return List.of(product("E-1"), product("E-2"));
            }
        };
        ProductSources sources = () -> List.of(source);
        CachedCatalogProductRepository repository = mock(CachedCatalogProductRepository.class);
        when(repository.deleteBySourceAndCachedAtBefore(eq("ExampleCatalog"), any())).thenReturn(1L);

        new CatalogCacheRefreshJob(sources, repository).refreshCatalogs();

        verify(repository).saveAll(any());
        verify(repository).deleteBySourceAndCachedAtBefore(eq("ExampleCatalog"), any());
    }

    private Product product(String externalId) {
        return new Product("ExampleCatalog", externalId, "Example GPU", "Example", "Example GPU", 8,
                "GDDR6", "", new BigDecimal("99.99"), "USD", URI.create("https://example.test/" + externalId),
                Map.of());
    }
}
