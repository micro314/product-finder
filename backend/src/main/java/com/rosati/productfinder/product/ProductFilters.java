package com.rosati.productfinder.product;

import java.math.BigDecimal;

public record ProductFilters(
        String source, String manufacturer, String chipsetManufacturer,
        String chipset, String memoryType,
        Integer minMemorySizeGb, Integer maxMemorySizeGb, Integer minBoostClockMhz, Integer maxBoostClockMhz,
        BigDecimal minPrice, BigDecimal maxPrice
) {
    public ProductFilters {
        if (minMemorySizeGb != null && minMemorySizeGb < 0 || maxMemorySizeGb != null && maxMemorySizeGb < 0
                || minBoostClockMhz != null && minBoostClockMhz < 0 || maxBoostClockMhz != null && maxBoostClockMhz < 0
                || minPrice != null && minPrice.signum() < 0 || maxPrice != null && maxPrice.signum() < 0) {
            throw new IllegalArgumentException("numeric filters must not be negative");
        }
        if (minMemorySizeGb != null && maxMemorySizeGb != null && minMemorySizeGb > maxMemorySizeGb
                || minBoostClockMhz != null && maxBoostClockMhz != null && minBoostClockMhz > maxBoostClockMhz
                || minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minimum filters must not exceed maximum filters");
        }
    }

    public static ProductFilters empty() {
        return new ProductFilters(null, null, null, null, null, null, null, null,
                null, null, null);
    }

    public boolean isEmpty() {
        return source == null && manufacturer == null && chipsetManufacturer == null && chipset == null
                && memoryType == null && minMemorySizeGb == null && maxMemorySizeGb == null
                && minBoostClockMhz == null && maxBoostClockMhz == null && minPrice == null && maxPrice == null;
    }
}
