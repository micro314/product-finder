package com.rosati.productfinder.cache.internal;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;

public interface CachedCatalogProductRepository extends MongoRepository<CachedCatalogProduct, String> {
    long deleteBySourceAndCachedAtBefore(String source, Instant cachedAt);
}
