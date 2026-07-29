package com.rosati.productfinder.product;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

public record Product(
        String source,
        String externalId,
        String name,
        String manufacturer,
        String chipset,
        int memorySizeGb,
        String memoryType,
        Integer boostClockMhz,
        String description,
        BigDecimal price,
        String currency,
        URI productUrl,
        Map<String, Object> attributes
) {
    public Product {
        source = requireText(source, "source");
        externalId = requireText(externalId, "externalId");
        name = requireText(name, "name");
        manufacturer = requireText(manufacturer, "manufacturer");
        chipset = requireText(chipset, "chipset");
        if (memorySizeGb < 1) {
            throw new IllegalArgumentException("memorySizeGb must be positive");
        }
        memoryType = requireText(memoryType, "memoryType");
        description = description == null ? "" : description;
        currency = currency == null ? "" : currency;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public Product(String source, String externalId, String name, String manufacturer, String chipset,
                   int memorySizeGb, String memoryType, String description, BigDecimal price, String currency,
                   URI productUrl, Map<String, Object> attributes) {
        this(source, externalId, name, manufacturer, chipset, memorySizeGb, memoryType, null,
                description, price, currency, productUrl, attributes);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
