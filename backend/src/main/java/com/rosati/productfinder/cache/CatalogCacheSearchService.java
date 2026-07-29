package com.rosati.productfinder.cache;

import com.rosati.productfinder.cache.internal.CachedCatalogProduct;
import com.rosati.productfinder.cache.internal.CachedCatalogProductRepository;
import com.rosati.productfinder.product.Product;
import com.rosati.productfinder.product.ProductQuery;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.math.BigDecimal;
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
                .filter(product -> product.matches(text) && product.matches(query.filters()))
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

    public CatalogFilterOptions filterOptions() {
        return new CatalogFilterOptions(
                distinctStrings("source"),
                distinctStrings("manufacturer"), distinctStrings("chipsetManufacturer"), distinctStrings("chipset"),
                distinctStrings("memoryType"), distinctNumbers("memorySizeGb"), clockFrequencies(), priceIncrements());
    }

    private List<String> distinctStrings(String field) {
        return mongoTemplate.query(CachedCatalogProduct.class).distinct(field).as(String.class).all().stream()
                .filter(value -> value != null && !value.isBlank()).sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    private List<Integer> distinctNumbers(String field) {
        return mongoTemplate.query(CachedCatalogProduct.class).distinct(field).as(Integer.class).all().stream()
                .filter(value -> value != null).sorted().toList();
    }

    private List<BigDecimal> distinctDecimals(String field) {
        return mongoTemplate.query(CachedCatalogProduct.class).distinct(field).as(BigDecimal.class).all().stream()
                .filter(value -> value != null).sorted().toList();
    }

    private List<BigDecimal> priceIncrements() {
        List<BigDecimal> prices = distinctDecimals("price");
        if (prices.isEmpty()) return List.of(BigDecimal.ZERO);
        BigDecimal increment = BigDecimal.valueOf(50);
        int maximum = prices.get(prices.size() - 1).divide(increment, 0, java.math.RoundingMode.CEILING).intValue();
        return java.util.stream.IntStream.rangeClosed(0, maximum)
                .mapToObj(value -> increment.multiply(BigDecimal.valueOf(value))).toList();
    }

    private List<Integer> clockFrequencies() {
        List<Integer> clocks = distinctNumbers("boostClockMhz");
        if (clocks.isEmpty()) return List.of();
        int maximum = ((clocks.get(clocks.size() - 1) + 99) / 100) * 100;
        return java.util.stream.IntStream.rangeClosed(0, maximum / 100).map(value -> value * 100).boxed().toList();
    }
}
