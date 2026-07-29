package com.rosati.productfinder.product;

import java.math.BigDecimal;
import java.util.List;

public record ProductFilters(
        List<String> source, List<String> manufacturer, List<String> chipsetManufacturer,
        List<String> chipset, List<String> memoryType,
        Integer minMemorySizeGb, Integer maxMemorySizeGb, Integer minBoostClockMhz, Integer maxBoostClockMhz,
        BigDecimal minPrice, BigDecimal maxPrice
) {
    public ProductFilters {
        source = source == null ? List.of() : List.copyOf(source);
        manufacturer = manufacturer == null ? List.of() : List.copyOf(manufacturer);
        chipsetManufacturer = chipsetManufacturer == null ? List.of() : List.copyOf(chipsetManufacturer);
        chipset = chipset == null ? List.of() : List.copyOf(chipset);
        memoryType = memoryType == null ? List.of() : List.copyOf(memoryType);
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
        return new ProductFilters(List.of(), List.of(), List.of(), List.of(), List.of(), null, null, null,
                null, null, null);
    }

    public boolean isEmpty() {
        return source.isEmpty() && manufacturer.isEmpty() && chipsetManufacturer.isEmpty() && chipset.isEmpty()
                && memoryType.isEmpty() && minMemorySizeGb == null && maxMemorySizeGb == null
                && minBoostClockMhz == null && maxBoostClockMhz == null && minPrice == null && maxPrice == null;
    }
}
