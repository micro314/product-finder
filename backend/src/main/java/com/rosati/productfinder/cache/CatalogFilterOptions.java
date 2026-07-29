package com.rosati.productfinder.cache;

import java.math.BigDecimal;
import java.util.List;

public record CatalogFilterOptions(
        List<String> sources, List<String> manufacturers,
        List<String> chipsetManufacturers, List<String> chipsets, List<String> memoryTypes,
        List<Integer> memorySizesGb,
        List<Integer> boostClockFrequenciesMhz, List<BigDecimal> prices
) { }
