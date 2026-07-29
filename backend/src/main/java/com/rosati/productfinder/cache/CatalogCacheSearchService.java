package com.rosati.productfinder.cache;

import com.rosati.productfinder.cache.internal.CachedCatalogProduct;
import com.rosati.productfinder.cache.internal.CachedCatalogProductRepository;
import com.rosati.productfinder.product.Product;
import com.rosati.productfinder.product.ProductQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/** Searches products that have been imported into the local catalog cache. */
@Service
public class CatalogCacheSearchService {
    private final CachedCatalogProductRepository repository;

    public CatalogCacheSearchService(CachedCatalogProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> search(ProductQuery query) {
        String text = query.text().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(product -> product.matches(text))
                .limit(query.limit())
                .map(CachedCatalogProduct::toProduct)
                .toList();
    }
}
