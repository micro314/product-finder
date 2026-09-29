package com.rosati.productfinder.cache.internal;

import com.rosati.productfinder.product.Product;
import com.rosati.productfinder.product.ProductFilters;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Document("catalog_products")
@CompoundIndex(name = "catalog_products_source_external_id", def = "{'source': 1, 'externalId': 1}", unique = true)
public class CachedCatalogProduct {
    @Id
    private String id;
    private String source;
    private String externalId;
    private String name;
    private String manufacturer;
    private String chipset;
    private String chipsetManufacturer;
    private int memorySizeGb;
    private String memoryType;
    private Integer boostClockMhz;
    private String description;
    private BigDecimal price;
    private String currency;
    private String productUrl;
    private Map<String, Object> attributes;
    private Instant cachedAt;

    protected CachedCatalogProduct() {
    }

    private CachedCatalogProduct(Product product, Instant cachedAt) {
        this.id = product.source() + ":" + product.externalId();
        this.source = product.source();
        this.externalId = product.externalId();
        this.name = product.name();
        this.manufacturer = product.manufacturer();
        this.chipset = product.chipset();
        this.chipsetManufacturer = product.chipsetManufacturer();
        this.memorySizeGb = product.memorySizeGb();
        this.memoryType = product.memoryType();
        this.boostClockMhz = product.boostClockMhz();
        this.description = product.description();
        this.price = product.price();
        this.currency = product.currency();
        this.productUrl = product.productUrl() == null ? null : product.productUrl().toString();
        this.attributes = product.attributes();
        this.cachedAt = cachedAt;
    }

    static CachedCatalogProduct from(Product product, Instant cachedAt) {
        return new CachedCatalogProduct(product, cachedAt);
    }

    public Instant cachedAt() {
        return cachedAt;
    }

    public boolean matches(String query) {
        return contains(name, query) || contains(manufacturer, query) || contains(chipset, query)
                || contains(chipsetManufacturer, query);
    }

    public boolean matches(ProductFilters filters) {
        return contains(name, filters.name())
                && containsAny(source, filters.source()) && containsAny(manufacturer, filters.manufacturer())
                && containsAny(chipsetManufacturer, filters.chipsetManufacturer()) && containsAny(chipset, filters.chipset())
                && containsAny(memoryType, filters.memoryType())
                && inRange(memorySizeGb, filters.minMemorySizeGb(), filters.maxMemorySizeGb())
                && inRange(boostClockMhz, filters.minBoostClockMhz(), filters.maxBoostClockMhz())
                && inRange(price, filters.minPrice(), filters.maxPrice());
    }

    public Product toProduct() {
        return new Product(source, externalId, name, manufacturer, chipset, chipsetManufacturer, memorySizeGb, memoryType,
                boostClockMhz, description, price, currency, productUrl == null ? null : URI.create(productUrl), attributes);
    }

    private boolean contains(String value, String query) {
        return query == null || query.isBlank()
                || value != null && value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    private boolean containsAny(String value, java.util.List<String> values) {
        return values.isEmpty() || values.stream().anyMatch(candidate -> value != null && value.equalsIgnoreCase(candidate));
    }

    private <T extends Comparable<? super T>> boolean inRange(T value, T minimum, T maximum) {
        return (minimum == null || value != null && value.compareTo(minimum) >= 0)
                && (maximum == null || value != null && value.compareTo(maximum) <= 0);
    }
}
