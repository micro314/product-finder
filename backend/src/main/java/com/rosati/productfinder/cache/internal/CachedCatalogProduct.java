package com.rosati.productfinder.cache.internal;

import com.rosati.productfinder.product.Product;
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

    public boolean matches(String query) {
        return contains(name, query) || contains(manufacturer, query) || contains(chipset, query)
                || contains(description, query);
    }

    public Product toProduct() {
        return new Product(source, externalId, name, manufacturer, chipset, memorySizeGb, memoryType,
                boostClockMhz, description, price, currency, productUrl == null ? null : URI.create(productUrl), attributes);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
