package com.rosati.productfinder.cache.internal;

import com.rosati.productfinder.product.Product;
import com.rosati.productfinder.product.ProductSource;
import com.rosati.productfinder.product.ProductSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
class CatalogCacheRefreshJob {
    private static final Logger log = LoggerFactory.getLogger(CatalogCacheRefreshJob.class);

    private final ProductSources sources;
    private final CachedCatalogProductRepository repository;

    CatalogCacheRefreshJob(ProductSources sources, CachedCatalogProductRepository repository) {
        this.sources = sources;
        this.repository = repository;
    }

    @Scheduled(cron = "${product-finder.cache-refresh-cron:0 0 0 * * *}", zone = "${product-finder.cache-refresh-zone:UTC}")
    void refreshCatalogs() {
        for (ProductSource source : sources.all()) {
            refresh(source);
        }
    }

    private void refresh(ProductSource source) {
        try {
            List<Product> products = source.allProducts();
            Instant cachedAt = Instant.now();
            repository.saveAll(products.stream().map(product -> CachedCatalogProduct.from(product, cachedAt)).toList());
            long deleted = repository.deleteBySourceAndCachedAtBefore(source.name(), cachedAt);
            log.info("Refreshed {} cached products from {} and removed {} stale products", products.size(), source.name(), deleted);
        } catch (UnsupportedOperationException exception) {
            log.warn("Skipping cache refresh for {}: {}", source.name(), exception.getMessage());
        } catch (RuntimeException exception) {
            log.warn("Cache refresh failed for {}: {}", source.name(), safeMessage(exception));
        }
    }

    private String safeMessage(RuntimeException exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }
}
