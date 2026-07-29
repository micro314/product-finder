package com.rosati.productfinder.cache;

import com.rosati.productfinder.cache.internal.CachedCatalogProduct;
import com.rosati.productfinder.cache.internal.CachedCatalogProductRepository;
import com.rosati.productfinder.product.Product;
import com.rosati.productfinder.product.ProductQuery;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

/** Searches products that have been imported into the local catalog cache. */
@Service
public class CatalogCacheSearchService {
    private final CachedCatalogProductRepository repository;
    private final MongoTemplate mongoTemplate;

    public CatalogCacheSearchService(CachedCatalogProductRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<Product> search(ProductQuery query) {
        String text = query.text().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(product -> product.matches(text))
                .limit(query.limit())
                .map(CachedCatalogProduct::toProduct)
                .toList();
    }

    public CatalogIndexStatus status() {
        long vendorCount = mongoTemplate.query(CachedCatalogProduct.class)
                .distinct("source")
                .as(String.class)
                .all()
                .size();
        long recordCount = repository.count();
        Instant lastPolledAt = repository.findAll(Sort.by(Sort.Direction.DESC, "cachedAt")).stream()
                .map(CachedCatalogProduct::cachedAt)
                .findFirst()
                .orElse(null);
        return new CatalogIndexStatus(vendorCount, recordCount, lastPolledAt);
    }
}
